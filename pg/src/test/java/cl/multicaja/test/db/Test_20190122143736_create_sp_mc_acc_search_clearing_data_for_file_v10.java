package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.text.DateFormatter;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cl.multicaja.test.db.Test_20190114151738_mc_acc_create_clearing_data_v10.createClearingData;

public class Test_20190122143736_create_sp_mc_acc_search_clearing_data_for_file_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".mc_acc_search_clearing_data_for_file_v10";

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
      new InParam(paramsIn.get("_in_to"), Types.VARCHAR),
      new InParam(paramsIn.get("_in_status"), Types.VARCHAR),
      new InParam(paramsIn.get("_in_file_id"), Types.VARCHAR),
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
      new OutParam("transaction_date", Types.TIMESTAMP),
      new OutParam("conciliation_date",Types.TIMESTAMP),
      new OutParam("created", Types.TIMESTAMP),
      new OutParam("updated", Types.TIMESTAMP),
      new OutParam("user_account_id", Types.BIGINT)
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
    map.put("create_date", numberUtils.toLong(resp.get("created")));
    map.put("update_date", numberUtils.toLong(resp.get("updated")));

    return map;
  }

  public static Map<String, Object> queryByFileId(Map<String, Object> paramsIn) throws SQLException {

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
    map.put("create_date", numberUtils.toLong(resp.get("created")));
    map.put("update_date", numberUtils.toLong(resp.get("updated")));

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

        Map<String, Object> data = createClearingData(numberUtils.toLong(acc.get("id")),numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),"OK");

        dateToSearch = new HashMap<>();

        Date transaction_date = null;
        try {
          transaction_date = inputFormat.parse(acc.get("transaction_date").toString());
        } catch (ParseException e) {
          e.printStackTrace();
        }
        String in_transaction_date = dateFormat.format(transaction_date);


        ZonedDateTime zd = ZonedDateTime.now();
        ZonedDateTime endDay = zd.withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

        ZonedDateTime endDayUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);

        LocalDateTime to = endDayUtc.toLocalDateTime();

        String format = "yyyy-MM-dd HH:mm:ss";

        String t = to.format(DateTimeFormatter.ofPattern(format));

        dateToSearch.put("_in_to", t);
        dateToSearch.put("_in_status", "OK");
        dateToSearch.put("_in_file_id","");

        Map<String, Object> resp = queryByDate(dateToSearch);
        Assert.assertNotNull("Debe retornar respuesta", resp);
        Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);
      }
    }

  }
  @Test
  public void getAccountsByFileIdOk() throws SQLException {

    List<Map<String, Object>> testSuite = getTestSuiteOk();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, Object> paramIn = null;
    Map<String, Object> clearinFile = Test_20190114145205_create_sp_mc_acc_create_accounting_file_v10.createAccountingFile(getRandomString(10),getRandomString(10),getRandomString(10),getRandomString(10),getRandomString(10),"OK");
    Long fileId = numberUtils.toLong(clearinFile.get("_r_id"));
    for (Map<String, Object> testCase : testSuite) {

      List<Map<String, Object>> acc_created = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccountOkByTestCase();

      for (Map<String, Object> acc : acc_created) {
        Map<String, Object> data = createClearingData(numberUtils.toLong(acc.get("id")),numberUtils.random(1L,9999L),fileId,"OK");
        Assert.assertNotNull("La respuesta no debe ser null",data);
        paramIn = new HashMap<>();
        paramIn.put("_in_to", "");
        paramIn.put("_in_status", "OK");
        paramIn.put("_in_file_id",fileId);
        Map<String, Object> resp = queryByFileId(paramIn);
        Assert.assertNotNull("Debe retornar respuesta", resp);
        Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);
      }
    }

  }
}
