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

public class Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_acc_create_ipm_file_v10";

  @BeforeClass
  @AfterClass
  public static void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.ipm_file", SCHEMA_ACCOUNTING));
  }

  public static Map<String, Object> createIpmFile(String fileName, String fileId, Integer messageCount, String status) throws SQLException {
    Object[] params = {
      fileName != null ? fileName : new NullParam(Types.VARCHAR),
      fileId != null ? fileId : "",
      messageCount != null ? messageCount : 0,
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR) };
    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void createIpmFile() throws SQLException {

    Map<String, Object> data = createIpmFile("FileName", "FileId", 1, "Status");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertNotNull("Debe tener ID", data.get("_r_id"));
    Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
  }

  @Test
  public void shouldFail_duplicatedFile() throws SQLException {
    {
      Map<String, Object> data = createIpmFile("FileName2", "FileId2", 1, "Status");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
      Assert.assertNotNull("Debe tener ID", data.get("_r_id"));
      Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
    }
    {
      Map<String, Object> data = createIpmFile("FileName2", "FileId2", 1, "Status");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertNotEquals("No debe ser error","0",data.get("_error_code"));
      Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
      Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
    }
  }

  @Test
  public void shouldFail_fileName_null() throws SQLException {

    Map<String, Object> data = createIpmFile(null, "FileId3", 1, "Status");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC001",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }

  @Test
  public void shouldNotFail_fileId_null() throws SQLException {
    Map<String, Object> data = createIpmFile("FileName4", null, 1, "Status");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
  }

  @Test
  public void shouldNotFail_messageCount_null() throws SQLException {
    Map<String, Object> data = createIpmFile("FileName5", "FileId5", null, "Status");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
  }

  @Test
  public void shouldFail_status_null() throws SQLException {
    Map<String, Object> data = createIpmFile("FileName6", "FileId6", 1, null);
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC002",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }

}
