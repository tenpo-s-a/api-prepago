package cl.multicaja.test.db;

import org.junit.*;
import cl.multicaja.test.TestDbBasePg;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

//TODO: Revisar despues
@Ignore
public class Test_20181010114032_create_sp_busca_movimientos_conciliar extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_busca_movimientos_conciliar_v11";

  @Before
  public void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @After
  public void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  /*
  @Test
  public void testBuscaMovimientosConciliar() throws SQLException {

    // Mov 1 ya conciliado
    Map<String,Object> mov =Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(((BigDecimal) mov.get("_id")).longValue(),"TEST","TEST");

    // Mov 2 ya conciliado
    mov =Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(((BigDecimal) mov.get("_id")).longValue(),"TEST","TEST");

    // Mov 3 ya conciliado
    mov =Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(((BigDecimal) mov.get("_id")).longValue(),"TEST","TEST");

    Map<String,Object> resp = buscaMovimientosPorConciliar();
    List result = (List)resp.get("result");
    Assert.assertNull("Debe tener 0 movimientos por conciliar", result);

    // Mov 4 no conciliado, listo para conciliar
    Map<String,Object> mov1 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    cambiarEstadosSwitchYTecnocom(((BigDecimal) mov1.get("_id")).longValue(), "OK", "OK");

    // Mov 5 no conciliado, pending
    Map<String,Object> mov2 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    cambiarEstadosSwitchYTecnocom(((BigDecimal) mov2.get("_id")).longValue(), "OK", "PENDING");

    // Mov 6 no conciliado, pending
    Map<String,Object> mov3 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    cambiarEstadosSwitchYTecnocom(((BigDecimal) mov3.get("_id")).longValue(), "PENDING", "OK");

    // Mov 7 no conciliado, pending
    Map<String,Object> mov4 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();

    // Mov 8 no conciliado, listo para conciliar
    Map<String,Object> mov5 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    cambiarEstadosSwitchYTecnocom(((BigDecimal) mov5.get("_id")).longValue(), "OK", "OK");

    resp = buscaMovimientosPorConciliar();
    result = (List)resp.get("result");
    Assert.assertEquals("Debe tener 2 movimientos por conciliar",2, result.size());
  }

  public static Map<String, Object> buscaMovimientosPorConciliar() throws SQLException {
      return dbUtils.execute(SP_NAME);
  }

  public void cambiarEstadosSwitchYTecnocom(Long idMovimiento, String estadoSwitch, String estadoTecnocom) {
    dbUtils.getJdbcTemplate().execute(String.format("UPDATE %s.prp_movimiento SET estado_con_switch = '" + estadoSwitch + "', estado_con_tecnocom = '" + estadoTecnocom + "' WHERE id = " + idMovimiento.toString(), SCHEMA));
  }*/

}
