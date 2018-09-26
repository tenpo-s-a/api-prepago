package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

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
        try{
          ReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
          if(file != null) {
            insertOrUpdateManualTrx(file.getDetails()
              .stream()
              .filter(detail -> detail.isFromSat())
              .collect(Collectors.toList())
            );

            validateTransactions(file.getDetails()
              .stream()
              .filter(detail -> !detail.isFromSat())
              .collect(Collectors.toList())
            );
          } else {
            String msg = String.format("Error processing file [%s]", fileName);
            log.error(msg);
            throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
          }
        } catch (Exception ex){
          String msg = String.format("Error processing file [%s]", fileName);
          log.error(msg, ex);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }
      }
    };
  }

  /**
   * Insertar en la tabla prp_movimientos, las transcacciones realizadas de forma manual por SAT.
   * @param trxs
   */
  public void insertOrUpdateManualTrx(List<ReconciliationFileDetail> trxs) {
    for(int i = 0; i < trxs.size(); i++) {
      log.info(String.format("%d - %s", i, trxs.get(i).toString()));
    }
  }

  public void validateTransactions(List<ReconciliationFileDetail> trxs) {
    for(int i = 0; i < trxs.size(); i++) {
      log.info(String.format("%d - %s", i, trxs.get(i).toString()));
    }
  }


}
