package cl.multicaja.accounting.async.v10.processors;

import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PendingClearingFile10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingMastercardAccountingFile10.class);

  public PendingClearingFile10(BaseRoute10 route) { super(route); }

  public Processor processClearingBatch() {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info("Process Clearing Batch");
      }
    };
  }
}
