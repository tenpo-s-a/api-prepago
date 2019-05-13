package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.json.JsonParser;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        ExchangeData<String> req = new ExchangeData<>(toJson(accountEvent));
        req.getProcessorMetadata().add(new ProcessorMetadata(0, SEDA_ACCOUNT_CREATED_EVENT));

        this.getProducerTemplate().sendBodyAndHeaders(SEDA_ACCOUNT_CREATED_EVENT, req, headers);
      } else {
        //headers.put(KafkaConstants.PARTITION_KEY, 0);
        //headers.put(KafkaConstants.KEY, Instant.now().toEpochMilli());
        this.getProducerTemplate().sendBodyAndHeaders(SEDA_ACCOUNT_CREATED_EVENT, toJson(accountEvent), headers);
      }
    }
  }

  public void publishCardEvent(CardEvent cardEvent, String endPoint) {
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

        ExchangeData<String> req = new ExchangeData<>(toJson(cardEvent));
        req.getProcessorMetadata().add(new ProcessorMetadata(0, endPoint));

        this.getProducerTemplate().sendBodyAndHeaders(endPoint, req, headers);
      } else {
        //headers.put(KafkaConstants.PARTITION_KEY, 0);
        //headers.put(KafkaConstants.KEY, Instant.now().toEpochMilli());

        this.getProducerTemplate().sendBodyAndHeaders(endPoint, toJson(cardEvent), headers);
      }
    }
  }

  /**
   * Envia un evento de transaccion
   *
   */
  public void publishTransactionEvent(TransactionEvent transactionEvent) {

    if(transactionEvent == null) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, transactionEvent -> null =======");
      throw new IllegalArgumentException();
    }

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      String route;
      log.info(String.format("TRANSACTION STATUS ++++ %s ++++", transactionEvent.getTransaction().getStatus()));
      if(transactionEvent.getTransaction().getStatus().equals("AUTHORIZED")){
        route = SEDA_TRANSACTION_AUTHORIZED_EVENT;
      }else if(transactionEvent.getTransaction().getStatus().equals("REJECTED")){
        route = SEDA_TRANSACTION_REJECTED_EVENT;
      } else if(transactionEvent.getTransaction().getStatus().equals("REVERSED")) {
        route = SEDA_TRANSACTION_REVERSED_EVENT;
      } else {
        route = SEDA_TRANSACTION_PAID_EVENT;
      }

      Map<String, Object> headers = new HashMap<>();
      if(!ConfigUtils.getInstance().getPropertyBoolean("kafka.enabled")) {
        headers.put("JMSCorrelationID", transactionEvent.getTransaction().getRemoteTransactionId());
        ExchangeData<String> req = new ExchangeData<>(toJson(transactionEvent));
        req.getProcessorMetadata().add(new ProcessorMetadata(0, route));

        this.getProducerTemplate().sendBodyAndHeaders(route, req, headers);
      } else {
        //headers.put(KafkaConstants.PARTITION_KEY, 0);
        //headers.put(KafkaConstants.KEY, Instant.now().toEpochMilli());

        this.getProducerTemplate().sendBodyAndHeaders(route, toJson(transactionEvent), headers);
      }
    }
  }

  /**
   * Envia un evento test
   *
   */
  public void publishTestEvent(String body) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {

      Map<String, Object> headers = new HashMap<>();
      log.info("[publishTestEvent] publicando evento de prueba");
      Map<String, Object> msg = new HashMap<>();
      msg.put("message", StringUtils.isAllBlank(body) ? "empty" : body);
      msg.put("date", LocalDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME));



      this.getProducerTemplate().sendBodyAndHeaders("direct:prepaid/test_kafka_endpoint", toJson(msg), headers);
    }
  }
}
