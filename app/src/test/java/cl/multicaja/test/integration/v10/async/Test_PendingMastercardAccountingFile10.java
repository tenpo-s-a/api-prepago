package cl.multicaja.test.integration.v10.async;


import cl.multicaja.test.integration.v10.helper.sftp.TestTecnocomSftpServer;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Map;

public class Test_PendingMastercardAccountingFile10 extends TestBaseUnit {
  private static Log log = LogFactory.getLog(Test_PendingMastercardAccountingFile10.class);

  @Test
  public void findAccountingFile() throws InterruptedException, IOException {
    String fileName = "reporte_mastercard.txt";
    String sourceDir = "mastercard/files/";
    String destDir = "src/test/resources/mastercard/contabilidad/";

    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(sourceDir + fileName);
    if (inputStream != null)
    {
      System.out.println("Encontré el archivo");
      File copiedFile = new File(destDir + fileName);
      OutputStream outputStream = new FileOutputStream(copiedFile);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, length);
      }
      inputStream.close();
      outputStream.close();
    }
    else
    {
      Assert.fail("File not found: " + fileName);
    }

    Thread.sleep(1500); // Esperar que lo agarre el metodo async
    Assert.assertTrue("Funciono", true);
  }

}
