package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20180430165508_create_sp_mc_cdt_crea_limite_v10 extends TestDbBasePg {

  @Test
  public void spCreaLimiteSinReglaOk() throws SQLException {

    Object[] params = {2,-1,"Mto Debe ser Mayor a 3000",3000,"MAYORQIG",999,new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_LIMITE.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    Assert.assertTrue("Numero de error 0 creacion correcta", numError.equals("0"));
    Assert.assertTrue("Msj de error vacio creacion correcta", StringUtils.isBlank(msjError));

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

  }
  @Test
  public void spCreaLimiteErrorIdMovimiento() throws SQLException {

    Object[] params = {0,-1,"Mto Debe ser Mayor a 3000",3000,"MAYORQIG",999,new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_LIMITE.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    Assert.assertTrue("Num Error = MC001", numError.equals("MC001"));
    Assert.assertFalse("El Msj Error no es Vacio ", StringUtils.isBlank(msjError));

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

  }
  @Test
  public void spCreaLimiteErrorIdReglaAcum() throws SQLException {

    Object[] params = {2,0,"Mto Debe ser Mayor a 3000",3000,"MAYORQIG",999,new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_LIMITE.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    Assert.assertFalse("Num Error != 0", numError.equals("0"));
    Assert.assertTrue("El Msj Error no es Vacio", msjError.equals("[mc_cdt_crea_limite] El Id Regla Acumulacion no puede ser 0"));

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

  }

  @Test
  public void spCreaLimiteErrorDescripcion() throws SQLException {

    Object[] params = {2,-1,"",3000,"MAYORQIG",999,new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_LIMITE.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    Assert.assertFalse("Num Error != 0", numError.equals("0"));
    Assert.assertTrue("El Msj Error no es Vacio", msjError.equals("[mc_cdt_crea_limite] La descripcion no puede estar vacia"));

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

  }
  @Test
  public void spCreaLimiteErrorCodOperacion() throws SQLException {

    Object[] params = {2,-1,"Mto Debe ser Mayor a 3000",3000,"",999,new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_LIMITE.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    Assert.assertFalse("Num Error != 0", numError.equals("0"));
    Assert.assertTrue("El Msj Error no es Vacio", msjError.equals("[mc_cdt_crea_limite] El Codigo de operacion no puede estar vacio"));

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

  }
  @Test
  public void spCreaLimiteErrorCodError() throws SQLException {

    Object[] params = {2,-1,"Mto Debe ser Mayor a 3000",3000,"",0,new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_LIMITE.getName(),params);

    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    Assert.assertFalse("Num Error != 0", numError.equals("0"));
    Assert.assertTrue("El Msj Error no es Vacio", msjError.equals("[mc_cdt_crea_limite] El Codigo de operacion no puede estar vacio"));

    System.out.println(" NumError: "+numError +" MsjError: "+msjError);

  }



}
