package cl.multicaja.prepaid.async.v10;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Queue;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase delegate que permite iniciar los procesos asincronos
 *
 * @autor vutreras
 */
public final class PrepaidTopupDelegate10 {

  private static Log log = LogFactory.getLog(PrepaidTopupDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private ProducerTemplate producerTemplate;

  public PrepaidTopupDelegate10() {
    super();
  }

  /**
   *
   * @return
   */
  public ProducerTemplate getProducerTemplate() {
    if (this.producerTemplate == null) {
      this.producerTemplate = this.camelFactory.createProducerTemplate();
    }
    return producerTemplate;
  }

  /**
   * Envia un registro de topup al proceso asincrono
   *
   * @param prepaidTopup
   * @param user
   * @return
   */
  public String sendTopUp(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%s#%s", prepaidTopup.getMerchantCode(), prepaidTopup.getTransactionId(), prepaidTopup.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidTopup.setMessageId(messageId);

    String endpoint = "seda:PrepaidTopupRoute10.pendingTopup";

    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));

    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

    return messageId;
  }

  /**
   * V2
   * @param prepaidTopup
   * @param prepaidUser10
   * @param cdtTransaction
   * @param prepaidMovement
   * @return
   */
  public String sendTopUp(PrepaidTopup10 prepaidTopup, PrepaidUser10 prepaidUser10, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%s#%s", prepaidTopup.getMerchantCode(), prepaidTopup.getTransactionId(), prepaidTopup.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidTopup.setMessageId(messageId);

    String endpoint = "seda:PrepaidTopupRoute10.pendingTopup";

    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, prepaidUser10, cdtTransaction, prepaidMovement);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));

    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

    return messageId;
  }


  public String sendPendingWithdrawReversal(PrepaidWithdraw10 prepaidWithdraw, PrepaidUser10 prepaidUser10, PrepaidMovement10 reverse) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = String.format("%s#%s#%s#%s", prepaidWithdraw.getMerchantCode(), prepaidWithdraw.getTransactionId(), prepaidWithdraw.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidWithdraw.setMessageId(messageId);

    String endpoint = "seda:TransactionReversalRoute10.pendingReversalWithdraw";

    PrepaidReverseData10 data = new PrepaidReverseData10();
    data.setPrepaidWithdraw10(prepaidWithdraw);
    data.setPrepaidUser10(prepaidUser10);
    data.setPrepaidMovementReverse(reverse);

    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));
    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

    return messageId;
  }

  public String sendPendingTopupReverse(PrepaidTopup10 prepaidTopup,PrepaidCard10 prepaidCard10, PrepaidUser10 prepaidUser10, PrepaidMovement10 reverse) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = String.format("%s#%s#%s#%s", prepaidTopup.getMerchantCode(), prepaidTopup.getTransactionId(), prepaidTopup.getId(), Utils.uniqueCurrentTimeNano());


    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidTopup.setMessageId(messageId);

    String endpoint = "seda:TransactionReversalRoute10.pendingReversalTopup";

    PrepaidReverseData10 data = new PrepaidReverseData10();
    data.setPrepaidTopup10(prepaidTopup);
    data.setPrepaidUser10(prepaidUser10);
    data.setPrepaidMovementReverse(reverse);
    data.setPrepaidCard10(prepaidCard10);

    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));
    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

    return messageId;
  }


  public String sendMovementToAccounting(PrepaidMovement10 prepaidWithdraw, UserAccount userAccount) {
    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    // Se crea un messageId unico
    String messageId = String.format("%s#%s", prepaidWithdraw.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);

    String endpoint = PrepaidTopupRoute10.SEDA_SEND_MOVEMENT_TO_ACCOUNTING_REQ;

    PrepaidTopupData10 data = new PrepaidTopupData10();
    data.setPrepaidMovement10(prepaidWithdraw);
    data.setUserAccount(userAccount);
    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(0);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));

    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

    return messageId;
  }
}
