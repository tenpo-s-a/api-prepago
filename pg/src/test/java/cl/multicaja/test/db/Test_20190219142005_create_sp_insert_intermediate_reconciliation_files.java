package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20190219142005_create_sp_insert_intermediate_reconciliation_files extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".prp_inserta_archivo_reconciliacion";
  private static final String TABLE_NAME = "prp_archivos_reconciliacion";

  /*
    IN _nombre_de_archivo VARCHAR,
    IN _proceso           VARCHAR,
    IN _tipo              VARCHAR,
    IN _status            VARCHAR,
    OUT _r_id             BIGINT,
    OUT _error_code       VARCHAR,
    OUT _error_msg        VARCHAR
  */

  //Opciones
  /*
   proceso: tecnocom, switch
   tipo: cargas, retiros, retiros_rechazados, cargas_rechazadas, tecnocom_file
   status: reading,ok
  */

  public static Map<String, Object> insertArchivoReconcialicionLog(String nombreArchivo, String proceso, String tipo, String status) throws SQLException {
    Object[] params = {
      nombreArchivo != null ? nombreArchivo : new NullParam(Types.VARCHAR),
      proceso != null ? proceso : new NullParam(Types.VARCHAR),
      tipo != null ? tipo : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return dbUtils.execute(SP_NAME, params);
  }


  @Test
  public void insertReconciliationFileLogOK () throws Exception{

    String nombreArchivo = "Archivo Prueba "+ getRandomNumericString(10);
    String proceso = "TECNOCOM";
    String tipo = "CARGAS";
    String status = "OK";

    Map<String, Object> data = insertArchivoReconcialicionLog(nombreArchivo,proceso,tipo,status);
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","0", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","", data.get("_error_msg"));

    String sql = "SELECT * FROM "+SCHEMA+"."+TABLE_NAME+" WHERE nombre_de_archivo = '"+nombreArchivo+"'";
    Map<String, Object> dataFound = dbUtils.getJdbcTemplate().queryForMap(sql);
    Assert.assertEquals("Id Encontrado igual a Id registrado?",data.get("_r_id"),dataFound.get("id"));
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?",nombreArchivo,dataFound.get("nombre_de_archivo"));
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?",proceso,dataFound.get("proceso"));
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?",tipo,dataFound.get("tipo"));
    Assert.assertEquals("Status igual a Status registrado?",status,dataFound.get("status"));

  }

  @Test
  public void insertReconciliationFileLogFail1 () throws Exception {

    Map<String, Object> data = insertArchivoReconcialicionLog(null,"TECNOCOM","CARGAS","OK");
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC001", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","El _nombre_de_archivo es obligatorio", data.get("_error_msg"));
  }

  @Test
  public void insertReconciliationFileLogFail2 () throws Exception{
    Map<String, Object> data = insertArchivoReconcialicionLog("Archivo Prueba",null,"CARGAS","OK");
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC002", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","El _proceso es obligatorio", data.get("_error_msg"));
  }

  @Test
  public void insertReconciliationFileLogFail3 () throws Exception{
    Map<String, Object> data = insertArchivoReconcialicionLog("Archivo Prueba","TECNOCOM",null,"OK");
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC003", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","El _tipo es obligatorio", data.get("_error_msg"));
  }

  @Test
  public void insertReconciliationFileLogFail4 () throws Exception{
    Map<String, Object> data = insertArchivoReconcialicionLog("Archivo Prueba","TECNOCOM","CARGAS",null);
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC004", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","El _status es obligatorio", data.get("_error_msg"));
  }


}
