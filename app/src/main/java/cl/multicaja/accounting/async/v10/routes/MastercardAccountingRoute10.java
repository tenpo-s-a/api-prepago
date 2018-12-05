package cl.multicaja.accounting.async.v10.routes;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.accounting.async.v10.processors.PendingMastercardAccountingFile10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MastercardAccountingRoute10 extends BaseRoute10 {
  private static Log log = LogFactory.getLog(MastercardAccountingRoute10.class);
  private final String SFTP_HOST_ENDPOINT;

  public MastercardAccountingRoute10(){
    super();
    SFTP_HOST_ENDPOINT = getSftpEndpoint();
  }

  @Override
  public void configure() throws Exception {
    if (ConfigUtils.isEnvTest()) {
      from(SFTP_HOST_ENDPOINT)
        .process(new PendingMastercardAccountingFile10(this).processAccountingBatch());
    }
  }

  private String getSftpEndpoint() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://");
    sb.append(getConfigUtils().getProperty("sftp.mastercard.host"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.accounting.received.folder"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.auth.username"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.auth.password"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.accounting.move.done.folder"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.accounting.move.error.folder").concat(fileErrorConfig));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.reconnectDelay"));
    sb.append(getConfigUtils().getProperty("sftp.mastercard.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }
}
