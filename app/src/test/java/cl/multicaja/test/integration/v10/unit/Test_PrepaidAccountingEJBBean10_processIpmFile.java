package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class Test_PrepaidAccountingEJBBean10_processIpmFile extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData() throws Exception {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", SCHEMA));
  }

  @Test
  public void processIpmFile_file_null() {
    try {
      getPrepaidAccountingEJBBean10().processIpmFile(null, null, null);
    } catch(Exception e) {
      Assert.assertEquals("Debe ser error [Csv file is null or does not exists]", "Csv file is null or does not exists", e.getMessage());
    }

  }

  @Test
  public void processIpmFile_file_doesNotExists() {
    try {
      File file = new File(getRandomString(10));

      getPrepaidAccountingEJBBean10().processIpmFile(null, file, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Csv file is null or does not exists]", "Csv file is null or does not exists", e.getMessage());
    }

  }

  @Test
  public void processIpmFile_ipmFile_null() {
    try {
      File file = new File("src/test/resources/mastercard/files/ipm/good.ipm.csv");
      getPrepaidAccountingEJBBean10().processIpmFile(null, file, null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [IpmFile object is null]", "IpmFile object is null", e.getMessage());
    }
  }

  @Test
  public void processIpmFile() {

    IpmFile ipmFile = new IpmFile();
    ipmFile.setFileName("test.ipm");

    try {
      File file = new File("src/test/resources/mastercard/files/ipm/good.ipm.csv");

      ipmFile = getPrepaidAccountingEJBBean10().processIpmFile(null, file, ipmFile);

      List<IpmFile> bdIpmFiles = getPrepaidAccountingEJBBean10().findIpmFile(null, null, ipmFile.getFileName(), null, null);

      Assert.assertEquals("Debe tener 1 archivo", Long.valueOf(1), Long.valueOf(bdIpmFiles.size()));

      IpmFile bdIpmFile = bdIpmFiles.get(0);

      Assert.assertEquals("Debe tener mismo fileName", ipmFile.getFileName(), bdIpmFile.getFileName());
      Assert.assertEquals("Debe tener status [PROCESSING]", IpmFileStatus.PROCESSING, bdIpmFile.getStatus());

    } catch (Exception e) {
      Assert.fail("No debe estar aca");
    }



  }
}
