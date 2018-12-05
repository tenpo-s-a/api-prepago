package cl.multicaja.test.db_cdt;


import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class Test_20180504104111_create_sp_mc_cdt_carga_fases_movimientos_v10 extends TestDbBasePg {


  @Test
  public void spCargaFasesMovimiento() throws SQLException {

    Object[] params = {"Primera Carga" ,new NullParam(Types.NUMERIC)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CARGA_FASES_MOVIMIENTOS.getName(),params);
    List lstMovimientos = (List) outputData.get("result");
    System.out.println(" Se encontraron: "+lstMovimientos.size()+" movimientos");
    Assert.assertNotNull("Lista no nula",lstMovimientos);
    Assert.assertTrue("Lista != 0",lstMovimientos.size()>0);
  }
  @Test
  public void spCargaFasesMovimientoParamInNull() throws SQLException {

    Object[] params = {new NullParam(Types.VARCHAR),new NullParam(Types.NUMERIC)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CARGA_FASES_MOVIMIENTOS.getName(),params);

    List lstMovimientos = (List) outputData.get("result");
    System.out.println(" Se encontraron: "+lstMovimientos.size()+" movimientos");
    Assert.assertNotNull("Lista no nula",lstMovimientos);
    Assert.assertTrue("Lista != 0",lstMovimientos.size()>0);

  }
  @Test
  public void spCargaFasesMovimientoParamInVacio() throws SQLException {

    Object[] params = {"",0};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CARGA_FASES_MOVIMIENTOS.getName(),params);

    List lstMovimientos = (List) outputData.get("result");
    System.out.println(" Se encontraron: "+lstMovimientos.size()+" movimientos");
    Assert.assertNotNull("Lista no nula",lstMovimientos);
    Assert.assertTrue("Lista != 0",lstMovimientos.size()>0);
  }
}
