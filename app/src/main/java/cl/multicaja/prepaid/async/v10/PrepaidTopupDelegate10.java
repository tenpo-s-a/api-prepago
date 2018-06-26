package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   * Envia un registro de withdraw al proceso asincrono
   *
   * @param prepaidWithdraw
   * @param user
   * @return
   */
  public String sendWithdraw(PrepaidWithdraw10 prepaidWithdraw, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%s#%s", prepaidWithdraw.getMerchantCode(), prepaidWithdraw.getTransactionId(), prepaidWithdraw.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidWithdraw.setMessageId(messageId);

    String endpoint = "seda:PrepaidTopupRoute10.pendingWithdrawMail";

    PrepaidTopupData10 data = new PrepaidTopupData10();
    data.setPrepaidWithdraw10(prepaidWithdraw);
    data.setUser(user);
    data.setCdtTransaction10(cdtTransaction);
    data.setPrepaidMovement10(prepaidMovement);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));

    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

    return messageId;
  }

  /**
   * Envia un registro de confirmacion de reversa de topup al proceso asincrono
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @param prepaidMovement
   * @return id del mensaje
   */
  //TODO: Verificar donde sera invocado este metodo
  public String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%s#%s", prepaidTopup.getMerchantCode(), prepaidTopup.getTransactionId(), prepaidTopup.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidTopup.setMessageId(messageId);

    String endpoint = "seda:PrepaidTopupRoute10.pendingTopupReverseConfirmation";

    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));

    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

    return messageId;
  }
}
