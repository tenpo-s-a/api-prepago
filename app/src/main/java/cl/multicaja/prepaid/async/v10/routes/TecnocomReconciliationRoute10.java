package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.processors.PendingTecnocomReconciliationFile10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author abarazarte
 **/
public class TecnocomReconciliationRoute10 extends BaseRoute10 {

  private static Log log = LogFactory.getLog(TecnocomReconciliationRoute10.class);
  private final String SFTP_HOST_ENDPOINT;

  public TecnocomReconciliationRoute10(){
    super();
    SFTP_HOST_ENDPOINT = getSftpEndpoint();
  }

  @Override
  public void configure() throws Exception {
    if(ConfigUtils.isEnvDevelopment()){
      from(SFTP_HOST_ENDPOINT)
        .process(new PendingTecnocomReconciliationFile10(this).processReconciliationFile());
    }
  }

  private String getSftpEndpoint() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://");
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.host"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.recived.folder"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.auth.username"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.auth.password"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.move.done.folder"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.move.error.folder").concat(fileErrorConfig));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.reconnectDelay"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }
}
