package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.TecnocomServiceMockImpl;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Implementacion personalizada de rutas camel
 *
 * @autor vutreras
 */
public final class PrepaidTopupRoute10 extends CamelRouteBuilder {

  private static Log log = LogFactory.getLog(PrepaidTopupRoute10.class);

  private ParametersUtil parametersUtil = ParametersUtil.getInstance();

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private UsersEJBBean10 usersEJBBean10;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

  private TecnocomService tecnocomService;

  private ConfigUtils configUtils ;

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

  public NumberUtils getNumberUtils() {
    if (this.numberUtils == null) {
      this.numberUtils = NumberUtils.getInstance();
    }
    return this.numberUtils;
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
      String codEntity = null;
      try {
        codEntity = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
      } catch (SQLException e) {
        log.error("Error al cargar parametro cod_entidad");
        codEntity = getConfigUtils().getProperty("tecnocom.codEntity");
      }
      this.tecnocomService = new TecnocomServiceMockImpl(apiKey, apiUrl, channel, codEntity, HashOrder.ASC);
    }
    return tecnocomService;
  }

  public void setTecnocomService(TecnocomService tecnocomService) {
    this.tecnocomService = tecnocomService;
  }

  public CdtEJBBean10 getCdtEJBBean10() {
    return cdtEJBBean10;
  }

  public void setCdtEJBBean10(CdtEJBBean10 cdtEJBBean10) {
    this.cdtEJBBean10 = cdtEJBBean10;
  }

  public static final String PENDING_TOPUP_REQ = "PrepaidTopupRoute10.pendingTopup.req";
  public static final String PENDING_TOPUP_RESP = "PrepaidTopupRoute10.pendingTopup.resp";

  public static final String PENDING_EMISSION_REQ = "PrepaidTopupRoute10.pendingEmission.req";
  public static final String PENDING_EMISSION_RESP = "PrepaidTopupRoute10.pendingEmission.resp";

  public static final String PENDING_TOPUP_REVERSE_REQ = "PrepaidTopupRoute10.pendingTopupReverse.req";
  public static final String PENDING_TOPUP_REVERSE_RESP = "PrepaidTopupRoute10.pendingTopupReverse.resp";

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
      .process(this.processPendingTopup())
      .to(createJMSEndpoint(PENDING_TOPUP_RESP)).end();

    /**
     * Emisiones pendientes
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_EMISSION_REQ, concurrentConsumers)))
      .process(this.processPendingEmission())
      .to(createJMSEndpoint(PENDING_EMISSION_RESP)).end();

    /**
     * Resersa de carga pendiente
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_TOPUP_REVERSE_REQ, concurrentConsumers)))
      .process(this.processPendingEmission())
      .to(createJMSEndpoint(PENDING_TOPUP_REVERSE_RESP)).end();
  }

  private ProcessorRoute processPendingTopup() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopup - REQ: " + req);

        PrepaidTopupDataRoute10 data = req.getData();

        if (data.getUser() == null) {
          log.error("Error req.getUser() es null");
          return null;
        }

        if (data.getUser().getRut() == null) {
          log.error("Error req.getUser().getRut() es null");
          return null;
        }

        Integer rut = data.getUser().getRut().getValue();

        if (rut == null){
          log.error("Error req.getUser().getRut().getValue() es null");
          return null;
        }

        PrepaidUser10 prepaidUser = getPrepaidEJBBean10().getPrepaidUserByRut(null, rut);

        if (prepaidUser == null){
          log.error("Error al buscar PrepaidUser10 con rut: " + rut);
          return null;
        }

        req.getData().setPrepaidUser10(prepaidUser);

        PrepaidCard10 card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE);

        if (card == null) {
          card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED);
        }

        if (card != null) {
          String codEntity = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
          req.getData().setTecnocomCodEntity(codEntity);
          req.getData().setPrepaidCard10(card);
          if (TopupType.WEB.equals(req.getData().getPrepaidTopup().getType())) {
            req.getData().setTecnocomInvoiceType(TipoFactura.CARGA_TRANSFERENCIA);
          } else {
            req.getData().setTecnocomInvoiceType(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA);
          }

          PrepaidTopup10 prepaidTopup = req.getData().getPrepaidTopup();

          String contrato = card.getProcessorUserId();
          String pan = card.getEncryptedPan(); // se debe desencriptar
          CodigoMoneda clamon = CodigoMoneda.CHILE_CLP;
          IndicadorNormalCorrector indnorcor = IndicadorNormalCorrector.NORMAL;
          Integer tipofac = req.getData().getTecnocomInvoiceType().getCode();
          BigDecimal impfac = prepaidTopup.getAmount().getValue();
          String codcom = prepaidTopup.getMerchantCode();
          Integer codact = prepaidTopup.getMerchantCategory();
          CodigoPais codpais = CodigoPais.CHILE;
          String numaut = prepaidTopup.getTransactionId(); //solamente los 6 primeros digitos de numreffac
          String nomcomred = prepaidTopup.getMerchantName();
          String numreffac = prepaidTopup.getTransactionId(); //este cambiara por el id de la tabla de inclusion de movimientos

          //InclusionMovimientosDTO dto = tecnocomService.inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac, numreffac, impfac, numaut, codcom, nomcomred, codact, codpais);

        } else {

          //https://www.pivotaltracker.com/story/show/157816408
          //3-En caso de tener estado bloqueado duro o expirada no se deberá seguir ningún proceso

          card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED_HARD);

          if (card == null) {
            card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.EXPIRED);
          }

          if (card == null) {
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(PENDING_EMISSION_REQ), req, exchange.getIn().getHeaders());
          } else {
            return null;
          }
        }

        return new ResponseRoute<>(req.getData());
      }
    };
  }

  private ProcessorRoute processPendingEmission() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {
        //TODO implementar logica
        log.info("processPendingEmission - REQ: " + req);
        return new ResponseRoute<>(req.getData());
      }
    };
  }

  private ProcessorRoute processPendingTopupReverse() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        PrepaidTopupDataRoute10 data = req.getData();

        if (data.getUser() == null) {
          log.error("Error req.getUser() es null");
          return null;
        }

        if (data.getUser().getRut() == null) {
          log.error("Error req.getUser().getRut() es null");
          return null;
        }

        Integer rut = data.getUser().getRut().getValue();

        if (rut == null){
          log.error("Error req.getUser().getRut().getValue() es null");
          return null;
        }

        PrepaidTopup10 topupRequest = data.getPrepaidTopup();

        CdtTransaction10 cdtTransaction = new CdtTransaction10();
        cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
        cdtTransaction.setGloss(CdtTransactionType.REVERSA_CARGA.getName() + " " + topupRequest.getAmount().getValue());

        cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

        // Si no cumple con los limites
        if(!cdtTransaction.getNumError().equals("0")){
          long lNumError = getNumberUtils().toLong(cdtTransaction.getNumError(),-1L);
          if(lNumError != -1 && lNumError > 10000)
            throw new ValidationException(4).setData(new KeyValue("value",cdtTransaction.getMsjError()));
          else
            throw new ValidationException(2);
        }

        return new ResponseRoute<>(req.getData());
      }
    };
  }
}
