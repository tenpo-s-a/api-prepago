package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.PdfUtils;
import cl.multicaja.prepaid.async.v10.processors.*;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;

/**
 * Implementacion personalizada de rutas camel
 *
 * @autor vutreras
 */
public final class PrepaidTopupRoute10 extends CamelRouteBuilder {

  private static Log log = LogFactory.getLog(PrepaidTopupRoute10.class);

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private UsersEJBBean10 usersEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

  @EJB
  private MailEJBBean10 mailEJBBean10;

  private TecnocomService tecnocomService;
  private ParametersUtil parametersUtil;
  private ConfigUtils configUtils;
  private EncryptUtil encryptUtil;
  private PdfUtils pdfUtils;
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
  public PdfUtils getPdfUtils(){
    if(pdfUtils == null){
      pdfUtils = PdfUtils.getInstance();
    }
    return pdfUtils;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJBBean10() {
    return prepaidCardEJBBean10;
  }

  public void setPrepaidCardEJBBean10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
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

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  public CdtEJBBean10 getCdtEJBBean10() {
    return cdtEJBBean10;
  }

  public void setCdtEJBBean10(CdtEJBBean10 cdtEJBBean10) {
    this.cdtEJBBean10 = cdtEJBBean10;
  }

  public MailEJBBean10 getMailEJBBean10() {
    return mailEJBBean10;
  }

  public void setMailEJBBean10(MailEJBBean10 mailEJBBean10) {
    this.mailEJBBean10 = mailEJBBean10;
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

  public static final String PENDING_CREATE_CARD_REQ = "PrepaidTopupRoute10.pendingCreateCard.req";
  public static final String PENDING_CREATE_CARD_RESP = "PrepaidTopupRoute10.pendingCreateCard.resp";

  public static final String PENDING_TOPUP_REVERSE_CONFIRMATION_REQ = "PrepaidTopupRoute10.pendingTopupReverseConfirmation.req";
  public static final String PENDING_TOPUP_REVERSE_CONFIRMATION_RESP = "PrepaidTopupRoute10.pendingTopupReverseConfirmation.resp";

  public static final String ERROR_EMISSION_REQ = "PrepaidTopupRoute10.errorEmission.req";
  public static final String ERROR_EMISSION_RESP = "PrepaidTopupRoute10.errorEmission.resp";

  public static final String ERROR_CREATE_CARD_REQ = "PrepaidTopupRoute10.errorCreateCard.req";
  public static final String ERROR_CREATE_CARD_RESP = "PrepaidTopupRoute10.errorCreateCard.resp";

  public static final String PENDING_CARD_ISSUANCE_FEE_REQ = "PrepaidTopupRoute10.pendingCardIssuanceFee.req";
  public static final String PENDING_CARD_ISSUANCE_FEE_RESP = "PrepaidTopupRoute10.pendingCardIssuanceFee.resp";

  public static final String ERROR_CARD_ISSUANCE_FEE_REQ = "PrepaidTopupRoute10.errorCardIssuanceFee.req";
  public static final String ERROR_CARD_ISSUANCE_FEE_RESP = "PrepaidTopupRoute10.errorCardIssuanceFee.resp";

  public static final String PENDING_SEND_MAIL_CARD_REQ = "PrepaidTopupRoute10.pendingSendMailCard.req";
  public static final String PENDING_SEND_MAIL_CARD_RESP = "PrepaidTopupRoute10.pendingSendMailCard.resp";

  public static final String ERROR_SEND_MAIL_CARD_REQ = "PrepaidTopupRoute10.errorSendMailCard.req";
  public static final String ERROR_SEND_MAIL_CARD_RESP = "PrepaidTopupRoute10.errorSendMailCard.resp";

  public static final String PENDING_SEND_MAIL_WITHDRAW_REQ = "PrepaidTopupRoute10.pendingSendMailWithdraw.req";
  public static final String PENDING_SEND_MAIL_WITHDRAW_RESP = "PrepaidTopupRoute10.pendingSendMailWithdraw.resp";

  public static final String ERROR_SEND_MAIL_WITHDRAW_REQ = "PrepaidTopupRoute10.errorSendMailWithdraw.req";
  public static final String ERROR_SEND_MAIL_WITHDRAW_RESP = "PrepaidTopupRoute10.errorSendMailWithdraw.resp";


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
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_CREATE_CARD_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processPendingCreateCard())
      .to(createJMSEndpoint(PENDING_CREATE_CARD_RESP)).end();

    /**
     * Error Emisiones
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_EMISSION_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processErrorEmission())
      .to(createJMSEndpoint(ERROR_EMISSION_RESP)).end();

    /**
     * Error Obtener Datos Tarjeta
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_CREATE_CARD_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processErrorCreateCard())
      .to(createJMSEndpoint(ERROR_CREATE_CARD_RESP)).end();

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

    /**
     * Envio recibo de retiro
     */

    from(String.format("seda:PrepaidTopupRoute10.pendingWithdrawMail?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_WITHDRAW_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingWithdrawMail())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_RESP)).end();

    //Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_SEND_MAIL_WITHDRAW_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processErrorPendingWithdrawMail())
      .to(createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_RESP)).end();
  }
}
