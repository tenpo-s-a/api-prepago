package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.accounting.async.v10.processors.PendingStoreInAccountingProcessor10;
import cl.multicaja.prepaid.async.v10.processors.*;

/**
 * Implementacion personalizada de rutas camel
 *
 * @autor vutreras
 */
public final class PrepaidTopupRoute10 extends BaseRoute10 {

  public static final String PENDING_TOPUP_SEDA = "seda:PrepaidTopupRoute10.pendingTopup";
  public static final String PENDING_TOPUP_REQ = "PrepaidTopupRoute10.pendingTopup.req";
  public static final String PENDING_TOPUP_RESP = "PrepaidTopupRoute10.pendingTopup.resp";

  public static final String ERROR_TOPUP_REQ = "PrepaidTopupRoute10.errorTopup.req";
  public static final String ERROR_TOPUP_RESP = "PrepaidTopupRoute10.errorTopup.resp";

  public static final String PENDING_EMISSION_REQ = "PrepaidTopupRoute10.pendingEmission.req";
  public static final String PENDING_EMISSION_RESP = "PrepaidTopupRoute10.pendingEmission.resp";

  public static final String PENDING_CREATE_CARD_REQ = "PrepaidTopupRoute10.pendingCreateCard.req";
  public static final String PENDING_CREATE_CARD_RESP = "PrepaidTopupRoute10.pendingCreateCard.resp";

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

  public static final String SEDA_SEND_MOVEMENT_TO_ACCOUNTING_REQ = "seda:PrepaidTopupRoute10.pendingSendMovementToAccounting";
  public static final String PENDING_SEND_MOVEMENT_TO_ACCOUNTING_REQ = "PrepaidTopupRoute10.pendingSendMovementToAccounting.req";
  public static final String PENDING_SEND_MOVEMENT_TO_ACCOUNTING_RESP = "PrepaidTopupRoute10.pendingSendMovementToAccounting.resp";

  @Override
  public void configure() throws Exception {

    int concurrentConsumers = 10;
    int sedaSize = 1000;

    //los mensajes de las colas de respuesta se usan para verificaciones en los test, en la practica no se usan realmente
    //dado eso se establece un tiempo de vida de esos mensajes de solo 10 minutos
    String confResp = "?timeToLive=" + 600000;

    /**
     * Cargas pendientes
     */

    //consume un mensaje desde un componente seda de alta velocidad y lo envia a una cola de requerimientos
    from(String.format("seda:PrepaidTopupRoute10.pendingTopup?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_TOPUP_REQ));

    //consume un mensaje desde una cola de requerimientos y lo envia a una cola de respuestas
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_TOPUP_REQ, concurrentConsumers)))
      .process(new PendingTopup10(this).processPendingTopup())
      .to(createJMSEndpoint(PENDING_TOPUP_RESP + confResp)).end();

    /**
     * Error Emisiones
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_TOPUP_REQ, concurrentConsumers)))
      .process(new PendingTopup10(this).processErrorTopup())
      .to(createJMSEndpoint(ERROR_TOPUP_RESP)).end();

    /**
     * Emisiones pendientes
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_EMISSION_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processPendingEmission())
      .to(createJMSEndpoint(PENDING_EMISSION_RESP + confResp)).end();

    /**
     * Obtener Datos Tarjeta
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_CREATE_CARD_REQ, concurrentConsumers)))
      .process(new PendingCard10(this).processPendingCreateCard())
      .to(createJMSEndpoint(PENDING_CREATE_CARD_RESP + confResp)).end();

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
     * Cobros de emisión pendientes
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_CARD_ISSUANCE_FEE_REQ, concurrentConsumers)))
      .process(new PendingCardIssuanceFee10(this).processPendingIssuanceFee())
      .to(createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_RESP + confResp)).end();

    // Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_CARD_ISSUANCE_FEE_REQ, concurrentConsumers)))
      .process(new PendingCardIssuanceFee10(this).processErrorPendingIssuanceFee())
      .to(createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_RESP)).end();

    /**
     * Envio Mail Tarjeta
     */
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MAIL_CARD_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processPendingSendMailCard())
      .to(createJMSEndpoint(PENDING_SEND_MAIL_CARD_RESP + confResp)).end();

    // Errores
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_SEND_MAIL_CARD_REQ, concurrentConsumers)))
      .process(new PendingSendMail10(this).processErrorPendingSendMailCard())
      .to(createJMSEndpoint(ERROR_SEND_MAIL_CARD_RESP + confResp)).end();

    /**
     * Envio de movement a accounting
     */
    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_SEND_MOVEMENT_TO_ACCOUNTING_REQ, concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_SEND_MOVEMENT_TO_ACCOUNTING_REQ));
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_SEND_MOVEMENT_TO_ACCOUNTING_REQ, concurrentConsumers)))
      .process(new PendingStoreInAccountingProcessor10(this).storeInAccounting())
      .to(createJMSEndpoint(PENDING_SEND_MOVEMENT_TO_ACCOUNTING_RESP + confResp)).end();
  }
}
