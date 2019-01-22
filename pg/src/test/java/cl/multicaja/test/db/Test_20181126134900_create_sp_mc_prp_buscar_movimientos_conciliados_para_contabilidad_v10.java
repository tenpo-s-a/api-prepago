package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Test_20181126134900_create_sp_mc_prp_buscar_movimientos_conciliados_para_contabilidad_v10 extends TestDbBasePg {

  private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @BeforeClass
  @AfterClass
  public static void beforeAndAfterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", SCHEMA));
  }

  @Before
  public void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", SCHEMA));
  }


  public static Map<String, Object> searchMovements(String date, String reconciliationStatus, String businessStatus, String accountingMovementStatus) throws SQLException {
    Object[] params = {
      date != null ? date :  new NullParam(Types.VARCHAR),
      reconciliationStatus != null ? reconciliationStatus : new NullParam(Types.VARCHAR),
      businessStatus != null ? businessStatus : new NullParam(Types.VARCHAR),
      accountingMovementStatus != null ? accountingMovementStatus : new NullParam(Types.VARCHAR)
    };

    return dbUtils.execute(SCHEMA + ".mc_prp_buscar_movimientos_conciliados_para_contabilidad_v10", params);
  }

  private Map<String, Object> insertIntoAccounting(Long idTrx, String type, String origin, BigDecimal amount, Integer currency,
                                    BigDecimal amountUsd, BigDecimal exchangeRateDiff, BigDecimal fee,
                                    BigDecimal feeIva, Timestamp trxDate) throws Exception {
    Object[] params = {
      new InParam(idTrx, Types.BIGINT), //id_tx
      new InParam(type, Types.VARCHAR), //type
      new InParam(type,Types.VARCHAR),
      new InParam(origin, Types.VARCHAR), //origin
      new InParam(amount, Types.NUMERIC), //amount
      new InParam(currency, Types.NUMERIC), //currency
      new InParam(amountUsd, Types.NUMERIC), //_ammount_usd
      new InParam(amountUsd, Types.NUMERIC), //_ammount_mcar
      new InParam(exchangeRateDiff, Types.NUMERIC), //_exchange_rate_dif
      new InParam(fee, Types.NUMERIC), //_fee
      new InParam(feeIva, Types.NUMERIC), //_fee_iva
      new InParam(fee, Types.NUMERIC), //_collector_fee
      new InParam(feeIva, Types.NUMERIC), //_collector_fee_iva
      new InParam(amount, Types.NUMERIC), //amount_balance
      new InParam(trxDate, Types.TIMESTAMP), //_transaction_date
      new InParam(trxDate, Types.TIMESTAMP), //conciliation date
      new InParam("OK",Types.VARCHAR),
      new InParam(0, Types.BIGINT),// File ID
      new OutParam("_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return dbUtils.execute(SCHEMA_ACCOUNTING + ".mc_prp_insert_accounting_data_v10", params);
  }


  @Test
  public void testBuscarMovimientosConciliadosParaContabilidad() throws Exception {
    //mov1
    {
      Map<String, Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0", "0", data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales", "", data.get("_error_msg"));
    }

    //mov 2
    {
      Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "NOT_OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    }

    //mov 3
    {
      Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    }

    Thread.sleep(1000);

    ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

    Map<String, Object> resp = searchMovements(utc.format(formatter), "OK", "OK", "IPM");

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2, result.size());
  }

  @Test
  public void testBuscarMovimientosConciliadosParaContabilidad_AlreadyProcessed() throws Exception {
    //mov1
    {
      Map<String, Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");

      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0", "0", data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales", "", data.get("_error_msg"));

      Map<String, Object> accounting = insertIntoAccounting(numberUtils.toLong(mov.get("_id")), "CARGA", "IPM", BigDecimal.ONE, 152, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, Timestamp.from(Instant.now()));
      Assert.assertNotNull("Debe retornar respuesta", accounting);
      Assert.assertEquals("Codigo de error debe ser 0", "0", accounting.get("_error_code"));
      Assert.assertTrue("debe retornar un id", numberUtils.toLong(accounting.get("_id")) > 0);
    }

    //mov 2
    {
      Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));

      Map<String, Object> accounting = insertIntoAccounting(numberUtils.toLong(mov.get("_id")), "CARGA", "IPM", BigDecimal.ONE, 152, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, Timestamp.from(Instant.now()));
      Assert.assertNotNull("Debe retornar respuesta", accounting);
      Assert.assertEquals("Codigo de error debe ser 0", "0", accounting.get("_error_code"));
      Assert.assertTrue("debe retornar un id", numberUtils.toLong(accounting.get("_id")) > 0);
    }

    //mov 3
    {
      Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));

      Map<String, Object> accounting = insertIntoAccounting(numberUtils.toLong(mov.get("_id")), "CARGA", "IPM", BigDecimal.ONE, 152, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, Timestamp.from(Instant.now()));
      Assert.assertNotNull("Debe retornar respuesta", accounting);
      Assert.assertEquals("Codigo de error debe ser 0", "0", accounting.get("_error_code"));
      Assert.assertTrue("debe retornar un id", numberUtils.toLong(accounting.get("_id")) > 0);
    }

    Thread.sleep(1000);

    ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

    Map<String, Object> resp = searchMovements(utc.format(formatter), "OK","OK", "IPM");

    List result = (List)resp.get("result");

    Assert.assertNull("No debe retornar una lista", result);
  }

  @Test
  public void testBuscarMovimientosConciliadosParaContabilidad_null() throws Exception {
    //mov1
    {
      Map<String, Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0", "0", data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales", "", data.get("_error_msg"));
    }

    //mov 2
    {
      Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "NOT_OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    }

    //mov 3
    {
      Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
      Map<String, Object> data = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    }

    Thread.sleep(1000);

    ZonedDateTime hereAndNow = Instant.now().atZone(ZoneId.of("America/Santiago"));

    Map<String, Object> resp = searchMovements(hereAndNow.format(formatter), "OK" ,"OK", "IPM");

    List result = (List)resp.get("result");

    Assert.assertNull("no debe retornar una lista", result);

  }

}
