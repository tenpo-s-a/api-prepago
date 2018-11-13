package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.AccountantFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.model.v10.ReconciliationStatusType;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

public class AccountingBatchProcessor10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(AccountingBatchProcessor10.class);

  public AccountingBatchProcessor10(BaseRoute10 route) {
    super(route);
  }

  public Processor processAccountingBatch() {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info("Process Accounting Batch");
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        log.error(exchange.getIn().getBody());
        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();

        try {
          AccountantFile file = TecnocomFileHelper.getInstance().validateAccountantFile(inputStream);

          // Todo: Do something with the extracted files

          // Todo: Write results into a file

          // Todo: Send file to email

        } catch (Exception ex) {
          String msg = String.format("Error processing file [%s]", fileName);
          log.error(msg, ex);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }
      }
    };
  }
}
