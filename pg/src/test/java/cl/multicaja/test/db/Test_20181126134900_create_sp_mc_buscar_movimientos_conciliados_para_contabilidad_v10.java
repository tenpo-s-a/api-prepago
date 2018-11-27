package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import org.junit.*;

import java.sql.SQLException;
import java.sql.Types;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Test_20181126134900_create_sp_mc_buscar_movimientos_conciliados_para_contabilidad_v10 extends TestDbBasePg {

  private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @BeforeClass
  @AfterClass
  public static void beforeAndAfterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", SCHEMA));
  }

  @Before
  public void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", SCHEMA));
  }


  public static Map<String, Object> searchMovements(String date, String status) throws SQLException {
    Object[] params = {
      date != null ? date :  new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR)
    };

    return dbUtils.execute(SCHEMA + ".mc_buscar_movimientos_conciliados_para_contabilidad_v10", params);
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

    Map<String, Object> resp = searchMovements(utc.format(formatter), "OK");

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2, result.size());
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

    Map<String, Object> resp = searchMovements(hereAndNow.format(formatter), "OK");

    List result = (List)resp.get("result");

    Assert.assertNull("no debe retornar una lista", result);

  }

}
