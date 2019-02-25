package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.time.ZonedDateTime;

public class BackofficeReport extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(BackofficeReport.class);

  public BackofficeReport(BaseRoute10 route) {
    super(route);
  }

  public Processor generateE06Report() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info(String.format("Generating and sending e06 report file", ZonedDateTime.now()));

        // Genera el reporte del mes anterior
        //File file = getRoute().getBackofficeEJBBEan10().generateE06Report(ZonedDateTime.now().minusMonths(1));

        // Upload file to SFTP
        //getRoute().getBackofficeEJBBEan10().uploadE06Report(file.getName());

        // Delete local file
        //file.delete();
      }
    };
  }
}
