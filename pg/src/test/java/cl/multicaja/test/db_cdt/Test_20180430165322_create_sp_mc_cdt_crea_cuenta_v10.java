package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20180430165322_create_sp_mc_cdt_crea_cuenta_v10 extends TestDbBasePg {

  @Test
  public void spCreaCuentaOk() throws SQLException {

    String run = getRandomNumericString(10);
    Object[] params = {"PREPAGO_"+run,"Prepago RUT "+run,new OutParam("_id_cuenta",Types.NUMERIC),new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_CUENTA.getName(),params);

    BigDecimal idCuenta = (BigDecimal) outputData.get("_id_cuenta");
    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    System.out.println("NumError: "+numError);
    System.out.println("msjError: "+msjError);

    Assert.assertNotEquals("Numero de cuenta debe ser != 0",0, idCuenta);
    Assert.assertTrue("Numero de error 0 creacion correcta", numError.equals("0"));
    Assert.assertTrue("Msj de error vacio creacion correcta", StringUtils.isBlank(msjError));

    System.out.println("Id Cuenta: "+idCuenta+" NumError: "+numError +" MsjError: "+msjError);

  }

  @Test
  public void spCreaCuentaErrorIdCuentaExterno() throws SQLException {

    Object[] params = {"","",new OutParam("_id_cuenta",Types.NUMERIC),new OutParam("_numerror",Types.VARCHAR),new OutParam("_msjerror",Types.VARCHAR)};
    Map<String,Object>  outputData = dbUtils.execute(SCHEMA_CDT+Constants.Procedures.SP_CREA_CUENTA.getName(),params);

    BigDecimal idCuenta = (BigDecimal) outputData.get("_id_cuenta");
    String numError = (String) outputData.get("_numerror");
    String msjError = (String) outputData.get("_msjerror");

    Assert.assertTrue("NNumero de cuenta debe ser != 0", idCuenta == null || idCuenta.intValue() == 0 );
    Assert.assertTrue("Numero de error = MC001", numError.equals("MC001"));
    Assert.assertFalse("Msj  no vacio", StringUtils.isBlank(msjError));

    System.out.println("Id Cuenta: "+idCuenta+" NumError: "+numError +" MsjError: "+msjError);

  }


}
