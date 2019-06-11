package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.List;

public class PendingConciliationMcRed10 extends BaseProcessor10  {

  private static Log log = LogFactory.getLog(PendingConciliationMcRed10.class);

  public PendingConciliationMcRed10(BaseRoute10 route) {
    super(route);
  }

  public Processor processReconciliationsMcRed(){
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        // Extraer el nombre del archivo encontrado
        Long fileId = exchange.getMessage().getHeader("fileId", Long.class);
        log.info("[processSwitchReconciliationFile] Processing file: " + fileId);

        // Buscamos el archivo correspondiente
        List<ReconciliationFile10> reconciliationFile10List = getRoute().getReconciliationFilesEJBBean10().getReconciliationFile(null, fileId, null, null, null, null);
        if (reconciliationFile10List == null || reconciliationFile10List.size() == 0) {
          String msg = String.format("Error - No se encontró archivo conciliacion switch con Id [%s]", fileId);
          log.error(msg);
          throw new ValidationException(Errors.ERROR_PROCESSING_FILE.getValue(), msg);
        }

        ReconciliationFile10 reconciliationFile10 = reconciliationFile10List.get(0);
        // Metodo que concilia los movimientos de la tabla Switch
        getRoute().getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
      }
    };
  }
}
