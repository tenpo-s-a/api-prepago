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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Test_20190114155847_mc_acc_update_accounting_data_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_acc_update_accounting_data_v10";

  @BeforeClass
  @AfterClass
  public static void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", SCHEMA_ACCOUNTING));
  }

  public static Map<String, Object> updateAccountingData(Long id, Long file_id, String status) throws SQLException {
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
    Map<String, Object> testCase = null;
    testCase = new HashMap<>();
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    Date date = new Date();
    String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

    testCase.put("id", getUniqueInteger());
    testCase.put("id_tx", 12);
    testCase.put("type", "1");
    testCase.put("origin", "2");
    testCase.put("amount", 500);
    testCase.put("currency", 3);
    testCase.put("amount_usd", 23);
    testCase.put("exchange_rate_dif", 50);
    testCase.put("fee", 23);
    testCase.put("fee_iva", 10);
    testCase.put("transaction_date", currentDate);
    testCase.put("accounting_mov","Carga WEb");
    testCase.put("amount_mcar",numberUtils.random(1000,9999));
    testCase.put("conciliation_date", currentDate);
    testCase.put("collector_fee",numberUtils.random(1000,9999));
    testCase.put("collector_fee_iva",numberUtils.random(1000,9999));
    testCase.put("amount_balance",numberUtils.random(1000,9999));
    testCase.put("status","OK");
    testCase.put("file_id",0);

    Map<String, Object> data = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(testCase);

    // Update
    Map<String, Object> dataUpdate = updateAccountingData(numberUtils.toLong(data.get("id")),numberUtils.random(1L,9999L),null);
    Assert.assertNotNull("Data no debe ser null", dataUpdate);
    Assert.assertEquals("No debe ser error","0",dataUpdate.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",dataUpdate.get("_error_msg"));

  }

  @Test
  public void shouldFail_Id_null() throws SQLException {
    Map<String, Object> data = updateAccountingData(null,numberUtils.random(1L,9999L),"OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","MC001",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
  }

  @Test
  public void shouldFail_NotExist() throws SQLException {
    Map<String, Object> data = updateAccountingData(numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),"OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser error","404",data.get("_error_code"));
    Assert.assertNotEquals("Deben ser iguales","",data.get("_error_msg"));
  }


}
