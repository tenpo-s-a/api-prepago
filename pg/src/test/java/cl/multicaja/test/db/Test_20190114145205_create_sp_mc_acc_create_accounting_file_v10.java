package cl.multicaja.test.db;

import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20190114145205_create_sp_mc_acc_create_accounting_file_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_acc_create_accounting_file_v10";


  @BeforeClass
  @AfterClass
  public static void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting_files", SCHEMA_ACCOUNTING));
  }

  public static Map<String, Object> createAccountingFile(String name, String fileId, String type, String format,String url, String status) throws SQLException {
    Object[] params =
     {
      name != null ? name : new NullParam(Types.VARCHAR),
      fileId != null ? fileId : new NullParam(Types.VARCHAR),
      type != null ? type : new NullParam(Types.VARCHAR),
      format != null ? format : new NullParam(Types.VARCHAR),
      url != null ? url : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void createAccountingFile() throws SQLException {

    Map<String, Object> data = createAccountingFile(getRandomString(10), getRandomString(10), getRandomString(5), "csv","www.sdasdasd.com/FileName.csv","OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertNotNull("Debe tener ID", data.get("_r_id"));
    Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
  }

  @Test
  public void shouldFail_duplicatedFile() throws SQLException {
    {
      Map<String, Object> data = createAccountingFile("FileName2", "FileId2", "Accounting", "csv","www.sdasdasd.com/FileName.csv","OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
      Assert.assertNotNull("Debe tener ID", data.get("_r_id"));
      Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
    }
    {
      Map<String, Object> data = createAccountingFile("FileName2", "FileId2", "Accounting", "csv","www.sdasdasd.com/FileName.csv","OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertNotEquals("No debe ser error","0",data.get("_error_code"));
      Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
      Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
    }
  }

  @Test
  public void shouldFail_name_null() throws SQLException {

    Map<String, Object> data = createAccountingFile(null, "FileId", "Accounting", "csv","www.sdasdasd.com/FileName.csv","OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC001",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }


  @Test
  public void shouldNotFail_type_null() throws SQLException {
    Map<String, Object> data = createAccountingFile("FileName", "FileId", null, "csv","www.sdasdasd.com/FileName.csv","OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC003",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }

  @Test
  public void shouldFail_status_null() throws SQLException {
    Map<String, Object> data = createAccountingFile("FileName", "FileId", "Accounting", "csv","www.sdasdasd.com/FileName.csv",null);
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC005",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }

  @Test
  public void shouldFail_format_null() throws SQLException {
    Map<String, Object> data = createAccountingFile("FileName", "FileId", "Accounting", null,"www.sdasdasd.com/FileName.csv","OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC004",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }
}
