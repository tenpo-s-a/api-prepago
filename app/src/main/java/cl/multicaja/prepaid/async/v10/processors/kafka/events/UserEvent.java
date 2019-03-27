package cl.multicaja.prepaid.async.v10.processors.kafka.events;

import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserEvent extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(UserEvent.class);

  public UserEvent(BaseRoute10 route) {
    super(route);
  }

  public Processor processUserCreatedEvent() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info("[processUserCreatedEvent] Processing USER_CREATED event");
        log.info(String.format("[processUserCreatedEvent] %s", exchange.getMessage().getBody()));
      }
    };
  }
}
