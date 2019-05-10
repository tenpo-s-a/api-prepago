package cl.multicaja.prepaid.async.v10.routes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConciliationMcRedRoute10 extends BaseRoute10 {

  private static Log log = LogFactory.getLog(CurrencyConvertionRoute10.class);
  private final String SFTP_HOST_ENDPOINT;

  public ConciliationMcRedRoute10(){
    super();
    SFTP_HOST_ENDPOINT = getSftpEndpoint();
  }

  @Override
  public void configure() throws Exception {
    /**
     * Extrae valor dolar
     */
    //FIXME: La escucha de la carpeta SFTP debe estar en el proyecto prepaid-batch-router.
    // En este caso se debe escuchar una cola Activemq para procesar dicho archivo.

    //if(ConfigUtils.isEnvTest()) {
      //from(SFTP_HOST_ENDPOINT).process(new PendingConciliationMcRed10(this).processReconciliationsMcRed());
    //}
  }

  private String getSftpEndpoint() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://");
    sb.append(getConfigUtils().getProperty("sftp.multicajared.host"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.recived.folder"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.auth.username"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.auth.password"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.move.done.folder"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.move.error.folder").concat(fileErrorConfig));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.reconnectDelay"));
    sb.append(getConfigUtils().getProperty("sftp.multicajared.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }

}
