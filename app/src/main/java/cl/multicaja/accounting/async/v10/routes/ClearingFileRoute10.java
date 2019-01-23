package cl.multicaja.accounting.async.v10.routes;

import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ClearingFileRoute10 extends BaseRoute10 {

  /** Camel URI, format is ftp://user@host/fileName?password=secret&passiveMode=true */

  public static final String DIRECT_CLEARING_UPLOAD_ENDPOINT = "direct:prepaid/upload_clearing_file";
  private Log log = LogFactory.getLog(ClearingFileRoute10.class);

  public ClearingFileRoute10() {
    super();
  }

  @Override
  public void configure() throws Exception {
    //Se ejecuta todos los dias a la 1am America/Santiago
    //from("quartz2://prepaid/clearing_file?cron=0+0+1+*+*+?&trigger.timeZone=America/Santiago")
    // .process(new AccountingScheduler10(this).generateClearingFile());

    //Ruta para la subida de archivos
    from(DIRECT_CLEARING_UPLOAD_ENDPOINT).id("prepaid/upload_clearing_file")
      .log("${date:now:yyyy-MM-dd'T'HH:mm:ssZ} - Uploading Clearing file")
      .pollEnrich("file:clearing_files?fileName=${header.CamelFileName}&noop=true", 10000/*investigate about timing according your file size*/, new AggregationStrategy() {
        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
          return newExchange;// save important data from old exchange
        }
      })
      .to(getFtpUri()) // upload the CSV file
      .log("${date:now:yyyy-MM-dd'T'HH:mm:ssZ} - FTP upload complete. File: ${header.CamelFileName}");

  }
  /**
   * Creates a Camel FTP URI
   */
  private String getFtpUri() {
    StringBuilder sb = new StringBuilder("sftp://");
    sb.append(getConfigUtils().getProperty("sftp.multicajared.host"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.clearing_file_upload.folder"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.auth.username"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.auth.password"));
    sb.append("&fileName=${header.CamelFileName}");
    return sb.toString();
  }
}
