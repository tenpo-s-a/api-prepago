package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20180430170307_create_sp_mc_cdt_crea_regla_acumulacion_v10 extends TestDbBasePg {

  @Test
  public void spCreaReglaAcumulacion() throws SQLException {

    Object[] params = {"SEM","SUM" ,"REGLA ACUM TEST 1",new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_REGLA_ACUMULACION.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

    Assert.assertTrue("Numero de error = 0 OK", numError.equals("0"));
    Assert.assertTrue("Sin Msje Error", StringUtils.isBlank(msjError));

  }

  @Test
  public void spCreaReglaAcumulacionErrorPeriocidad() throws SQLException {


    Object[] params = {"","SUM" ,"REGLA ACUM TEST 2",new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_REGLA_ACUMULACION.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);
    Assert.assertTrue("El mensaje de error debe ser = MC001", numError.equals("MC001"));
    Assert.assertFalse("Con Msje Error", StringUtils.isBlank(msjError));

  }
  @Test
  public void spCreaReglaAcumulacionErrorCodOperacion() throws SQLException {

    Object[] params = {"SEM","" ,"REGLA ACUM TEST 3",new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_REGLA_ACUMULACION.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);
    Assert.assertTrue("El mensaje de error debe ser = MC002", numError.equals("MC002"));
    Assert.assertFalse("Con Msje Error", StringUtils.isBlank(msjError));

  }

}
