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
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile;
import static cl.multicaja.test.db.Test_20181218164838_create_sp_mc_acc_search_ipm_file_v10.searchIpmFile;

public class Test_20181218164855_create_sp_mc_acc_update_ipm_file_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_acc_update_ipm_file_v10";

  @BeforeClass
  @AfterClass
  public static void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.ipm_file", SCHEMA_ACCOUNTING));
  }

  public static Map<String, Object> updateIpmFile(Long id, String fileId, Integer messageCount , String status) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      fileId != null ? fileId : new NullParam(Types.VARCHAR),
      messageCount != null ? messageCount : new NullParam(Types.NUMERIC),
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR) };
    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void shouldFail_id_null() throws SQLException {

    Map<String, Object> data = updateIpmFile(null, "FileId", Integer.MAX_VALUE, "Status");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC001",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
  }

  @Test
  public void shouldFail_status_null() throws SQLException {

    Map<String, Object> data = updateIpmFile(Long.MAX_VALUE, "FileId", Integer.MAX_VALUE, null);
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC002",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
  }

  @Test
  public void updateFile() throws SQLException {

    String fileName = "FileName";
    String fileId = "FileId";
    Integer messageCount = 1;
    String status = "Status";


    Map<String, Object> data = createIpmFile(fileName, fileId, messageCount, status);
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertNotNull("Debe tener ID", data.get("_r_id"));

    Long id = NumberUtils.getInstance().toLong(data.get("_r_id"));

    Assert.assertTrue("Debe tener ID", id > 0L);

    {
      Map<String, Object> resp = searchIpmFile(id, null, null, null);

      List result = (List)resp.get("result");

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1, result.size());

      Map<String, Object> file = (Map<String, Object>) result.get(0);

      Assert.assertEquals("debe tener el mismo id", id, file.get("_id"));
      Assert.assertEquals("debe tener el mismo fileName", fileName, file.get("_file_name"));
      Assert.assertEquals("debe tener el mismo fileId", fileId, file.get("_file_id"));
      Assert.assertEquals("debe tener el mismo messageCount", messageCount,  NumberUtils.getInstance().toInteger(file.get("_message_count")));
      Assert.assertEquals("debe tener el mismo status", status, file.get("_status"));
      Assert.assertNotNull("debe tener createdDate", file.get("_create_date"));
      Assert.assertNotNull("debe tener updatedDate", file.get("_update_date"));
    }

    String newStatus = "NewStatus";
    String newFileId = "NewFileId";
    Integer newMessageCount = 2;

    Map<String, Object> update = updateIpmFile(id, newFileId, newMessageCount, newStatus);
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","0", update.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","", update.get("_error_msg"));

    {
      Map<String, Object> resp = searchIpmFile(id, null, null, null);

      List result = (List)resp.get("result");

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1, result.size());

      Map<String, Object> file = (Map<String, Object>) result.get(0);

      Assert.assertEquals("debe tener el mismo id", id, file.get("_id"));
      Assert.assertEquals("debe tener el mismo fileName", fileName, file.get("_file_name"));
      Assert.assertEquals("debe tener el mismo fileId", newFileId, file.get("_file_id"));
      Assert.assertEquals("debe tener el mismo messageCount", newMessageCount,  NumberUtils.getInstance().toInteger(file.get("_message_count")));
      Assert.assertEquals("debe tener el mismo status", newStatus, file.get("_status"));
      Assert.assertNotNull("debe tener createdDate", file.get("_create_date"));
      Assert.assertNotNull("debe tener updatedDate", file.get("_update_date"));
    }

  }
}
