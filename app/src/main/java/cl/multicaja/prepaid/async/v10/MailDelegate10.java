package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.SEDA_PENDING_SEND_MAIL_TOPUP;
import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.SEDA_PENDING_SEND_MAIL_WITHDRAW;

/**
 * Clase delegate que permite iniciar los procesos asincronos
 *
 * @autor abarazarte
 */
public final class MailDelegate10 {

  private static Log log = LogFactory.getLog(MailDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private ProducerTemplate producerTemplate;

  public MailDelegate10() {
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
   * Envia un registro de withdraw al proceso asincrono
   *
   * @param prepaidTopup
   * @param user
   *
   *
   * @return
   */
  public String sendTopupMail(PrepaidTopup10 prepaidTopup, User user, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%s#%s", prepaidTopup.getMerchantCode(), prepaidTopup.getTransactionId(), prepaidTopup.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidTopup.setMessageId(messageId);

    PrepaidTopupData10 data = new PrepaidTopupData10();
    data.setUser(user);
    data.setPrepaidMovement10(prepaidMovement);
    data.setPrepaidTopup10(prepaidTopup);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_PENDING_SEND_MAIL_TOPUP));

    this.getProducerTemplate().sendBodyAndHeaders(SEDA_PENDING_SEND_MAIL_TOPUP, req, headers);

    return messageId;
  }

  /**
   * Envia un registro de withdraw al proceso asincrono
   *
   * @param prepaidWithdraw
   * @param user
   * @return
   */
  public String sendWithdrawRequestMail(PrepaidWithdraw10 prepaidWithdraw, User user, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%s#%s", prepaidWithdraw.getMerchantCode(), prepaidWithdraw.getTransactionId(), prepaidWithdraw.getId(), Utils.uniqueCurrentTimeNano());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidWithdraw.setMessageId(messageId);

    PrepaidTopupData10 data = new PrepaidTopupData10();
    data.setPrepaidWithdraw10(prepaidWithdraw);
    data.setUser(user);
    data.setPrepaidMovement10(prepaidMovement);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_PENDING_SEND_MAIL_WITHDRAW));

    this.getProducerTemplate().sendBodyAndHeaders(SEDA_PENDING_SEND_MAIL_WITHDRAW, req, headers);

    return messageId;
  }
}