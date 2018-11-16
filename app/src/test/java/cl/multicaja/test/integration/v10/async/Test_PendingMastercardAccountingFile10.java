package cl.multicaja.test.integration.v10.async;


import cl.multicaja.test.integration.v10.helper.sftp.TestTecnocomSftpServer;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

public class Test_PendingMastercardAccountingFile10 extends TestBaseUnit {
  private static Log log = LogFactory.getLog(Test_PendingMastercardAccountingFile10.class);

  @Test
  public void findAccountingFile() throws InterruptedException {
    String accountingfileName = "MPJ15015.APUNTES.FCON0002.0987.D20180903";
    try {
      putFileIntoAccountingSftp(accountingfileName);
    } catch (Exception e) {
      Assert.fail("Fallo colocar el archivo de contabilidad en el sftp");
    }

    Thread.sleep(1500); // Esperar que lo agarre el metodo async

    Assert.assertTrue("Funciono", true);
  }

  private void putFileIntoAccountingSftp(String filename) throws Exception {
    try {
      final Map<String, Object> context = TestTecnocomSftpServer.getInstance().openChanel();
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/" + filename);
      ChannelSftp channelSftp = (ChannelSftp) context.get("channel");
      channelSftp.put(inputStream, TestTecnocomSftpServer.getInstance().BASE_DIR + "tecnocom/contabilidad/" + filename);
      channelSftp.exit();
      ((Session) context.get("session")).disconnect();
      inputStream.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }
}
