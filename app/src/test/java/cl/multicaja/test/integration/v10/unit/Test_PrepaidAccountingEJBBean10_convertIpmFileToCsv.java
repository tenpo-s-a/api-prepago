package cl.multicaja.test.integration.v10.unit;

import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidAccountingEJBBean10_convertIpmFileToCsv extends TestBaseUnit {

  @Test
  public void convertIpmFileToCsv_fileName_null() {
    try {
      getPrepaidAccountingEJBBean10().convertIpmFileToCsv(null);
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Ipm file name is null or empty]", "Ipm file name is null or empty", e.getMessage());
    }
  }

  @Test
  public void convertIpmFileToCsv_fileName_empty() {
    try {
      getPrepaidAccountingEJBBean10().convertIpmFileToCsv("");
    } catch (Exception e) {
      Assert.assertEquals("Debe ser error [Ipm file name is null or empty]", "Ipm file name is null or empty", e.getMessage());
    }
  }

}
