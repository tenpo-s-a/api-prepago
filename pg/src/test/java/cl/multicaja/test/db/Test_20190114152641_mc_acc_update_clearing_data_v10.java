package cl.multicaja.test.db;

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

public class Test_20190114152641_mc_acc_update_clearing_data_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_acc_update_clearing_data_v10";

  @BeforeClass
  @AfterClass
  public static void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", SCHEMA_ACCOUNTING));
  }

  public static Map<String, Object> updateClearingData(Long id, Long file_id, String status) throws SQLException {
    Object[] params =
      {
        id != null ? id : new NullParam(Types.BIGINT),
        file_id != null ? file_id : new NullParam(Types.BIGINT),
        status != null ? status : new NullParam(Types.VARCHAR),
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };
    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void updateClearingData() throws SQLException {
    // Create Clearing Data
    Map<String, Object> data = Test_20190114151738_mc_acc_create_clearing_data_v10.createClearingData(numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),"OK");
    // Update
    Map<String, Object> dataUpdate = updateClearingData(numberUtils.toLong(data.get("_r_id")),numberUtils.random(1L,9999L),null);
    Assert.assertNotNull("Data no debe ser null", dataUpdate);
    Assert.assertEquals("No debe ser error","0",dataUpdate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",dataUpdate.get("_error_msg"));

  }

  @Test
  public void shouldFail_Id_null() throws SQLException {
    Map<String, Object> data = updateClearingData(null,numberUtils.random(1L,9999L),"OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC001",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
  }

  @Test
  public void shouldFail_NotExist() throws SQLException {
    Map<String, Object> data = updateClearingData(numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),"OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","404",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
  }

}
