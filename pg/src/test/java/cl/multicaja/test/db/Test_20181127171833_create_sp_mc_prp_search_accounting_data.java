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

public class Test_20181127171833_create_sp_mc_prp_search_accounting_data extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_prp_search_accounting_data_v10";

  @BeforeClass
  public static void beforeClass() {

  }

  @AfterClass
  public static void afterClass() {

  }

  public static List<Map<String, Object>> getTestSuiteOk(){

    List<Map<String, Object>> testSuite  = new ArrayList<Map<String, Object>>();
    Map<String, Object> testCase = null;

    Date date = new Date();
    String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

    testCase = new HashMap<>();
    testCase.put("_in_create_date", currentDate);
    testSuite.add(testCase);

    return testSuite;
  }

  public static Map<String, Object> buildQuery(Map<String, Object> paramsIn) throws SQLException {

    Object[] params = {
      new InParam(paramsIn.get("_in_create_date"),Types.VARCHAR),
      new OutParam("id", Types.BIGINT),
      new OutParam("id_tx", Types.BIGINT),
      new OutParam("type", Types.VARCHAR),
      new OutParam("origin", Types.VARCHAR),
      new OutParam("amount", Types.NUMERIC),
      new OutParam("currency", Types.NUMERIC),
      new OutParam("ammount_usd", Types.NUMERIC),
      new OutParam("exchange_rate_dif", Types.NUMERIC),
      new OutParam("fee", Types.NUMERIC),
      new OutParam("fee_iva", Types.NUMERIC),
      new OutParam("transaction_date", Types.TIMESTAMP),
      new OutParam("create_date", Types.TIMESTAMP),
      new OutParam("update_date", Types.TIMESTAMP)
    };

    return dbUtils.execute(SP_NAME, params);
  }

  /**
   * insert new account
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> queryByDate(Map<String, Object> paramsIn) throws SQLException {

    Map<String, Object> resp = buildQuery(paramsIn);

    Map<String, Object> map = new HashMap<>();

    map.put("id", numberUtils.toLong(resp.get("id")));
    map.put("id_tx", numberUtils.toLong(resp.get("id_tx")));
    map.put("type", String.valueOf(resp.get("type")));
    map.put("origin", String.valueOf(resp.get("origin")));
    map.put("amount", numberUtils.toLong(resp.get("amount")));
    map.put("currency", numberUtils.toLong(resp.get("currency")));
    map.put("ammount_usd", numberUtils.toLong(resp.get("ammount_usd")));
    map.put("exchange_rate_dif", numberUtils.toLong(resp.get("exchange_rate_dif")));
    map.put("fee", numberUtils.toLong(resp.get("fee")));
    map.put("fee_iva", numberUtils.toLong(resp.get("fee_iva")));
    map.put("transaction_date", numberUtils.toLong(resp.get("transaction_date")));
    map.put("create_date", numberUtils.toLong(resp.get("create_date")));
    map.put("update_date", numberUtils.toLong(resp.get("update_date")));

    return map;
  }

  @Test
  public void getAccountsByDateOk() throws SQLException {

    List<Map<String, Object>> testSuite = getTestSuiteOk();
    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");

    Map<String, Object> dateToSearch = null;

    for (Map<String, Object> testCase : testSuite) {

      List<Map<String, Object>> acc_created = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccountOkByTestCase();

      for (Map<String, Object> acc : acc_created) {

        dateToSearch = new HashMap<>();
        String transaction_date = dateFormat.format(acc.get("transaction_date"));
        dateToSearch.put("_in_create_date", transaction_date);

        Map<String, Object> resp = queryByDate(dateToSearch);
        Assert.assertNotNull("Debe retornar respuesta", resp);
        Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);
      }
    }

  }


}
