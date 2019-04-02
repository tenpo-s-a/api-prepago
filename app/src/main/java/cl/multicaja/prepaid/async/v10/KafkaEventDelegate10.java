package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.json.JsonParser;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.kafka.events.model.Account;
import cl.multicaja.prepaid.kafka.events.model.Card;
import cl.multicaja.prepaid.kafka.events.model.Timestamps;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10.*;

public final class KafkaEventDelegate10 {

  private static Log log = LogFactory.getLog(KafkaEventDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private JsonParser jsonParser;

  private ProducerTemplate producerTemplate;

  private JsonParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser = JsonUtils.getJsonParser();
    }

    return this.jsonParser;
  }

  private String toJson(Object obj) {
    return this.getJsonParser().toJson(obj);
  }

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
   * Envia un evento de cuenta creada
   *
   */
  //FIXME: debe recibir la informacion de la cuenta
  public void publishAccountCreatedEvent(AccountEvent accountEvent) {

    if(accountEvent == null) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, accountEvent -> null =======");
      throw new IllegalArgumentException();
    }

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      Map<String, Object> headers = new HashMap<>();
      if(!ConfigUtils.getInstance().getPropertyBoolean("kafka.enabled")) {
        headers.put("JMSCorrelationID", accountEvent.getAccount().getId());
      }
      headers.put(KafkaConstants.PARTITION_KEY, 0);
      headers.put(KafkaConstants.KEY, "1");

      ExchangeData<String> req = new ExchangeData<>(toJson(accountEvent));
      req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_ACCOUNT_CREATED_EVENT));

      this.getProducerTemplate().sendBodyAndHeaders(SEDA_ACCOUNT_CREATED_EVENT, req, headers);
    }
  }

  /**
   * Envia un evento de tarjeta creada
   *
   */
  public void publishCardCreatedEvent(CardEvent cardEvent) {

    if(cardEvent == null) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, prepaidCard -> cardEvent =======");
      throw new IllegalArgumentException();
    }

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      Map<String, Object> headers = new HashMap<>();
      if(!ConfigUtils.getInstance().getPropertyBoolean("kafka.enabled")) {
        headers.put("JMSCorrelationID", cardEvent.getCard().getId());
      }
      headers.put(KafkaConstants.PARTITION_KEY, 0);
      headers.put(KafkaConstants.KEY, "1");

      ExchangeData<String> req = new ExchangeData<>(toJson(cardEvent));
      req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_CARD_CREATED_EVENT));

      this.getProducerTemplate().sendBodyAndHeaders(SEDA_CARD_CREATED_EVENT, req, headers);
    }
  }

  public void publishCardClosedEvent(CardEvent cardEvent) {
    if(cardEvent == null) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, cardEvent -> null =======");
      throw new IllegalArgumentException();
    }

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      Map<String, Object> headers = new HashMap<>();
      if(!ConfigUtils.getInstance().getPropertyBoolean("kafka.enabled")) {
        headers.put("JMSCorrelationID", cardEvent.getCard().getId());
      }
      headers.put(KafkaConstants.PARTITION_KEY, 0);
      headers.put(KafkaConstants.KEY, "1");

      ExchangeData<String> req = new ExchangeData<>(toJson(cardEvent));
      req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_CARD_CLOSED_EVENT));

      this.getProducerTemplate().sendBodyAndHeaders(SEDA_CARD_CLOSED_EVENT, req, headers);
    }
  }
}
