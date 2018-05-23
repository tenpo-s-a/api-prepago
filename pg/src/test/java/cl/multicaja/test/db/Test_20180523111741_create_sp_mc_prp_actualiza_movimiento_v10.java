package cl.multicaja.test.db;

import static cl.multicaja.test.db.Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertaMovimiento;

import cl.multicaja.core.utils.db.NullParam;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20180523111741_create_sp_mc_prp_actualiza_movimiento_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_movimiento_v10";

  @Test
  public void actualizaMovimientoOk() throws SQLException {

    Map<String,Object> mapMovimiento = insertaMovimiento();
    Map<String,Object> resp = dbUtils.execute(SP_NAME,mapMovimiento.get("_id"),"PROCE");
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void actualizaMovimientoErrorId()throws SQLException
  {
    Map<String,Object> resp = dbUtils.execute(SP_NAME,new NullParam(Types.NUMERIC),"PROCE");
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }
  @Test
  public void actualizaMovimientoErrorEstado()throws SQLException {
    Map<String,Object> resp = dbUtils.execute(SP_NAME,1,new NullParam(Types.VARCHAR));
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }



}
