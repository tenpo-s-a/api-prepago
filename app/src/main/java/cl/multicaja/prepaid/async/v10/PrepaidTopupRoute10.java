package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.prepaid.async.v10.processors.PendingCard10;
import cl.multicaja.prepaid.async.v10.processors.PendingTopup10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.TecnocomServiceMockImpl;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import java.sql.SQLException;

/**
 * Implementacion personalizada de rutas camel
 *
 * @autor vutreras
 */
public final class PrepaidTopupRoute10 extends CamelRouteBuilder {

  private static Log log = LogFactory.getLog(PrepaidTopupRoute10.class);

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private UsersEJBBean10 usersEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  private TecnocomService tecnocomService;
  private ParametersUtil parametersUtil;
  private ConfigUtils configUtils;
  private EncryptUtil encryptUtil;

  public PrepaidTopupRoute10() {
    super();
  }

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  public EncryptUtil getEncryptUtil(){
    if(this.encryptUtil == null){
      this.encryptUtil = new EncryptUtil();
    }
    return this.encryptUtil;
  }

  public ParametersUtil getParametersUtil() {
    if (parametersUtil == null) {
      parametersUtil = ParametersUtil.getInstance();
    }
    return parametersUtil;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  public PrepaidEJBBean10 getPrepaidEJBBean10() {
    return prepaidEJBBean10;
  }

  public void setPrepaidEJBBean10(PrepaidEJBBean10 prepaidEJBBean10) {
    this.prepaidEJBBean10 = prepaidEJBBean10;
  }

  public UsersEJBBean10 getUsersEJBBean10() {
    return usersEJBBean10;
  }

  public void setUsersEJBBean10(UsersEJBBean10 usersEJBBean10) {
    this.usersEJBBean10 = usersEJBBean10;
  }

  public TecnocomService getTecnocomService() {
    //TODO se usa la version mock mientras, se debe implementar como resolver que instancia usar la mock o la real de acuerdo al ambiente
    if (this.tecnocomService == null) {
      String apiKey = getConfigUtils().getProperty("tecnocom.apiKey");
      String apiUrl = getConfigUtils().getProperty("tecnocom.apiUrl");
      String channel = getConfigUtils().getProperty("tecnocom.channel");
      String codent = null;
      try {
        codent = getParametersUtil().getString("api-prepaid", "cod_entidad", "v10");
      } catch (SQLException e) {
        log.error("Error al cargar parametro cod_entidad");
        codent = getConfigUtils().getProperty("tecnocom.codEntity");
      }
      this.tecnocomService = new TecnocomServiceMockImpl(apiKey, apiUrl, channel, codent, HashOrder.ASC);
    }
    return tecnocomService;
  }

  public void setTecnocomService(TecnocomService tecnocomService) {
    this.tecnocomService = tecnocomService;
  }

  public static final String PENDING_TOPUP_REQ = "PrepaidTopupRoute10.pendingTopup.req";
  public static final String PENDING_TOPUP_RESP = "PrepaidTopupRoute10.pendingTopup.resp";

  public static final String PENDING_EMISSION_REQ = "PrepaidTopupRoute10.pendingEmission.req";
  public static final String PENDING_EMISSION_RESP = "PrepaidTopupRoute10.pendingEmission.resp";

  public static final String PENDING_CREATECARD_REQ = "PrepaidTopupRoute10.pendingCreateCard.req";
  public static final String PENDING_CREATECARD_RESP = "PrepaidTopupRoute10.pendingCreateCard.resp";

  public static final String ERROR_EMISSION_REQ = "PrepaidTopupRoute10.errorEmission.req";
  public static final String ERROR_EMISSION_RESP = "PrepaidTopupRoute10.errorEmission.resp";

  public static final String ERROR_CREATECARD_REQ = "PrepaidTopupRoute10.errorCreateCard.req";
  public static final String ERROR_CREATECARD_RESP = "PrepaidTopupRoute10.errorCreateCard.resp";


  @Override
  public void configure() {

    int concurrentConsumers = 10;
    int sedaSize = 1000;

    /**
     * Cargas pendientes
     */

    //consume un mensaje desde un componente seda de alta velocidad y lo envia a una cola de requerimientos
    from(String.format("seda:PrepaidTopupRoute10.pendingTopup?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_TOPUP_REQ));

    //consume un mensaje desde una cola de requerimientos y lo envia a una cola de respuestas
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_TOPUP_REQ, concurrentConsumers)))
      .process(new PendingTopup10(this).processPendingTopup())
      .to(createJMSEndpoint(PENDING_TOPUP_RESP)).end();

    /**
     * Emisiones pendientes
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_EMISSION_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processPendingEmission())
      .to(createJMSEndpoint(PENDING_EMISSION_RESP)).end();

    /**
     * Obtener Datos Tarjeta
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_CREATECARD_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processPendingCreateCard())
      .to(createJMSEndpoint(PENDING_CREATECARD_RESP)).end();

    /**
     * Error Emisiones
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_EMISSION_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processErrorEmission())
      .to(createJMSEndpoint(ERROR_EMISSION_RESP)).end();

    /**
     * Error Obtener Datos Tarjeta
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_CREATECARD_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processErrorCreateCard())
      .to(createJMSEndpoint(ERROR_CREATECARD_RESP)).end();

  }
}
