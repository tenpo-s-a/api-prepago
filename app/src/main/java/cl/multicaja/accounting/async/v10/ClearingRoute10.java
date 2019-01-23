package cl.multicaja.accounting.async.v10;

import cl.multicaja.accounting.async.v10.processors.PendingClearingFile10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClearingRoute10 extends BaseRoute10 {

  private static Log log = LogFactory.getLog(ClearingRoute10.class);
  private final String SFTP_HOST_ENDPOINT;

  public ClearingRoute10(){
    super();
    SFTP_HOST_ENDPOINT = getSftpEndpoint();
  }

  @Override
  public void configure() throws Exception {
    from(SFTP_HOST_ENDPOINT).process(new PendingClearingFile10(this).processClearingBatch());
  }

  private String getSftpEndpoint() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://")
      .append(getConfigUtils().getProperty("sftp.clearing.host"))
      .append(getConfigUtils().getProperty("sftp.clearing.accounting.received.folder"))
      .append(getConfigUtils().getProperty("sftp.clearing.auth.username"))
      .append(getConfigUtils().getProperty("sftp.clearing.auth.password"))
      .append(getConfigUtils().getProperty("sftp.clearing.accounting.move.done.folder"))
      .append(getConfigUtils().getProperty("sftp.clearing.accounting.move.error.folder").concat(fileErrorConfig))
      .append(getConfigUtils().getProperty("sftp.clearing.reconnectDelay"))
      .append(getConfigUtils().getProperty("sftp.clearing.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }
}
