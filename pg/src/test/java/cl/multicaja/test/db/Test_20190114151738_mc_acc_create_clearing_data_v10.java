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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Test_20190114151738_mc_acc_create_clearing_data_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_acc_create_clearing_data_v10";


  @BeforeClass
  @AfterClass
  public static void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", SCHEMA_ACCOUNTING));
  }

  public static Map<String, Object> createClearingData(Long accounting_id, Long user_account_id, Long file_id,String status) throws SQLException {
    Object[] params =
      {
        accounting_id != null ? accounting_id : new NullParam(Types.BIGINT),
        user_account_id != null ? user_account_id : new NullParam(Types.BIGINT),
        file_id != null ? file_id : new NullParam(Types.BIGINT),
        status != null ? status : new NullParam(Types.VARCHAR),
        new OutParam("_r_id", Types.BIGINT),
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };
    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void createAccountingFile() throws SQLException {
    // Crea Accounting para obtener el ID
    Map<String, Object> randomAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.createRandomAccounting();
    Map<String, Object> resp = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(randomAccounting);
    //Create Clearing Data

    Map<String, Object> data = createClearingData(numberUtils.toLong(resp.get("id")),numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),"OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertNotNull("Debe tener ID", data.get("_r_id"));
    Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
  }

  @Test
  public void shouldFail_duplicated() throws SQLException {
    // Crea Accounting para obtener el ID
    Map<String, Object> randomAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.createRandomAccounting();
    Map<String, Object> resp = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(randomAccounting);
    {
      Map<String, Object> data = createClearingData(numberUtils.toLong(resp.get("id")),1L,numberUtils.random(1L,9999L),"OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser error","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
      Assert.assertNotNull("Debe tener ID", data.get("_r_id"));
      Assert.assertTrue("Debe tener ID", NumberUtils.getInstance().toInteger(data.get("_r_id")) > 0);
    }
    {
      Map<String, Object> data = createClearingData(numberUtils.toLong(resp.get("id")),1L,numberUtils.random(1L,9999L),"OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertNotEquals("No debe ser error","0",data.get("_error_code"));
      Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
      Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
    }
  }

  @Test
  public void shouldFail_AccuntingId_null() throws SQLException {

    Map<String, Object> data = createClearingData(null,numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),"OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC001",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }

  @Test
  public void shouldNotFail_Status_null() throws SQLException {
    Map<String, Object> data = createClearingData(numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),null);
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC002",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
    Assert.assertEquals("No Debe tener ID", 0L, data.get("_r_id"));
  }

}
