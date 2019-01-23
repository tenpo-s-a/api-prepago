package cl.multicaja.accounting.async.v10.routes;

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
    //from(SFTP_HOST_ENDPOINT)
    //  .process(new PendingMastercardAccountingFile10(this).processAccountingBatch());
  }

  private String getSftpEndpoint() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://")
      .append(getConfigUtils().getProperty("sftp.mastercard.host"))
      .append(getConfigUtils().getProperty("sftp.mastercard.accounting.received.folder"))
      .append(getConfigUtils().getProperty("sftp.mastercard.auth.username"))
      .append(getConfigUtils().getProperty("sftp.mastercard.auth.password"))
      .append(getConfigUtils().getProperty("sftp.mastercard.accounting.move.done.folder"))
      .append(getConfigUtils().getProperty("sftp.mastercard.accounting.move.error.folder").concat(fileErrorConfig))
      .append(getConfigUtils().getProperty("sftp.mastercard.reconnectDelay"))
      .append(getConfigUtils().getProperty("sftp.mastercard.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }
}
