package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20180430170002_create_sp_mc_cdt_crea_fase_movimiento_v10 extends TestDbBasePg {


  @Test
  public void spCreaFaseMovimientoOK() throws SQLException {

    Object[] params = {0,"Movimiento de Carga Test","Carga de Tarjeta Test" ,"N", new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_FASE_MOVIMIENTO.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");
    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

    Assert.assertTrue("Numero de error 0 creacion correcta", numError.equals("0"));
    Assert.assertTrue("Msj de error vacio creacion correcta", StringUtils.isBlank(msjError));

  }

  @Test
  public void spCreaFaseMovimientoErrorSinNombre() throws SQLException {

    Object[] params = {0,"","Carga de Tarjeta Test" , "N",new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_FASE_MOVIMIENTO.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

    Assert.assertTrue("Num Error = MC001", numError.equals("MC001"));
    Assert.assertFalse("Existe Msj Error", StringUtils.isBlank(msjError));

  }

  @Test
  public void spCreaFaseMovimientoErrorIndConfirmacion() throws SQLException {

    Object[] params = {0,"Movimiento de Carga Test","Carga de Tarjeta Test", "", new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_FASE_MOVIMIENTO.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");
    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

    Assert.assertTrue("Num Error = MC002", numError.equals("MC002"));
    Assert.assertFalse("Existe Msj Error", StringUtils.isBlank(msjError));

  }
}
