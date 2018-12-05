package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement;

public class Test_20180523111741_create_sp_mc_prp_actualiza_movimiento_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_movimiento_v10";

  private final String pan = getRandomString(22);
  private final String centalta = getRandomNumericString(4);
  private final String cuenta = getRandomNumericString(12);

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @Test
  public void updateMovementsOk() throws SQLException {

    Map<String, Object> mapMovimiento = insertRandomMovement();

    Object[] params = {
      mapMovimiento.get("_id"), //id
      pan,
      centalta,
      cuenta,
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      "OK",
      "PROCE",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

    List lstMov = searchMovement(mapMovimiento.get("_id"));

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);

    Assert.assertEquals("El estado debe ser PROCE", "PROCE", fila.get("estado"));
    Assert.assertEquals("El estado de negocio debe ser OK", "OK", fila.get("estado_de_negocio"));
    Assert.assertNotNull("El pan debe estar lleno", fila.get("pan"));
    Assert.assertEquals("El pan debe estar lleno", pan, fila.get("pan"));
    Assert.assertNotNull("El centalta debe estar lleno", fila.get("centalta"));
    Assert.assertEquals("El centalta debe estar lleno", centalta, fila.get("centalta"));
    Assert.assertNotNull("El cuenta debe estar lleno", fila.get("cuenta"));
    Assert.assertEquals("El cuenta debe estar lleno", cuenta, fila.get("cuenta"));
  }

  @Test
  public void updateMovementsNotOkByIdNull()throws SQLException {

    Object[] params = {
      new NullParam(Types.NUMERIC), //id
      pan,
      centalta,
      cuenta,
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      "PROCE",
      "OK",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateMovementsNotOkByStatusNull()throws SQLException {

    Map<String, Object> mapMovimiento = insertRandomMovement();

    Object[] params = {
      mapMovimiento.get("_id"), //id
      pan,
      centalta,
      cuenta,
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      new InParam(1,Types.NUMERIC),
      "OK",
      new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateMovementsNotOkByNullParams()throws SQLException {

    {// PRIMER PARAMETRO NULL
      Map<String, Object> mapMovimiento = insertRandomMovement();
      Object[] params = {
        mapMovimiento.get("_id"), //id
        pan,
        centalta,
        cuenta,
        new NullParam(Types.NUMERIC),
        new InParam(1, Types.NUMERIC),
        new InParam(1, Types.NUMERIC),
        "OK",
        "PROCE",
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

      List lstMov = searchMovement(mapMovimiento.get("_id"));

      Assert.assertNotNull("La lista debe ser not null",lstMov);
      Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

      Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);
      Assert.assertEquals("El estado debe ser PROCE", "PROCE", fila.get("estado"));
    }

    {// SEGUNDO PARAMETRO NULL
      Map<String, Object> mapMovimiento = insertRandomMovement();
      Object[] params = {
        mapMovimiento.get("_id"), //id
        pan,
        centalta,
        cuenta,
        new InParam(1, Types.NUMERIC),
        new NullParam(Types.NUMERIC),
        new InParam(0, Types.NUMERIC),
        "OK",
        "PROCE",
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

      List lstMov = searchMovement(mapMovimiento.get("_id"));

      Assert.assertNotNull("La lista debe ser not null",lstMov);
      Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

      Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);
      Assert.assertEquals("El estado debe ser PROCE", "PROCE", fila.get("estado"));
    }

    {// TERCER PARAMETRO NULL
      Map<String, Object> mapMovimiento = insertRandomMovement();
      Object[] params = {
        mapMovimiento.get("_id"), //id
        pan,
        centalta,
        cuenta,
        new InParam(2, Types.NUMERIC),
        new InParam(3, Types.NUMERIC),
        new NullParam(Types.NUMERIC),
        "OK",
        "PROCE",
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

      List lstMov = searchMovement(mapMovimiento.get("_id"));

      Assert.assertNotNull("La lista debe ser not null",lstMov);
      Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

      Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);
      Assert.assertEquals("El estado debe ser PROCE", "PROCE", fila.get("estado"));
    }

    {// CUARTO PARAMETRO NULL
      Map<String, Object> mapMovimiento = insertRandomMovement();
      Object[] params = {
        mapMovimiento.get("_id"), //id
        pan,
        centalta,
        cuenta,
        new InParam(2, Types.NUMERIC),
        new InParam(3, Types.NUMERIC),
        new InParam(1, Types.NUMERIC),
        new NullParam(Types.VARCHAR),
        "PROCE",
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

      List lstMov = searchMovement(mapMovimiento.get("_id"));

      Assert.assertNotNull("La lista debe ser not null",lstMov);
      Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

      Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);
      Assert.assertEquals("El estado debe ser PROCE", "PROCE", fila.get("estado"));
    }
  }

  private List searchMovement(Object idMovimiento)  {
   return dbUtils.getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento WHERE ID = %s", SCHEMA, idMovimiento.toString()));
  }
}
