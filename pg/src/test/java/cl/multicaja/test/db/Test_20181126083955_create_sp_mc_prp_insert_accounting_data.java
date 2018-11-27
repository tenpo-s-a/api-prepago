package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Test_20181126083955_create_sp_mc_prp_insert_accounting_data extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_prp_insert_accounting_data_v10";

  @BeforeClass
  public static void beforeClass() {

  }

  @AfterClass
  public static void afterClass() {

  }


  public static Map<String, Object> getTestCasesOk(){
    Map<String, Object> testCases = new HashMap<>();

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    Timestamp _transaction_date = new Timestamp(c.getTime().getTime());

    testCases.put("id", getUniqueInteger());
    testCases.put("id_tx", 2);
    testCases.put("type", "1");
    testCases.put("origin", "2");
    testCases.put("amount", 500);
    testCases.put("currency", 3);
    testCases.put("ammount_usd", 23);
    testCases.put("exchange_rate_dif", 50);
    testCases.put("fee", 23);
    testCases.put("fee_iva", 10);
    testCases.put("transaction_date", _transaction_date);

    return testCases;
  }

  public static Map<String, Object> getTestCasesErr(){
    Map<String, Object> testCases = new HashMap<>();

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    Timestamp _transaction_date = new Timestamp(c.getTime().getTime());

    testCases.put("id", getUniqueInteger());
    testCases.put("id_tx", 2);
    testCases.put("type", "");
    testCases.put("origin", "2");
    testCases.put("amount", 500);
    testCases.put("currency", 3);
    testCases.put("ammount_usd", 23);
    testCases.put("exchange_rate_dif", 50);
    testCases.put("fee", 23);
    testCases.put("fee_iva", 10);
    testCases.put("transaction_date", _transaction_date);

    return testCases;
  }


  public static Object[] buildAccount(Map<String, Object> paramsIn){


    Object[] params = {
      new InParam(paramsIn.get("id_tx"), Types.BIGINT), //id_tx
      new InParam(paramsIn.get("type"),Types.VARCHAR), //type
      new InParam(paramsIn.get("origin"),Types.VARCHAR), //origin
      new InParam(paramsIn.get("amount"),Types.NUMERIC), //amount
      new InParam(paramsIn.get("currency"),Types.NUMERIC), //currency
      new InParam(paramsIn.get("ammount_usd"),Types.NUMERIC), //_ammount_usd
      new InParam(paramsIn.get("exchange_rate_dif"),Types.NUMERIC), //_exchange_rate_dif
      new InParam(paramsIn.get("fee"),Types.NUMERIC), //_fee
      new InParam(paramsIn.get("fee_iva"),Types.NUMERIC), //_fee_iva
      new InParam(paramsIn.get("transaction_date"),Types.TIMESTAMP), //_transaction_date
      new OutParam("_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return params;
  }

  /**
   * insert new account
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> insertAccount(Map<String, Object> paramsIn) throws SQLException {

    Object[] params = buildAccount(paramsIn);

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Map<String, Object> map = new HashMap<>();

    map.put("id", numberUtils.toLong(resp.get("_id")));
    map.put("id_tx", numberUtils.toLong(params[0]));
    map.put("type", String.valueOf(params[1]));
    map.put("origin", String.valueOf(params[2]));
    map.put("amount", numberUtils.toLong(params[3]));
    map.put("currency", numberUtils.toLong(params[4]));
    map.put("ammount_usd", numberUtils.toLong(params[5]));
    map.put("exchange_rate_dif", numberUtils.toLong(params[6]));
    map.put("fee", numberUtils.toLong(params[7]));
    map.put("fee_iva", numberUtils.toLong(params[8]));
    map.put("transaction_date", numberUtils.toLong(params[9]));
    map.put("_error_code",resp.get("_error_code"));
    map.put("_error_msg",resp.get("_error_msg"));

    return map;
  }


  @Test
  public void insertAccountOk() throws SQLException {

    Map<String, Object> testCases = getTestCasesOk();

    Map<String, Object> resp =  insertAccount(testCases);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);

  }


  @Test
  public void insertAccountNoOk() throws SQLException {

    Map<String, Object> testCases = getTestCasesErr();

    Map<String, Object> resp =  insertAccount(testCases);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser !=0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id = 0", numberUtils.toLong(resp.get("id")) == 0);

  }

}
