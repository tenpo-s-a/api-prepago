package cl.multicaja.test.v10.async;

import cl.multicaja.prepaid.async.v10.routes.CurrencyConvertionRoute10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;

public class Test_PendingCurrencyModification10 extends TestBaseUnitAsync {
  private static Log log = LogFactory.getLog(Test_PendingCurrencyModification10.class);

  @Test
  public void shouldUpdateCurrentUsdValue() throws Exception {
    SshServer server = SshServer.setUpDefaultServer();
    server.setPort(7001);
    server.setPasswordAuthenticator(new PasswordAuthenticator() {
      @Override
      public boolean authenticate(String username, String password, ServerSession serverSession) throws PasswordChangeRequiredException {
        return "test".equals(username) && "test".equals(password);
      }
    });

    server.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(new SftpSubsystemFactory()));
    server.setFileSystemFactory(new NativeFileSystemFactory(true));
    server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
    server.start();

    CurrencyConvertionRoute10 currencyConvertionRoute10 = new CurrencyConvertionRoute10();
    currencyConvertionRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
    camelFactory.getCamelContext().addRoutes(currencyConvertionRoute10);

    final String fileName = "TEST.AR.T058.OK";
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
    //String content = IOUtils.toString(in, UTF_8);

  }

}
