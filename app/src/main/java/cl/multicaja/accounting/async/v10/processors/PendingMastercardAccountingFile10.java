package cl.multicaja.accounting.async.v10.processors;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;

public class PendingMastercardAccountingFile10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingMastercardAccountingFile10.class);

  private String privKeyFile = "./private_key.dat";
  private String passwd = "******";

  public PendingMastercardAccountingFile10(BaseRoute10 route) { super(route); }

  public Processor processAccountingBatch() {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info("Process Accounting Batch");
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);

        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info(String.format("processAccountingBatch: se encontro el archivo: %s", fileName));

        // Desencriptar usando la private key.
        // Todo: ¿Donde se almacenara esa clave?
        // TODO: revisar esto, quizas externalizar en metodo EJB para testearlo unitariamente
        //FileInputStream privKeyIn = new FileInputStream(privKeyFile);
        //File tempFile = new File("./" + fileName);
        //FileOutputStream tempOutputFile = new FileOutputStream(tempFile, false);
        //PgpHelper.getInstance().decryptFile(inputStream, tempOutputFile, privKeyIn, passwd.toCharArray());
        //privKeyIn.close();
        //tempOutputFile.close();

        IpmFile csvIpmFile = new IpmFile();

        List<IpmFile> processedFiles = getRoute().getPrepaidAccountingEJBBean10().findIpmFile(null, null, fileName, null, null);
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

        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        inputStream.close();

        File targetFile = new File("./" + fileName);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();

        // convertir ipm a csv
        try {
          getRoute().getPrepaidAccountingEJBBean10().convertIpmFileToCsv(fileName);
        } catch(Exception e) {
          log.error(e);
          targetFile.delete();
          throw e;
        }

        String csvFileName = fileName + ".csv";
        log.info(String.format("Processing file -> [%s]", csvFileName));
        File file = new File("./" + csvFileName);

        try {
          csvIpmFile = getRoute().getPrepaidAccountingEJBBean10().processIpmFile(null, file, csvIpmFile);
          getRoute().getPrepaidAccountingEJBBean10().processIpmFileTransactions(null, csvIpmFile);
        } catch (Exception e) {
          log.error(String.format("Error processing CSV file: [%s]", fileName), e);
          targetFile.delete();
          file.delete();
          throw e;
        }

        /*
        getRoute().getPrepaidAccountingEJBBean10().processMovementForAccounting(null, ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        getRoute().getPrepaidAccountingEJBBean10().generateAccountingFile(null, ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        */
      }
    };
  }
}
