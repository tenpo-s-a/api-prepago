package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.PendingSendMail10;

public class MailRoute10 extends BaseRoute10 {

  /*
    Comprobante de Carga
   */
  public static final String SEDA_PENDING_SEND_MAIL_TOPUP = "seda:MailRoute10.pendingSendMailTopup";
  public static final String PENDING_SEND_MAIL_TOPUP_REQ = "MailRoute10.pendingSendMailTopup.req";
  public static final String PENDING_SEND_MAIL_TOPUP_RESP = "MailRoute10.pendingSendMailTopup.resp";
  public static final String ERROR_SEND_MAIL_TOPUP_REQ = "MailRoute10.errorSendMailTopup.req";
  public static final String ERROR_SEND_MAIL_TOPUP_RESP = "MailRoute10.errorSendMailTopup.resp";

  /*
    Comprobante de retiro
   */
  public static final String SEDA_PENDING_SEND_MAIL_WITHDRAW = "seda:MailRoute10.pendingSendMailWithdraw";
  public static final String PENDING_SEND_MAIL_WITHDRAW_REQ = "MailRoute10.pendingSendMailWithdraw.req";
  public static final String PENDING_SEND_MAIL_WITHDRAW_RESP = "MailRoute10.pendingSendMailWithdraw.resp";
  public static final String ERROR_SEND_MAIL_WITHDRAW_REQ = "MailRoute10.errorSendMailWithdraw.req";
  public static final String ERROR_SEND_MAIL_WITHDRAW_RESP = "MailRoute10.errorSendMailWithdraw.resp";

  /*
    Comprobante de solicitud retiro TEF
   */
  public static final String SEDA_PENDING_SEND_MAIL_WEB_WITHDRAW_REQUEST = "seda:MailRoute10.pendingSendMailWebWithdrawRequest";
  public static final String PENDING_SEND_MAIL_WEB_WITHDRAW_REQUEST_REQ = "MailRoute10.pendingSendMailWebWithdrawRequest.req";
  public static final String PENDING_SEND_MAIL_WEB_WITHDRAW_REQUEST_RESP = "MailRoute10.pendingSendMailWebWithdrawRequest.resp";

  /*
    Comprobante de retiro exitoso
   */
  public static final String SEDA_PENDING_SEND_MAIL_WITHDRAW_SUCCESS = "seda:MailRoute10.pendingSendMailWithdrawSuccess";
  public static final String PENDING_SEND_MAIL_WITHDRAW_SUCCESS_REQ = "MailRoute10.pendingSendMailWithdrawSuccess.req";
  public static final String PENDING_SEND_MAIL_WITHDRAW_SUCCESS_RESP = "MailRoute10.pendingSendMailWithdrawSuccess.resp";
  public static final String ERROR_SEND_MAIL_WITHDRAW_SUCCESS_REQ = "MailRoute10.errorSendMailWithdrawSuccess.req";
  public static final String ERROR_SEND_MAIL_WITHDRAW_SUCCESS_RESP = "MailRoute10.errorSendMailWithdrawSuccess.resp";

  /*
    Comprobante de retiro fallado
   */
  public static final String SEDA_PENDING_SEND_MAIL_WITHDRAW_FAILED = "seda:MailRoute10.pendingSendMailWithdrawFailed";
  public static final String PENDING_SEND_MAIL_WITHDRAW_FAILED_REQ = "MailRoute10.pendingSendMailWithdrawFailed.req";
  public static final String PENDING_SEND_MAIL_WITHDRAW_FAILED_RESP = "MailRoute10.pendingSendMailWithdrawFailed.resp";
  public static final String ERROR_SEND_MAIL_WITHDRAW_FAILED_REQ = "MailRoute10.errorSendMailWithdrawFailed.req";
  public static final String ERROR_SEND_MAIL_WITHDRAW_FAILED_RESP = "MailRoute10.errorSendMailWithdrawFailed.resp";

  /*
    Comprobante de devolucion exitosa
   */
  public static final String SEDA_PENDING_SEND_MAIL_TOPUP_REFUND_SUCCESS = "seda:MailRoute10.pendingSendMailTopupRefundSuccess";
  public static final String PENDING_SEND_MAIL_TOPUP_REFUND_SUCCESS_REQ = "MailRoute10.pendingSendMailTopupRefundSuccess.req";
  public static final String PENDING_SEND_MAIL_TOPUP_REFUND_SUCCESS_RESP = "MailRoute10.pendingSendMailTopupRefundSuccess.resp";


  @Override
  public void configure() throws Exception {

    int concurrentConsumers = 10;
    int sedaSize = 1000;

    //los mensajes de las colas de respuesta se usan para verificaciones en los test, en la practica no se usan realmente
    //dado eso se establece un tiempo de vida de esos mensajes de solo 10 minutos
    String confResp = "?timeToLive=" + 600000;

    /**
     * Envio recibo de carga
     */

    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_PENDING_SEND_MAIL_TOPUP, concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MAIL_TOPUP_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_TOPUP_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingTopupMail())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_TOPUP_RESP + confResp)).end();

    //Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_SEND_MAIL_TOPUP_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processErrorPendingTopupMail())
      .to(createJMSEndpoint(ERROR_SEND_MAIL_TOPUP_RESP)).end();


    /**
     * Envio recibo de retiro
     */

    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_PENDING_SEND_MAIL_WITHDRAW, concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_WITHDRAW_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingWithdrawMail())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_RESP + confResp)).end();

    //Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_SEND_MAIL_WITHDRAW_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processErrorPendingWithdrawMail())
      .to(createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_RESP)).end();

    /**
     * Envio de confirmacion retiro exitoso
     */

    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_PENDING_SEND_MAIL_WITHDRAW_SUCCESS, concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_SUCCESS_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_WITHDRAW_SUCCESS_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingWebWithdrawSuccessMail())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_SUCCESS_RESP + confResp)).end();

    //Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_SEND_MAIL_WITHDRAW_SUCCESS_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processErrorPendingWithdrawSuccessMail())
      .to(createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_SUCCESS_RESP)).end();

    /**
     * Envio de confirmacion retiro fallado
     */

    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_PENDING_SEND_MAIL_WITHDRAW_FAILED, concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_FAILED_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_WITHDRAW_FAILED_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingWebWithdrawFailedMail())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WITHDRAW_FAILED_RESP + confResp)).end();

    //Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_SEND_MAIL_WITHDRAW_FAILED_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processErrorPendingWithdrawFailedMail())
      .to(createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_FAILED_RESP)).end();


    /**
     * Envio de confirmacion de devolucion
     */

    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_PENDING_SEND_MAIL_TOPUP_REFUND_SUCCESS, concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MAIL_TOPUP_REFUND_SUCCESS_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_TOPUP_REFUND_SUCCESS_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingTopupRefundConfirmationMail())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_TOPUP_REFUND_SUCCESS_RESP + confResp)).end();


    /**
     * Envio de solicitud retiro TEF
     */

    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_PENDING_SEND_MAIL_WEB_WITHDRAW_REQUEST, concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WEB_WITHDRAW_REQUEST_REQ));

    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_WEB_WITHDRAW_REQUEST_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingWebWithdrawRequestMail())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_WEB_WITHDRAW_REQUEST_RESP + confResp)).end();

  }
}
