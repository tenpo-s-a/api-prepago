package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20180430164905_create_sp_mc_cdt_crea_bolsa_v10 extends TestDbBasePg {


  /*
    IN _nombre character varying,
    IN _descripcion character varying,
    OUT _numerror character varying,
    OUT _msjerror character varying)
   */
  @Test
  public void spCreaBolsaOK() throws SQLException {

      Object[] params = {getRandomString(10),getRandomString(20),new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
      Map<String,Object>  outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_BOLSA.getName(),params);
      String numError = (String) outputData.get("_numerror");
      String msjError = (String) outputData.get("_msjerror");
     System.out.println("NumError: "+numError +"MsjError"+msjError);

      Assert.assertTrue("Numero de error 0 creacion correcta",numError.equals("0"));
      Assert.assertTrue("Msj de error vacio creacion correcta",StringUtils.isBlank(msjError));
  }

  @Test
  public void spCreaBolsaErrorSp() throws SQLException {
    Object[] params = {""," ",new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object>  outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_BOLSA.getName(),params);
    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");
    Assert.assertTrue("Error en SP NumError = MC001",numError.equals("MC001"));
    Assert.assertFalse("Error en SP MsjError Not Blank",StringUtils.isBlank(msjError));
    System.out.println("NumError: "+numError +"MsjError"+msjError);
  }
}
