package cl.multicaja.test.integration.v10.async;

import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;


public class Test_PrepaidAccountingEJBBean10_sendMail extends TestBaseUnitAsync {

  @Test
  public void sendReportMail() throws Exception {
    try {
      String attachmentName = "src/test/resources/mastercard/files/attachment";
      FileWriter attachment = new FileWriter(attachmentName);
      attachment.write("Enviando prueba a email");
      attachment.close();
      getPrepaidAccountingEJBBean10().sendFile(attachmentName, getConfigUtils().getProperty("accounting.email.dailyreport"));
      new File(attachmentName).delete();
    } catch (Exception e) {
      Assert.fail("Error al enviar el correo");
      e.printStackTrace();
    }
  }

  @Test(expected = FileNotFoundException.class)
  public void sendReportEmail_ErrorNoReport() throws Exception {
    getPrepaidAccountingEJBBean10().sendFile("", getConfigUtils().getProperty("accounting.email.dailyreport"));
  }
}
