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
  }
}
