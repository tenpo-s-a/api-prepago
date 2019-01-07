package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.MastercardIpmFileHelper;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;

public class Test_MastercardIpmFileHelper_readCsvIpmData extends TestBaseUnit {

  @Test
  public void readCsvIpmData_csv_null() {
    try {
      MastercardIpmFileHelper.readCsvIpmData(null, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Csv File is null]", "Csv File is null", e.getMessage());
    }

  }

  @Test
  public void readCsvIpmData_ipmFileObject_null() throws Exception {
    try {
      File file = new File("src/test/resources/mastercard/files/ipm/good.ipm.csv");
      FileReader fr = new FileReader(file);
      try{
        MastercardIpmFileHelper.readCsvIpmData(fr, null);
      } catch (Exception e) {
        fr.close();
        throw e;
      }
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [IpmFile object is null]", "IpmFile object is null", e.getMessage());
    }

  }

  @Test
  public void readCsvIpmData() throws Exception {
    File file = new File("src/test/resources/mastercard/files/ipm/good.ipm.csv");
    FileReader fr = new FileReader(file);

    IpmFile ipmFile = new IpmFile();

    try{
      ipmFile = MastercardIpmFileHelper.readCsvIpmData(fr, ipmFile);
      fr.close();
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

    Assert.assertNotNull("Debe tener header", ipmFile.getHeader());
    Assert.assertNotNull("Debe tener tailer", ipmFile.getTrailer());
    Assert.assertFalse("Debe tener transacciones", ipmFile.getTransactions().isEmpty());
    Assert.assertFalse("Debe tener otros mensajes", ipmFile.getTransactions().isEmpty());
    Assert.assertEquals("Debe tener 103 mensajes", Integer.valueOf(103), ipmFile.getMessageCount());
  }
}
