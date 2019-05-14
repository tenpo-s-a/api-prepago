package cl.multicaja.accounting.async.v10.routes;

import cl.multicaja.accounting.async.v10.processors.PendingClearingFile10;
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

    //TODO: La escucha de la carpeta SFTP debe estar en el proyecto prepaid-batch-router.
    // En este caso se debe escuchar una cola Activemq para procesar dicho archivo.
    //Se agrega ruta para procesar respuesta de Banco
    //from(getFtpUriResponse()).process(new PendingClearingFile10(this).processClearingBatch());
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

  private String getFtpUriResponse() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://")
      .append(getConfigUtils().getProperty("sftp.multicajared.host"))
      .append(getConfigUtils().getProperty("sftp.multicajared.clearing.received.folder"))
      .append(getConfigUtils().getProperty("sftp.multicajared.auth.username"))
      .append(getConfigUtils().getProperty("sftp.multicajared.auth.password"))
      .append(getConfigUtils().getProperty("sftp.multicajared.clearing.move.done.folder"))
      .append(getConfigUtils().getProperty("sftp.multicajared.clearing.move.error.folder").concat(fileErrorConfig))
      .append(getConfigUtils().getProperty("sftp.multicajared.reconnectDelay"))
      .append(getConfigUtils().getProperty("sftp.multicajared.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }

}
