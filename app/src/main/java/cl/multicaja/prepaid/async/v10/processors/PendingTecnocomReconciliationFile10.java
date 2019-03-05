package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

/**
 * @author abarazarte
 **/
public class PendingTecnocomReconciliationFile10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCurrencyModification10.class);

  public PendingTecnocomReconciliationFile10(BaseRoute10 route) {
    super(route);
  }

  public Processor processReconciliationFile() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info("Proccess file name : " + fileName);
        try {
          Long fileId = getRoute().getTecnocomReconciliationEJBBean10().processFile(inputStream, fileName);
          getRoute().getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);
        } catch(Exception e) {
          log.info("Error processing file: " + fileName);
          inputStream.close();
          throw e;
        }
      }
    };
  }
}
