package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.BackofficeReport;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BackofficeReportRoute10 extends BaseRoute10 {

  public static final String DIRECT_E06_REPORT_UPLOAD_ENDPOINT = "direct:prepaid/upload_06_report_file";

  private Log log = LogFactory.getLog(BackofficeReportRoute10.class);

  public BackofficeReportRoute10() {
    super();
  }

  /**
   * Creates a Camel FTP URI
   */
  private String getFtpUri() {
    StringBuilder sb = new StringBuilder("sftp://");
    sb.append(getConfigUtils().getProperty("sftp.multicajared.host"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.e06_file.folder"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.auth.username"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.auth.password"));
    sb.append("&fileName=${header.CamelFileName}");
    return sb.toString();
  }

  @Override
  public void configure() throws Exception {
    //Se ejecuta todos los dias a la 1am America/Santiago
    //from("quartz2://prepaid/clearing_file?cron=0+0+1+2+*+?+*&trigger.timeZone=America/Santiago")
    // .process(new BackofficeReport(this).generateE06Report());

    //Ruta para la subida de archivos
    from(DIRECT_E06_REPORT_UPLOAD_ENDPOINT).id("prepaid/upload_06_report_file")
      .log("${date:now:yyyy-MM-dd'T'HH:mm:ssZ} - Uploading E06 Report file")
      .pollEnrich("file:report_e06?fileName=${header.CamelFileName}&noop=true", 10000/*investigate about timing according your file size*/, new AggregationStrategy() {
        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
          return newExchange;// save important data from old exchange
        }
      })
      .to(getFtpUri()) // upload the CSV file
      .log("${date:now:yyyy-MM-dd'T'HH:mm:ssZ} - FTP upload complete. File: ${header.CamelFileName}");
  }
}
