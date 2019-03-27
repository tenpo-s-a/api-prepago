package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.processors.kafka.events.UserEvent;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;

public final class KafkaEventsRoute10 extends BaseRoute10 {

  public static final String SEDA_USER_CREATED_EVENT = "seda:KafkaEventsRoute10.userCreated";

  private static final String USER_CREATED_TOPIC = "USER_CREATED";

  @Override
  public void configure() throws Exception {
    int concurrentConsumers = 10;
    int sedaSize = 1000;

    //from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_USER_CREATED_EVENT, concurrentConsumers, sedaSize))
    //  .to(getTopicProducerEndpoint(USER_CREATED_TOPIC));

    //from(getTopicConsumerEndpoint(USER_CREATED_TOPIC)).process(new UserEvent(this).processUserCreatedEvent());

    from(String.format("%s?concurrentConsumers=%s&size=%s", SEDA_USER_CREATED_EVENT, concurrentConsumers, sedaSize))
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          exchange.getIn().setBody(exchange.getIn().getBody() ,String.class);
          exchange.getIn().setHeader(KafkaConstants.PARTITION_KEY, 0);
          exchange.getIn().setHeader(KafkaConstants.KEY, "1");
        }
      })
      .to("kafka:kafka:9093?topic=test&brokers=kafka:9093");

    from("kafka:kafka:9093?topic=test&groupId=testing&autoOffsetReset=earliest&consumersCount=1&brokers=kafka:9093")
      .process(new Processor() {
        @Override
        public void process(Exchange exchange)
          throws Exception {
          String messageKey = "";
          if (exchange.getIn() != null) {
            Message message = exchange.getIn();
            Integer partitionId = (Integer) message
              .getHeader(KafkaConstants.PARTITION);
            String topicName = (String) message
              .getHeader(KafkaConstants.TOPIC);
            if (message.getHeader(KafkaConstants.KEY) != null)
              messageKey = (String) message
                .getHeader(KafkaConstants.KEY);
            Object data = message.getBody();


            System.out.println("topicName :: "
              + topicName + " partitionId :: "
              + partitionId + " messageKey :: "
              + messageKey + " message :: "
              + data + "\n");
          }
        }
      }).to("log:input");
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
