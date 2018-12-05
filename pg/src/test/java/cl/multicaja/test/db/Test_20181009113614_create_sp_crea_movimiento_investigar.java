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

public class Test_20181009113614_create_sp_crea_movimiento_investigar extends TestDbBasePg {
  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", SCHEMA));
  }


  @Test
  public void testCreaMovimientoInvestigar() throws SQLException {

    {//
      Map<String, Object> data = creaMovimientoInvestigar(null, null, null);
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","El _mov_ref es obligatorio",data.get("_error_msg"));
    }

    {
      Map<String, Object> data = creaMovimientoInvestigar(getRandomString(10), null, null);
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertNotEquals("No debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","El _origen es obligatorio",data.get("_error_msg"));
    }

    {
      Map<String, Object> data = creaMovimientoInvestigar(getRandomString(10), "SWITCH", "carga_20123123.csv");
      Assert.assertNotNull("Data no debe ser null", data);
      System.out.println(data.get("_error_msg"));
      Assert.assertEquals("No debe ser 0","0",data.get("_error_code"));
      System.out.println(data.get("_error_msg"));
    }
  }
  public static Map<String, Object> creaMovimientoInvestigar(String mov_ref, String origen, String nombreArchivo) throws SQLException {
    Object[] params = {
      mov_ref != null ? mov_ref : new NullParam(Types.VARCHAR),
      origen != null ? origen : new NullParam(Types.VARCHAR),
      nombreArchivo != null ? nombreArchivo : new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR) };
    return dbUtils.execute(SCHEMA + ".mc_prp_crea_movimiento_investigar_v10", params);
  }
}
