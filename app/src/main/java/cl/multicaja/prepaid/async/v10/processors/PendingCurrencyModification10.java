package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.prepaid.helpers.MastercardFileHelper;
import cl.multicaja.prepaid.model.v10.CcrDetailRecord10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

public class PendingCurrencyModification10  {
  private static Log log = LogFactory.getLog(PendingCurrencyModification10.class);
  public Processor process() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        final InputStream is = exchange.getIn().getBody(InputStream.class);
        try {
          CcrDetailRecord10 ccrDetailRecord10 = MastercardFileHelper.getInstance().getValidCurrencyDetailRecordClp(is);
          /* ================================= */
          if(ccrDetailRecord10 != null) {
            log.info("Buy " + ccrDetailRecord10.getBuyCurrencyConversion());
            log.info("Mid " + ccrDetailRecord10.getMidCurrencyConversion());
            log.info("Sell " + ccrDetailRecord10.getSellCurrencyConversion());
          }
          /* ================================= */
        } catch (Exception ex) {
          log.info(ex.getMessage());
        }
      }
    };
  }

}
