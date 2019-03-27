package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10.SEDA_USER_CREATED_EVENT;
import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.SEDA_PENDING_SEND_MAIL_TOPUP;

public final class KafkaEventDelegate10 {

  private static Log log = LogFactory.getLog(KafkaEventDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private ProducerTemplate producerTemplate;

  public KafkaEventDelegate10() {
    super();
  }

  private ProducerTemplate getProducerTemplate() {
    if (this.producerTemplate == null) {
      this.producerTemplate = this.camelFactory.createProducerTemplate();
    }
    return producerTemplate;
  }

  /**
   * Envia un evento de usuario creado
   *
   */
  public void sendUserCreatedEvent(String body) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {
      Map<String, Object> headers = new HashMap<>();
      headers.put(KafkaConstants.PARTITION_KEY, 0);
      headers.put(KafkaConstants.KEY, "1");

      ExchangeData<String> req = new ExchangeData<>(body);
      req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_USER_CREATED_EVENT));

      this.getProducerTemplate().sendBodyAndHeaders(SEDA_USER_CREATED_EVENT, req, headers);
    }
  }
}
