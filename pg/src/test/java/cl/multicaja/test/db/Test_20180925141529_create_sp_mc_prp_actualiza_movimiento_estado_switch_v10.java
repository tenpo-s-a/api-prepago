package cl.multicaja.test.db;

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

public class Test_20180925141529_create_sp_mc_prp_actualiza_movimiento_estado_switch_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_movimiento_estado_switch_v10";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }

  @Test
  public void updateMovementsOk() throws SQLException {

    Map<String, Object> mapMovimiento = insertRandomMovement();

    Object[] params = {
      mapMovimiento.get("_id"), //id
      "Conciliado",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    List lstMov = searchMovement(mapMovimiento.get("_id"));

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);

    Assert.assertEquals("El estado debe ser Conciliado", "Conciliado", fila.get("estado_con_switch"));
  }

  @Test
  public void updateMovementsNotOkByIdNull()throws SQLException {

    Object[] params = {
      new NullParam(Types.NUMERIC), //id
      "Conciliado",
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
      new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME,params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  private List searchMovement(Object idMovimiento)  {
    return dbUtils.getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento WHERE ID = %s", SCHEMA, idMovimiento.toString()));
  }
}

