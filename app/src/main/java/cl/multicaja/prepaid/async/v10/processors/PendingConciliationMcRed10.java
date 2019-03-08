package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

public class PendingConciliationMcRed10 extends BaseProcessor10  {

  private static Log log = LogFactory.getLog(PendingConciliationMcRed10.class);

  public PendingConciliationMcRed10(BaseRoute10 route) {
    super(route);
  }

  public Processor processReconciliationsMcRed(){
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);

        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info("Proccess file name : " + fileName);
        try {
          // Metodo que procesa el archivo y lo inserta en la tabla de
          ReconciliationFile10 reconciliationFile10 = getRoute().getMcRedReconciliationEJBBean10().processFile(inputStream, fileName);
          // Metodo que procesa los movimientos de la tabla Switch
          if(reconciliationFile10 != null) {
            getRoute().getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
            // Se borran los movimientos de la tabla temporal una vez procesados
            getRoute().getMcRedReconciliationEJBBean10().deleteFileMovementsByFileId(null,reconciliationFile10.getId());
          }
        } catch(Exception ex) {
          inputStream.close();
          throw ex;
        }
      }
    };
  }


}
