package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
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

  public static Object[] buildAccount(){

    Object[] params = {
      new InParam(getUniqueInteger(), Types.BIGINT), //id_tx
      new InParam("type",Types.VARCHAR), //type
      new InParam("origin",Types.VARCHAR), //origin
      new InParam(500,Types.NUMERIC), //amount
      new InParam(600,Types.NUMERIC), //currency
      new InParam(600,Types.NUMERIC), //_ammount_usd
      new InParam(1,Types.NUMERIC), //_exchange_rate_dif
      new InParam(120,Types.NUMERIC), //_fee
      new InParam(0,Types.NUMERIC), //_fee_iva
      new InParam("2018-11-26",Types.VARCHAR), //_transaction_date
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
  public static Map<String, Object> insertAccount() throws SQLException {

    Object[] params = buildAccount();

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
    map.put("transaction_date", String.valueOf(params[9]));
    map.put("_error_code",resp.get("_error_code"));
    map.put("_error_msg",resp.get("_error_msg"));

    return map;
  }


  @Test
  public void insertAccountOk() throws SQLException {

    Map<String, Object> resp =  insertAccount();

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("id")) > 0);
  }


}
