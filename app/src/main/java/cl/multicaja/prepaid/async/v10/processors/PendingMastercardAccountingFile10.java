package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.encryption.PgpHelper;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.mastercard.MastercardFileHelper;
import cl.multicaja.prepaid.helpers.mastercard.model.AccountingFile;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.model.v10.MailTemplates;
import cl.multicaja.prepaid.model.v10.MimeType;
import com.sun.mail.iap.ByteArray;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

public class PendingMastercardAccountingFile10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingMastercardAccountingFile10.class);

  private String privKeyFile = "/tmp/secret.dat";
  private String passwd = "clavesecreta";

  public PendingMastercardAccountingFile10(BaseRoute10 route) {
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

        System.out.println("processAccountingBatch: encontre el archivo: " + fileName);

        /*
        // Lanzar un thread que desencripte el archivo y los meta a decryptedInputStream.
        // Este thread es para evitar hacer duplicado del archivo completo en memoria
        PipedInputStream decryptedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream(decryptedInputStream);
        new Thread(
          new Runnable() {
            public void run() {
              FileInputStream privKeyIn = null;
              try {
                privKeyIn = new FileInputStream(privKeyFile);
              } catch (FileNotFoundException e) {
                log.error("AccountingBatch Encryption Thread: Unable to find private key file: " + privKeyFile + ", exception msg: " + e.getMessage());
                e.printStackTrace();
              }
              try {
                PgpHelper.getInstance().decryptFile(inputStream, pipedOutputStream, privKeyIn, passwd.toCharArray());
              } catch (Exception e) {
                log.error("AccountingBatch Encryption Thread: error decrypting the accounting file, msg: " + e.getMessage());
              }
              try {
                privKeyIn.close();
              } catch (IOException e) {
                log.error("AccoutingBatch Encryption Thread: error closing private key file, msg: " + e.getMessage());
              }
            }
          }
        ).start();

        // Extraer los datos del archivo
        AccountingFile file = MastercardFileHelper.getInstance().validateAccountantFile(decryptedInputStream);

        if (file.hasError()) {
          // Enviar email de soporte avisando error en archivo
          EmailBody emailBody = new EmailBody();
          emailBody.setTemplateData(null);
          emailBody.setTemplate(MailTemplates.TEMPLATE_MAIL_ACCOUNTING_FILE_ERROR);
          emailBody.setAddress(ConfigUtils.getInstance().getProperty("accounting.email.support"));
          getRoute().getMailPrepaidEJBBean10().sendMailAsync(null, emailBody);
        } else {
          // Todo: Write results into database table
        }

        // Todo: Read extra data from database

        // Todo: Write all new results into a new file

        // Todo: Convert file to string
        String fileToSend = "archivo convertido a string";

        // Enviamos el archivo al mail de reportes diarios
        EmailBody emailBodyToSend = new EmailBody();
        String fileNameToSend = String.format("reporte_contable_%s.csv", LocalDateTime.now().atZone(ZoneId.of("America/Santiago")));
        emailBodyToSend.addAttached(fileToSend, MimeType.CSV.getValue(), fileNameToSend);
        emailBodyToSend.setTemplateData(null);
        emailBodyToSend.setTemplate(MailTemplates.TEMPLATE_MAIL_ACCOUNTING_FILE_OK);
        emailBodyToSend.setAddress(ConfigUtils.getInstance().getProperty("accounting.email.dailyreport"));
        getRoute().getMailPrepaidEJBBean10().sendMailAsync(null, emailBodyToSend);
        */
      }
    };
  }
}
