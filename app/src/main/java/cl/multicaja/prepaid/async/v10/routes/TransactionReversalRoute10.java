package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.PendingReverseTopup10;
import cl.multicaja.prepaid.async.v10.processors.PendingReverseWithdraw10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TransactionReversalRoute10 extends BaseRoute10 {

  private static Log log = LogFactory.getLog(CurrencyConvertionRoute10.class);

  public static final String PENDING_REVERSAL_TOPUP_REQ = "TransactionReversalRoute10.pendingReversalTopup.req";
  public static final String PENDING_REVERSAL_TOPUP_RESP = "TransactionReversalRoute10.pendingReversalTopup.resp";

  public static final String ERROR_REVERSAL_TOPUP_REQ = "TransactionReversalRoute10.errorReversalTopup.req";
  public static final String ERROR_REVERSAL_TOPUP_RESP = "TransactionReversalRoute10.errorReversalTopup.resp";

  public static final String PENDING_REVERSAL_WITHDRAW_REQ = "TransactionReversalRoute10.pendingReversalWithdraw.req";
  public static final String PENDING_REVERSAL_WITHDRAW_RESP = "TransactionReversalRoute10.pendingReversalWithdraw.resp";

  public static final String ERROR_REVERSAL_WITHDRAW_REQ = "TransactionReversalRoute10.errorReversalWithdraw.req";
  public static final String ERROR_REVERSAL_WITHDRAW_RESP = "TransactionReversalRoute10.errorReversalWithdraw.resp";

  @Override
  public void configure() throws Exception {
    int concurrentConsumers = 10;
    int sedaSize = 1000;

    //los mensajes de las colas de respuesta se usan para verificaciones en los test, en la practica no se usan realmente
    //dado eso se establece un tiempo de vida de esos mensajes de solo 10 minutos
    String confResp = "?timeToLive=" + 600000;

    /**
     * Reversa de Cargas
     */
    //consume un mensaje desde un componente seda de alta velocidad y lo envia a una cola de requerimientos
    from(String.format("seda:TransactionReversalRoute10.pendingReversalTopup?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ));

    //consume un mensaje desde una cola de requerimientos y lo envia a una cola de respuestas
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_REVERSAL_TOPUP_REQ, concurrentConsumers)))
      .process(new PendingReverseTopup10(this).processPendingTopupReverse())
      .to(createJMSEndpoint(PENDING_REVERSAL_TOPUP_RESP + confResp)).end();

    //Errores Reversa de carga
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_REVERSAL_TOPUP_REQ, concurrentConsumers)))
      .process(new PendingReverseTopup10(this).processErrorTopupReverse())
      .to(createJMSEndpoint(ERROR_REVERSAL_TOPUP_RESP + confResp)).end();

    /**
     * Reversa de Retiros
     */
    //consume un mensaje desde un componente seda de alta velocidad y lo envia a una cola de requerimientos
    from(String.format("seda:TransactionReversalRoute10.pendingReversalWithdraw?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint(PENDING_REVERSAL_WITHDRAW_REQ));

    //consume un mensaje desde una cola de requerimientos y lo envia a una cola de respuestas
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_REVERSAL_WITHDRAW_REQ, concurrentConsumers)))
      .process(new PendingReverseWithdraw10(this).processPendingWithdrawReversal())
      .to(createJMSEndpoint(PENDING_REVERSAL_WITHDRAW_RESP + confResp)).end();

    //Errores Reversa de Retiro
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_REVERSAL_WITHDRAW_REQ, concurrentConsumers)))
      .process(new PendingReverseWithdraw10(this).processErrorWithdrawReversal())
      .to(createJMSEndpoint(ERROR_REVERSAL_WITHDRAW_RESP + confResp)).end();

  }

}
