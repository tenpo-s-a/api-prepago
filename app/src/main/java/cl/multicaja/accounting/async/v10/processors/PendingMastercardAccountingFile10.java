package cl.multicaja.accounting.async.v10.processors;

import cl.multicaja.accounting.helpers.mastercard.MastercardIpmFileHelper;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.utils.PgpUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class PendingMastercardAccountingFile10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingMastercardAccountingFile10.class);

  public PendingMastercardAccountingFile10(BaseRoute10 route) { super(route); }

  public Processor processAccountingBatch() {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info("Process Accounting Batch");
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);

        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info(String.format("processAccountingBatch: se encontro el archivo: %s", fileName));

        try {
          File tempFile = new File("./" + fileName + "_decrypted");

          String privateKey = System.getenv("MCRED_PGP_PRIVATE_KEY");
          String publicKey = System.getenv("MCRED_PGP_PUBLIC_KEY");
          String passphrase = System.getenv("MCRED_PGP_PASSPHRASE");

          if(StringUtils.isAllBlank(privateKey)) {
            String msg = "MCRED_PGP_PRIVATE_KEY env variable not found";
            log.error(msg);
            tempFile.delete();
            throw new Exception(msg);
          }

          if(StringUtils.isAllBlank(publicKey)) {
            String msg = "MCRED_PGP_PUBLIC_KEY env variable not found";
            log.error(msg);
            tempFile.delete();
            throw new Exception(msg);
          }

          if(StringUtils.isAllBlank(passphrase)) {
            String msg = "MCRED_PGP_PASSPHRASE env variable not found";
            log.error(msg);
            tempFile.delete();
            throw new Exception(msg);
          }

          PgpUtil.decryptFile(inputStream, privateKey, publicKey, tempFile, passphrase);

          IpmFile csvIpmFile = new IpmFile();

          List<IpmFile> processedFiles = getRoute().getPrepaidAccountingEJBBean10().findIpmFile(null, null, tempFile.getName(), null, null);
          if(!processedFiles.isEmpty()) {
            IpmFile f = processedFiles.get(0);
            if(!IpmFileStatus.ERROR.equals(f.getStatus())) {
              log.info(String.format("File [%s] already processed", fileName));
              return;
            } else {
              log.info(String.format("Reprocessing file [%s]", fileName));
              csvIpmFile = f;
            }
          } else {
            csvIpmFile.setFileName(fileName);
          }

          // convertir ipm a csv
          try {
            getRoute().getPrepaidAccountingEJBBean10().convertIpmFileToCsv("./" + tempFile.getName());
          } catch(Exception e) {
            log.error(e);
            throw e;
          }

          String csvFileName = "./" + tempFile.getName() + ".csv";
          log.info(String.format("Processing file -> [%s]", csvFileName));
          File file = new File(csvFileName);

          try {
            csvIpmFile = getRoute().getPrepaidAccountingEJBBean10().processIpmFile(null, file, csvIpmFile);
            getRoute().getPrepaidAccountingEJBBean10().processIpmFileTransactions(null, csvIpmFile);
            file.delete();
            tempFile.delete();
          } catch (Exception e) {
            log.error(String.format("Error processing CSV file: [%s]", csvFileName), e);
            file.delete();
            tempFile.delete();
            throw e;
          }

          getRoute().getPrepaidAccountingEJBBean10().processMovementForAccounting(null, ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());



        } catch (Exception e) {
          log.info(String.format("Error processing file: %s", fileName));
          log.error(e);
          throw e;
        }
      }
    };
  }
}
