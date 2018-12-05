package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

public class Test_20181126083955_create_sp_mc_prp_insert_accounting_data extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_prp_insert_accounting_data_v10";

  @BeforeClass
  public static void beforeClass() {

  }

  @AfterClass
  public static void afterClass() {

  }


  public static List<Map<String, Object>> getTestSuiteOk(){

    List<Map<String, Object>> testSuite  = new ArrayList<>();
    Map<String, Object> testCase = null;

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());

    Date date = new Date();
    String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

    testCase = new HashMap<>();
    testCase.put("id", getUniqueInteger());
    testCase.put("id_tx", 12);
    testCase.put("type", "1");
    testCase.put("origin", "2");
    testCase.put("amount", 500);
    testCase.put("currency", 3);
    testCase.put("ammount_usd", 23);
    testCase.put("exchange_rate_dif", 50);
    testCase.put("fee", 23);
    testCase.put("fee_iva", 10);
    testCase.put("transaction_date", currentDate);
    testSuite.add(testCase);

    return testSuite;
  }


  public static List<Map<String, Object>> getTestSuiteErr(){

    List<Map<String, Object>> testSuite  = new ArrayList<>();
    Map<String, Object> testCase = null;

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());

    Date date = new Date();
    String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

    testCase = new HashMap<>();
    testCase.put("id", getUniqueInteger());
    testCase.put("id_tx", 0);
    testCase.put("type", "1");
    testCase.put("origin", "2");
    testCase.put("amount", 500);
    testCase.put("currency", 3);
    testCase.put("ammount_usd", 23);
    testCase.put("exchange_rate_dif", 50);
    testCase.put("fee", 23);
    testCase.put("fee_iva", 10);
    testCase.put("transaction_date", currentDate);
    testSuite.add(testCase);

    testCase = new HashMap<>();
    testCase.put("id", getUniqueInteger());
    testCase.put("id_tx", 0);
    testCase.put("type", "");
    testCase.put("origin", "2");
    testCase.put("amount", 0);
    testCase.put("currency", 3);
    testCase.put("ammount_usd", 23);
    testCase.put("exchange_rate_dif", 50);
    testCase.put("fee", 23);
    testCase.put("fee_iva", 10);
    testCase.put("transaction_date", currentDate);
    testSuite.add(testCase);


    testCase = new HashMap<>();
    testCase.put("id", getUniqueInteger());
    testCase.put("id_tx", 0);
    testCase.put("type", "");
    testCase.put("origin", "");
    testCase.put("amount", 0);
    testCase.put("currency", 3);
    testCase.put("ammount_usd", 23);
    testCase.put("exchange_rate_dif", 50);
    testCase.put("fee", 23);
    testCase.put("fee_iva", 10);
    testCase.put("transaction_date", currentDate);
    testSuite.add(testCase);

    testCase = new HashMap<>();
    testCase.put("id", getUniqueInteger());
    testCase.put("id_tx", 0);
    testCase.put("type", "");
    testCase.put("origin", "");
    testCase.put("amount", 0);
    testCase.put("currency", 3);
    testCase.put("ammount_usd", 23);
    testCase.put("exchange_rate_dif", 50);
    testCase.put("fee", 23);
    testCase.put("fee_iva", 10);
    testCase.put("transaction_date", null);
    testSuite.add(testCase);

    return testSuite;
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
    map.put("id_tx", numberUtils.toLong(paramsIn.get("id_tx")));
    map.put("type", String.valueOf(paramsIn.get("type")));
    map.put("origin", String.valueOf(paramsIn.get("origin")));
    map.put("amount", numberUtils.toLong(paramsIn.get("amount")));
    map.put("currency", numberUtils.toLong(paramsIn.get("currency")));
    map.put("ammount_usd", numberUtils.toLong(paramsIn.get("ammount_usd")));
    map.put("exchange_rate_dif", numberUtils.toLong(paramsIn.get("exchange_rate_dif")));
    map.put("fee", numberUtils.toLong(paramsIn.get("fee")));
    map.put("fee_iva", numberUtils.toLong(paramsIn.get("fee_iva")));
    map.put("transaction_date", paramsIn.get("transaction_date"));
    map.put("_error_code",resp.get("_error_code"));
    map.put("_error_msg",resp.get("_error_msg"));

    return map;
  }

  public static List<Map<String,Object>> insertAccountOkByTestCase() throws SQLException {

    List<Map<String, Object>> respList  = new ArrayList<Map<String, Object>>();

    List<Map<String, Object>> testSuite = getTestSuiteOk();
    for (Map<String, Object> testCase : testSuite) {
      Map<String, Object> resp = insertAccount(testCase);
      respList.add(resp);
    }

    return respList;

  }


  @Test
  public void insertAccountOk() throws SQLException {

    List<Map<String, Object>> testSuite = getTestSuiteOk();

    for (Map<String, Object> testCase : testSuite) {
      Map<String, Object> resp =  insertAccount(testCase);
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
      Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);
    }

  }


  @Test
  public void insertAccountNoOk() throws SQLException {

    List<Map<String, Object>> testSuite = getTestSuiteErr();

    for (Map<String, Object> testCase : testSuite) {

      Map<String, Object> resp = insertAccount(testCase);
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertNotEquals("Codigo de error debe ser !=0", "0", resp.get("_error_code"));
      Assert.assertTrue("debe retornar un id = 0", numberUtils.toLong(resp.get("id")) == 0);
    }

  }

}
