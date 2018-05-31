package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.prepaid.async.v10.processors.*;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
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

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

  private TecnocomService tecnocomService;
  private ParametersUtil parametersUtil;
  private ConfigUtils configUtils;
  private EncryptUtil encryptUtil;

  private NumberUtils numberUtils;

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

  public NumberUtils getNumberUtils() {
    if (this.numberUtils == null) {
      this.numberUtils = NumberUtils.getInstance();
    }
    return this.numberUtils;
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

  public CdtEJBBean10 getCdtEJBBean10() {
    return cdtEJBBean10;
  }

  public void setCdtEJBBean10(CdtEJBBean10 cdtEJBBean10) {
    this.cdtEJBBean10 = cdtEJBBean10;
  }

  public TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }

  public static final String PENDING_TOPUP_REQ = "PrepaidTopupRoute10.pendingTopup.req";
  public static final String PENDING_TOPUP_RESP = "PrepaidTopupRoute10.pendingTopup.resp";

  public static final String PENDING_TOPUP_RETURNS_REQ = "PrepaidTopupRoute10.pendingTopupReturns.req";
  public static final String PENDING_TOPUP_RETURNS_RESP = "PrepaidTopupRoute10.pendingTopupReturns.resp";

  public static final String PENDING_EMISSION_REQ = "PrepaidTopupRoute10.pendingEmission.req";
  public static final String PENDING_EMISSION_RESP = "PrepaidTopupRoute10.pendingEmission.resp";

  public static final String PENDING_CREATECARD_REQ = "PrepaidTopupRoute10.pendingCreateCard.req";
  public static final String PENDING_CREATECARD_RESP = "PrepaidTopupRoute10.pendingCreateCard.resp";

  public static final String PENDING_TOPUP_REVERSE_CONFIRMATION_REQ = "PrepaidTopupRoute10.pendingTopupReverseConfirmation.req";
  public static final String PENDING_TOPUP_REVERSE_CONFIRMATION_RESP = "PrepaidTopupRoute10.pendingTopupReverseConfirmation.resp";

  public static final String ERROR_EMISSION_REQ = "PrepaidTopupRoute10.errorEmission.req";
  public static final String ERROR_EMISSION_RESP = "PrepaidTopupRoute10.errorEmission.resp";

  public static final String ERROR_CREATECARD_REQ = "PrepaidTopupRoute10.errorCreateCard.req";
  public static final String ERROR_CREATECARD_RESP = "PrepaidTopupRoute10.errorCreateCard.resp";

  public static final String PENDING_CARD_ISSUANCE_FEE_REQ = "PrepaidTopupRoute10.pendingCardIssuanceFee.req";
  public static final String PENDING_CARD_ISSUANCE_FEE_RESP = "PrepaidTopupRoute10.pendingCardIssuanceFee.resp";

  public static final String ERROR_CARD_ISSUANCE_FEE_REQ = "PrepaidTopupRoute10.errorCardIssuanceFee.req";
  public static final String ERROR_CARD_ISSUANCE_FEE_RESP = "PrepaidTopupRoute10.errorCardIssuanceFee.resp";


  public static final String PENDING_SEND_MAIL_CARD_REQ = "PrepaidTopupRoute10.pendingSendMailCard.req";
  public static final String PENDING_SEND_MAIL_CARD_RESP = "PrepaidTopupRoute10.pendingSendMailCard.resp";

  public static final String ERROR_SEND_MAIL_CARD_REQ = "PrepaidTopupRoute10.errorSendMailCard.req";
  public static final String ERROR_SEND_MAIL_CARD_RESP = "PrepaidTopupRoute10.errorSendMailCard.resp";

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
     * devoluciones pendientes
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_TOPUP_RETURNS_REQ, concurrentConsumers)))
      .process(new PendingTopup10(this).processPendingTopupReturns())
      .to(createJMSEndpoint(PENDING_TOPUP_RETURNS_RESP)).end();

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

    /**
     * Confirmacion reversa de topup pendientes
     */
    from(String.format("seda:PrepaidTopupRoute10.pendingTopupReverseConfirmation?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_TOPUP_REVERSE_CONFIRMATION_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_TOPUP_REVERSE_CONFIRMATION_REQ, concurrentConsumers)))
      .process(new PendingTopupReverseConfirmation10(this).processPendingTopupReverseConfirmation())
      .to(createJMSEndpoint(PENDING_TOPUP_REVERSE_CONFIRMATION_RESP)).end();

    /**
     * Cobros de emisión pendientes
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_CARD_ISSUANCE_FEE_REQ, concurrentConsumers)))
      .process(new PendingCardIssuanceFee10(this).processPendingIssuanceFee())
      .to(createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_RESP)).end();

    // Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_CARD_ISSUANCE_FEE_REQ, concurrentConsumers)))
      .process(new PendingCardIssuanceFee10(this).processErrorPendingIssuanceFee())
      .to(createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_RESP)).end();

    /**
     * Envio Mail Tarjeta
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_CARD_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingSendMailCard())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_CARD_RESP)).end();

    // Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_SEND_MAIL_CARD_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processErrorPendingSendMailCard())
      .to(createJMSEndpoint(ERROR_SEND_MAIL_CARD_RESP)).end();
  }
}
