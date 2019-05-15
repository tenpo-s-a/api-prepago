package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.processors.PendingCurrencyModification10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class CurrencyConvertionRoute10 extends BaseRoute10 {

  private static Log log = LogFactory.getLog(CurrencyConvertionRoute10.class);
  private final String SFTP_HOST_ENDPOINT;

  public CurrencyConvertionRoute10(){
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
    //  from(SFTP_HOST_ENDPOINT)
    //    .process(new PendingCurrencyModification10(this).processCurrencyConvertionFile());
    //}
  }

  private String getSftpEndpoint() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://");
    sb.append(getConfigUtils().getProperty("sftp.mastercard.host"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.recived.folder"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.auth.username"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.auth.password"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.move.done.folder"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.move.error.folder").concat(fileErrorConfig));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.reconnectDelay"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }
}
