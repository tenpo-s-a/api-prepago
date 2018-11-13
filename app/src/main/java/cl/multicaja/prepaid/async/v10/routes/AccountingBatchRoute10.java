package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.processors.AccountingBatchProcessor10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AccountingBatchRoute10 extends BaseRoute10 {
  private static Log log = LogFactory.getLog(AccountingBatchRoute10.class);
  private final String SFTP_HOST_ENDPOINT;

  public AccountingBatchRoute10 (){
    super();
    SFTP_HOST_ENDPOINT = getSftpEndpoint();
  }

  @Override
  public void configure() throws Exception {
    if (ConfigUtils.isEnvTest()) {
      from(SFTP_HOST_ENDPOINT)
        .process(new AccountingBatchProcessor10(this).processAccountingBatch());
    }
  }

  private String getSftpEndpoint() {
    final String fileErrorConfig = "/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}";
    StringBuilder sb = new StringBuilder();
    sb.append("sftp://");
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.host"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.received.accountant.folder"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.auth.username"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.auth.password"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.move.accountant.done.folder"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.move.accountant.error.folder").concat(fileErrorConfig));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.reconnectDelay"));
    sb.append(getConfigUtils().getProperty("sftp.tecnocom.throwExceptionOnConnectFailed"));
    log.info(String.format("sftp endpoint -> [%s]", sb.toString()));
    return sb.toString();
  }
}
