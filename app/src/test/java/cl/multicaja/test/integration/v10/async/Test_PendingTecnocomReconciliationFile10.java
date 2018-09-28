package cl.multicaja.test.integration.v10.async;

import cl.multicaja.test.integration.v10.helper.sftp.TestSftpServer;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.InputStream;
import java.util.Map;

/**
 * @author abarazarte
 **/
public class Test_PendingTecnocomReconciliationFile10 extends TestBaseUnitAsync {
  private static Log log = LogFactory.getLog(Test_PendingTecnocomReconciliationFile10.class);





  @BeforeClass
  public static void setup() {

  }

  @AfterClass
  public static void tearDown(){

  }

  @Before
  public void before() {

  }

  private void putSuccessFileIntoSftp(String filename) throws Exception {
    final Map<String, Object> context = TestSftpServer.getInstance().openChanel();
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
    ChannelSftp channelSftp = (ChannelSftp) context.get("channel");
    channelSftp.put(inputStream, TestSftpServer.getInstance().BASE_DIR + "tecnocom/" + filename);
    channelSftp.exit();
    ((Session) context.get("session")).disconnect();
    inputStream.close();
    log.info("Wait for camel process");
    Thread.sleep(3000);
  }
}
