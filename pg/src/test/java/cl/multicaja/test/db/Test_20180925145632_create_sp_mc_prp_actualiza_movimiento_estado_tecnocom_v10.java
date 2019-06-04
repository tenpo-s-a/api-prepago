package cl.multicaja.test.db;

import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

//TODO: Revisar despues
@Ignore
public class Test_20180925145632_create_sp_mc_prp_actualiza_movimiento_estado_tecnocom_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_movimiento_estado_tecnocom_v10";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }
  /*
  public static Map<String,Object> updateTecnocomStatus(Long id, String status) throws SQLException {
    Object[] params = {
      id == null ? new NullParam(Types.NUMERIC) : id, //id
      status == null ? new NullParam(Types.VARCHAR) : status,
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void updateMovementsOk() throws SQLException {

    Map<String, Object> mapMovimiento = insertRandomMovement();

    Map<String,Object> resp = updateTecnocomStatus(numberUtils.toLong(mapMovimiento.get("_id")), "Conciliado");

    List lstMov = searchMovement(mapMovimiento.get("_id"));

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);

    Assert.assertEquals("El estado debe ser Conciliado", "Conciliado", fila.get("estado_con_tecnocom"));
  }

  @Test
  public void updateMovementsNotOkByIdNull()throws SQLException {

    Map<String,Object> resp = updateTecnocomStatus(null, "Conciliado");

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateMovementsNotOkByStatusNull()throws SQLException {

    Map<String, Object> mapMovimiento = insertRandomMovement();

    Map<String,Object> resp = updateTecnocomStatus(numberUtils.toLong(mapMovimiento.get("_id")), null);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  private List searchMovement(Object idMovimiento)  {
    return dbUtils.getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento WHERE ID = %s", SCHEMA, idMovimiento.toString()));
  }*/
}
