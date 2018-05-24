package cl.multicaja.test.db;

import static cl.multicaja.test.db.Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertaMovimiento;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20180523111741_create_sp_mc_prp_actualiza_movimiento_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_movimiento_v10";
  private static final String TABLE_NAME = SCHEMA + ".prp_movimiento";


  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }
  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }

  @Test
  public void actualizaMovimientoOk() throws SQLException {

    Map<String,Object> mapMovimiento = insertaMovimiento();
    Object[] params = {
      mapMovimiento.get("_id"), //id
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      "PROCE",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);
    System.out.println(resp);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));
  }

  @Test
  public void actualizaMovimientoErrorId()throws SQLException
  {
    Object[] params = {
      new NullParam(Types.NUMERIC), //id
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      "PROCE",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }
  @Test
  public void actualizaMovimientoErrorEstado()throws SQLException {
    Map<String,Object> mapMovimiento = insertaMovimiento();
    Object[] params = {
      mapMovimiento.get("_id"), //id
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void actualizaMovimientoOkEstadoError()throws SQLException {
    Map<String,Object> mapMovimiento = insertaMovimiento();
    Object[] params = {
      mapMovimiento.get("_id"), //id
      new InParam(0,Types.NUMERIC),
      new InParam(0,Types.NUMERIC),
      new InParam(0,Types.NUMERIC),
      "ERRORENV",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
  }
  @Test
  public void actualizaMovimientoOkVariablesNull()throws SQLException {
    {// PRIMER PARAMETRO NULL
      Map<String, Object> mapMovimiento = insertaMovimiento();
      Object[] params = {
        mapMovimiento.get("_id"), //id
        new NullParam(Types.NUMERIC),
        new InParam(1, Types.NUMERIC),
        new InParam(1, Types.NUMERIC),
        "PROCE",
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };
      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    }
    {
      Map<String, Object> mapMovimiento = insertaMovimiento();
      Object[] params = {
        mapMovimiento.get("_id"), //id
        new InParam(1, Types.NUMERIC),
        new NullParam(Types.NUMERIC),
        new InParam(0, Types.NUMERIC),
        "PROCE",
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };
      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    }

    {
      Map<String, Object> mapMovimiento = insertaMovimiento();
      Object[] params = {
        mapMovimiento.get("_id"), //id
        new InParam(2, Types.NUMERIC),
        new InParam(3, Types.NUMERIC),
        new NullParam(Types.NUMERIC),
        "PROCE",
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };
      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    }

  }

}
