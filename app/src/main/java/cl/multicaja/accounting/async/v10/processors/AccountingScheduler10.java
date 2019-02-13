package cl.multicaja.accounting.async.v10.processors;

import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author abarazarte
 **/
public class AccountingScheduler10 extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(AccountingScheduler10.class);

  public AccountingScheduler10(BaseRoute10 route) {
    super(route);
  }

  public Processor generateAccountingFile() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info(String.format("Generating and sending accounting file", LocalDateTime.now()));
        //getRoute().getPrepaidAccountingEJBBean10().generateAccountingFile(null, ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
      }
    };
  }
}
