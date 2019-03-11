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
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info("Proccess file name : " + fileName);
        try {

          File tempFile = new File("./" + fileName + "_decrypted");
          String privateKey = System.getenv("TECNOCOM_PGP_PRIVATE_KEY");
          String publicKey = System.getenv("TECNOCOM_PGP_PUBLIC_KEY");
          String passphrase = System.getenv("TECNOCOM_PGP_PASSPHRASE");

          if(StringUtils.isAllBlank(privateKey)) {
            String msg = "TECNOCOM_PGP_PRIVATE_KEY env variable not found";
            log.error(msg);
            tempFile.delete();
            throw new Exception(msg);
          }

          if(StringUtils.isAllBlank(publicKey)) {
            String msg = "TECNOCOM_PGP_PUBLIC_KEY env variable not found";
            log.error(msg);
            tempFile.delete();
            throw new Exception(msg);
          }

          if(StringUtils.isAllBlank(passphrase)) {
            String msg = "TECNOCOM_PGP_PASSPHRASE env variable not found";
            log.error(msg);
            tempFile.delete();
            throw new Exception(msg);
          }

          // Desencriptar Archivo
          InputStream decryptedData = PgpUtil.decryptFileToIs(inputStream, privateKey, publicKey, tempFile, passphrase);
          // Procesa el Archivo y lo mete en la tabla.
          Long fileId = getRoute().getTecnocomReconciliationEJBBean10().processFile(decryptedData, fileName);
          //Procesa la data guardada en la tabla
          getRoute().getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);
          // llamar a F3
          getRoute().getPrepaidMovementEJBBean10().clearingResolution();

        } catch(Exception e) {
          log.info("Error processing file: " + fileName);
          inputStream.close();
          throw e;
        }
      }
    };
  }
}
