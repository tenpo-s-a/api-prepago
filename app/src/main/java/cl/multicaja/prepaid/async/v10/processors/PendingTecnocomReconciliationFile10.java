package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.utils.PgpUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
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
        Long fileId = exchange.getMessage().getHeader("fileId", Long.class);
        log.info("[processReconciliationFile] Processing file: " + fileId);
        try{
          //Procesa la data guardada en la tabla
          getRoute().getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);
          // llamar a F3
          getRoute().getPrepaidMovementEJBBean10().clearingResolution();
        } catch(Exception e) {
          log.info("Error processing file: " + fileId);
          throw e;
        }
      }
    };
  }
}
