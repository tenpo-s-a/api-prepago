package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20181009113559_create_sp_crea_movimiento_conciliado extends TestDbBasePg {

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", SCHEMA));
  }
  @Test
  public void testCreaMovimientoConciliado() throws SQLException {
    Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Map<String, Object> data = creaMovimientoConciliado(numberUtils.toLong(mov.get("_id")), "CARGA", "OK");
    Assert.assertNotNull("Data no debe ser null", data);
    Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
  }

  @Test
  public void testCreaMovimientoConciliadoError() throws SQLException {

    {//
      Map<String, Object> data = creaMovimientoConciliado(null, null, null);
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","El _id es obligatorio",data.get("_error_msg"));
    }

    {
      Map<String, Object> data = creaMovimientoConciliado(getUniqueLong(), null, null);
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertNotEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","El _accion es obligatorio",data.get("_error_msg"));
    }

    {
      Map<String, Object> data = creaMovimientoConciliado(getUniqueLong(), "CARGA", null);
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertNotEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","El _estado es obligatorio",data.get("_error_msg"));
    }
    {
      Map<String, Object> data = creaMovimientoConciliado(getUniqueLong(), "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertNotEquals("No debe ser 0","0",data.get("_error_code"));
    }
  }

  /**
   *
   * @param id_mov_ref
   * @param accion
   * @param estado
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> creaMovimientoConciliado(Long id_mov_ref, String accion, String estado) throws SQLException {
    Object[] params = {
      id_mov_ref != null ? id_mov_ref : new NullParam(Types.BIGINT),
      accion != null ? accion : new NullParam(Types.VARCHAR),
      estado != null ? estado : new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR) };
    return dbUtils.execute(SCHEMA + ".mc_prp_crea_movimiento_conciliado_v10", params);
  }
}
