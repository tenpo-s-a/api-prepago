package cl.multicaja.test.db;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Test_20181010114032_create_sp_busca_movimientos_conciliar extends TestDbBasePg{

  private static final String SP_NAME = SCHEMA + ".mc_prp_busca_movimientos_conciliar_v10";


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

    // Mov 4 no conciliado
    Map<String,Object> mov1 =Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    // Mov 4 no conciliado
    Map<String,Object> mov2 =Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Map<String,Object> resp = buscaMovimientosPorConciliar();
    List result = (List)resp.get(0); //"result");
    Assert.assertEquals("Debe tener 2 movimientos por conciliar",2,result.size());

  }

  public static Map<String, Object> buscaMovimientosPorConciliar() throws SQLException {
      return dbUtils.execute(SP_NAME);
  }

}
