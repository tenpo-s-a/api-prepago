package cl.multicaja.accounting.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
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
public class PendingStoreWithdrawToAccounting10 extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(AccountingScheduler10.class);

  public PendingStoreWithdrawToAccounting10(BaseRoute10 route) {
    super(route);
  }

  public Processor storeWithdrawToAccounting() throws Exception {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("Storing web withdraw in accouting and clearing tables");
        getRoute().getPrepaidAccountingEJBBean10().generateAccountingFile(null, ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());

        PrepaidTopupData10 data = req.getData();
        PrepaidWithdraw10 prepaidWithdraw = data.getPrepaidWithdraw10();


        return req;
      }
    };
  }
}
