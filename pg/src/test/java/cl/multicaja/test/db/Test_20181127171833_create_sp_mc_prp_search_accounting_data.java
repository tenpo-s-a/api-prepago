package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Test_20181127171833_create_sp_mc_prp_search_accounting_data extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_prp_search_accounting_data_v10";

  @Before
  @After
  public void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.accounting cascade", SCHEMA_ACCOUNTING));
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
      new InParam(paramsIn.get("_id_mov_ref"),Types.BIGINT),
      new OutParam("id", Types.BIGINT),
      new OutParam("id_tx", Types.BIGINT),
      new OutParam("type", Types.VARCHAR),
      new OutParam("accounting_mov",Types.VARCHAR),
      new OutParam("origin", Types.VARCHAR),
      new OutParam("amount", Types.NUMERIC),
      new OutParam("currency", Types.NUMERIC),
      new OutParam("amount_usd", Types.NUMERIC),
      new OutParam("amount_mcar",Types.NUMERIC),
      new OutParam("exchange_rate_dif", Types.NUMERIC),
      new OutParam("fee", Types.NUMERIC),
      new OutParam("fee_iva", Types.NUMERIC),
      new OutParam("collector_fee",Types.NUMERIC),
      new OutParam("collector_fee_iva",Types.NUMERIC),
      new OutParam("amount_balance",Types.NUMERIC),
      new OutParam("status",Types.VARCHAR),
      new OutParam("file_id",Types.BIGINT),
      new OutParam("accounting_status",Types.VARCHAR),
      new OutParam("transaction_date", Types.TIMESTAMP),
      new OutParam("conciliation_date",Types.TIMESTAMP),
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

    map.put("id", numberUtils.toBigDecimal(resp.get("id")));
    map.put("id_tx", numberUtils.toBigDecimal(resp.get("id_tx")));
    map.put("type", String.valueOf(resp.get("type")));
    map.put("accounting_mov",String.valueOf(resp.get("accounting_mov")));
    map.put("origin", String.valueOf(resp.get("origin")));
    map.put("amount", numberUtils.toLong(resp.get("amount")));
    map.put("currency", numberUtils.toLong(resp.get("currency")));
    map.put("amount_usd", numberUtils.toLong(resp.get("amount_usd")));
    map.put("amount_mcar", numberUtils.toLong(resp.get("amount_mcar")));
    map.put("exchange_rate_dif", numberUtils.toLong(resp.get("exchange_rate_dif")));
    map.put("fee", numberUtils.toLong(resp.get("fee")));
    map.put("fee_iva", numberUtils.toLong(resp.get("fee_iva")));
    map.put("collector_fee", numberUtils.toLong(resp.get("collector_fee")));
    map.put("collector_fee_iva", numberUtils.toLong(resp.get("collector_fee_iva")));
    map.put("amount_balance",numberUtils.toLong(resp.get("amount_balance")));
    map.put("status", String.valueOf(resp.get("status")));
    map.put("file_id",numberUtils.toBigDecimal(resp.get("file_id")));
    map.put("transaction_date", numberUtils.toLong(resp.get("transaction_date")));
    map.put("conciliation_date", numberUtils.toLong(resp.get("conciliation_date")));
    map.put("create_date", numberUtils.toLong(resp.get("create_date")));
    map.put("update_date", numberUtils.toLong(resp.get("update_date")));

    return map;
  }

  public static Map<String, Object> queryByIdTx(Map<String, Object> paramsIn) throws SQLException {

    Map<String, Object> resp = buildQuery(paramsIn);

    Map<String, Object> map = new HashMap<>();

    map.put("id", numberUtils.toBigDecimal(resp.get("id")));
    map.put("id_tx", numberUtils.toBigDecimal(resp.get("id_tx")));
    map.put("type", String.valueOf(resp.get("type")));
    map.put("accounting_mov",String.valueOf(resp.get("accounting_mov")));
    map.put("origin", String.valueOf(resp.get("origin")));
    map.put("amount", numberUtils.toLong(resp.get("amount")));
    map.put("currency", numberUtils.toLong(resp.get("currency")));
    map.put("amount_usd", numberUtils.toLong(resp.get("amount_usd")));
    map.put("amount_mcar", numberUtils.toLong(resp.get("amount_mcar")));
    map.put("exchange_rate_dif", numberUtils.toLong(resp.get("exchange_rate_dif")));
    map.put("fee", numberUtils.toLong(resp.get("fee")));
    map.put("fee_iva", numberUtils.toLong(resp.get("fee_iva")));
    map.put("collector_fee", numberUtils.toLong(resp.get("collector_fee")));
    map.put("collector_fee_iva", numberUtils.toLong(resp.get("collector_fee_iva")));
    map.put("amount_balance",numberUtils.toLong(resp.get("amount_balance")));
    map.put("status", String.valueOf(resp.get("status")));
    map.put("file_id",numberUtils.toBigDecimal(resp.get("file_id")));
    map.put("transaction_date", numberUtils.toLong(resp.get("transaction_date")));
    map.put("conciliation_date", numberUtils.toLong(resp.get("conciliation_date")));
    map.put("create_date", numberUtils.toLong(resp.get("create_date")));
    map.put("update_date", numberUtils.toLong(resp.get("update_date")));

    return map;
  }

  @Test
  public void getAccountsByDateOk() throws SQLException {

    List<Map<String, Object>> testSuite = getTestSuiteOk();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, Object> dateToSearch = null;

    for (Map<String, Object> testCase : testSuite) {

      List<Map<String, Object>> acc_created = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccountOkByTestCase();

      for (Map<String, Object> acc : acc_created) {

        dateToSearch = new HashMap<>();

        Date transaction_date = null;
        try {
          transaction_date = inputFormat.parse(acc.get("transaction_date").toString());
        } catch (ParseException e) {
          e.printStackTrace();
        }
        String in_transaction_date = dateFormat.format(transaction_date);

        System.out.println(in_transaction_date);

        dateToSearch.put("_in_create_date", in_transaction_date);

        Map<String, Object> resp = queryByDate(dateToSearch);
        Assert.assertNotNull("Debe retornar respuesta", resp);
        Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);
      }
    }

  }

  @Test
  public void getAccountsByIdTx() throws SQLException {

    List<Map<String, Object>> testSuite = getTestSuiteOk();

    Map<String, Object> search = null;

    for (Map<String, Object> testCase : testSuite) {

      List<Map<String, Object>> acc_created = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccountOkByTestCase();

      for (Map<String, Object> acc : acc_created) {

        search = new HashMap<>();

        String id_mov_ref = String.valueOf(acc.get("id_tx"));

        search.put("_id_mov_ref", id_mov_ref);

        Map<String, Object> resp = queryByIdTx(search);
        Assert.assertNotNull("Debe retornar respuesta", resp);
        Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);
      }
    }

  }


}
