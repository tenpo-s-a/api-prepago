package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.kafka.events.UserEvent;

public final class KafkaEventsRoute10 extends BaseRoute10 {

  public static final String SEDA_ACCOUNT_CREATED_EVENT = "seda:KafkaEventsRoute10.accountCreated";
  public static final String SEDA_CARD_CREATED_EVENT = "seda:KafkaEventsRoute10.cardCreated";
  public static final String SEDA_CARD_LOCKED_EVENT = "seda:KafkaEventsRoute10.cardLocked";
  public static final String SEDA_CARD_UNLOCKED_EVENT = "seda:KafkaEventsRoute10.cardUnlocked";
  public static final String SEDA_CARD_CLOSED_EVENT = "seda:KafkaEventsRoute10.cardClosed";
  public static final String SEDA_TRANSACTION_AUTHORIZED_EVENT = "seda:KafkaEventsRoute10.transactionAuthorized";
  public static final String SEDA_TRANSACTION_REVERSED_EVENT = "seda:KafkaEventsRoute10.transactionReversed";
  public static final String SEDA_TRANSACTION_REJECTED_EVENT = "seda:KafkaEventsRoute10.transactionRejected";
  public static final String SEDA_TRANSACTION_PAID_EVENT = "seda:KafkaEventsRoute10.transactionPaid";
  public static final String SEDA_INVOICE_ISSUED_EVENT = "seda:KafkaEventsRoute10.invoiceIssued";
  public static final String SEDA_INVOICE_REVERSED_EVENT = "seda:KafkaEventsRoute10.invoiceReversed";


  private static final String USER_CREATED_TOPIC = "USER_CREATED";
  private static final String USER_UPDATED_TOPIC = "USER_UPDATED";
  public static final String ACCOUNT_CREATED_TOPIC = "ACCOUNT_CREATED";
  public static final String CARD_CREATED_TOPIC = "CARD_CREATED";
  private static final String CARD_LOCKED_TOPIC = "CARD_LOCKED";
  private static final String CARD_UNLOCKED_TOPIC = "CARD_UNLOCKED";
  private static final String CARD_CLOSED_TOPIC = "CARD_CLOSED";
  public static final String TRANSACTION_AUTHORIZED_TOPIC = "TRANSACTION_AUTHORIZED";
  private static final String TRANSACTION_REVERSED_TOPIC = "TRANSACTION_REVERSED";
  public static final String TRANSACTION_REJECTED_TOPIC = "TRANSACTION_REJECTED";
  private static final String TRANSACTION_PAID_TOPIC = "TRANSACTION_PAID";
  private static final String TRANSACTION_INVOICE_ISSUED_TOPIC = "TRANSACTION_INVOICE_ISSUED";
  private static final String TRANSACTION_INVOICE_REVERSED_TOPIC = "TRANSACTION_INVOICE_REVERSED";

  @Override
  public void configure() throws Exception {
    int concurrentConsumers = 10;
    int sedaSize = 1000;


    if(getConfigUtils().getPropertyBoolean("kafka.enabled")) {

      // Eventos a consumir
      from(getTopicConsumerEndpoint(USER_CREATED_TOPIC)).process(new UserEvent(this).processUserCreatedEvent());
      from(getTopicConsumerEndpoint(USER_UPDATED_TOPIC)).process(new UserEvent(this).processUserUpdatedEvent());

      //Eventos a publicar
      // Contrato/cuenta
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_ACCOUNT_CREATED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(ACCOUNT_CREATED_TOPIC));

      // Tarjetas
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_CREATED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(CARD_CREATED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_LOCKED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(CARD_LOCKED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_UNLOCKED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(CARD_UNLOCKED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_CLOSED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(CARD_CLOSED_TOPIC));

      // Transacciones
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_AUTHORIZED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(TRANSACTION_AUTHORIZED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_REVERSED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(TRANSACTION_REVERSED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_REJECTED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(TRANSACTION_REJECTED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_PAID_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(TRANSACTION_PAID_TOPIC));

      // Boletas
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_INVOICE_ISSUED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(TRANSACTION_INVOICE_ISSUED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_INVOICE_REVERSED_EVENT, concurrentConsumers, sedaSize))
        .to(getTopicProducerEndpoint(TRANSACTION_INVOICE_REVERSED_TOPIC));

    } else {
      // Si kafka no esta habilitado, se publica y consume desde colas en ActiveMQ

      // Eventos a consumir
      from(createJMSEndpoint(USER_CREATED_TOPIC))
        .process(new UserEvent(this).processUserCreatedEvent());
      from(createJMSEndpoint(USER_UPDATED_TOPIC))
        .process(new UserEvent(this).processUserUpdatedEvent());

      //Eventos a publicar
      // Contrato/cuenta
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_ACCOUNT_CREATED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(ACCOUNT_CREATED_TOPIC));

      // Tarjetas
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_CREATED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(CARD_CREATED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_LOCKED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(CARD_LOCKED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_UNLOCKED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(CARD_UNLOCKED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_CARD_CLOSED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(CARD_CLOSED_TOPIC));

      // Transacciones
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_AUTHORIZED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(TRANSACTION_AUTHORIZED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_REVERSED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(TRANSACTION_REVERSED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_REJECTED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(TRANSACTION_REJECTED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_TRANSACTION_PAID_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(TRANSACTION_PAID_TOPIC));

      // Boletas
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_INVOICE_ISSUED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(TRANSACTION_INVOICE_ISSUED_TOPIC));
      from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_INVOICE_REVERSED_EVENT, concurrentConsumers, sedaSize))
        .to(createJMSEndpoint(TRANSACTION_INVOICE_REVERSED_TOPIC));
    }
  }

  private String getTopicConsumerEndpoint(String topic) {

    String host = getConfigUtils().getProperty("kafka.host");

    StringBuilder sb = new StringBuilder();
    sb.append("kafka://")
      .append(host)
      .append("?topic=")
      .append(topic)
      .append("&brokers=")
      .append(host)
      .append("&autoOffsetReset=earliest&consumersCount=1");

    log.info(String.format("kafka consumer endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }

  private String getTopicProducerEndpoint(String topic) {

    String host = getConfigUtils().getProperty("kafka.host");

    StringBuilder sb = new StringBuilder();
    sb.append("kafka://")
      .append(host)
      .append("?topic=")
      .append(topic)
      .append("&brokers=")
      .append(host);

    log.info(String.format("kafka producer endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }
}
