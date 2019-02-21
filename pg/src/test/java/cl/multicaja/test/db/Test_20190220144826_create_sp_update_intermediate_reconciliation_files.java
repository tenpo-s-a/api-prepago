package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test_20190220144826_create_sp_update_intermediate_reconciliation_files extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".prp_actualiza_archivo_conciliacion";
  private static final String TABLE_NAME = "prp_archivos_conciliacion";

  /*Opciones
    status: reading,ok
   */

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_archivos_conciliacion",SCHEMA));
  }

  public Map<String, Object> searchArchivosReconciliacionLog(
    String nombreDeArchivo, String proceso, String tipo, String status) throws SQLException {

    Object[] params = {
      nombreDeArchivo != null ? nombreDeArchivo : new NullParam(Types.VARCHAR),
      proceso != null ? proceso : new NullParam(Types.VARCHAR),
      tipo != null ? tipo : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR)
    };

    return dbUtils.execute(SCHEMA + ".prp_busca_archivo_conciliacion", params);
  }

  /**
   *
   * @param nombreDeArchivo
   * @param proceso
   * @param tipo
   * @param status
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> insertArchivoReconcialicionLog(String nombreDeArchivo,String proceso, String tipo, String status) throws SQLException {

    Map<String, Object> data = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.
      insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);
    Assert.assertNotNull("Debe retornar respuesta", data);
    Assert.assertEquals("No debe ser 0", "0", data.get("_error_code"));
    Assert.assertEquals("Deben ser iguales", "", data.get("_error_msg"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(data.get("_r_id")) > 0);

    String sql = "SELECT * FROM "+SCHEMA+"."+TABLE_NAME+" WHERE nombre_de_archivo = '"+nombreDeArchivo+"'";
    Map<String, Object> dataFound = dbUtils.getJdbcTemplate().queryForMap(sql);

    Assert.assertEquals("Id Encontrado igual a Id registrado?",data.get("_r_id"),dataFound.get("id"));
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?",nombreDeArchivo,dataFound.get("nombre_de_archivo"));
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?",proceso,dataFound.get("proceso"));
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?",tipo,dataFound.get("tipo"));
    Assert.assertEquals("Status igual a Status registrado?",status,dataFound.get("status"));

    Map<String, Object> map = new HashMap<>();
    map.put("id", numberUtils.toLong(data.get("_r_id") ));
    map.put("nombre_de_archivo", nombreDeArchivo);
    map.put("proceso", proceso);
    map.put("tipo", tipo);
    map.put("status", status);
    map.put("created_at",dataFound.get("created_at"));
    map.put("updated_at",dataFound.get("updated_at"));

    return map;
  }


  public static Map<String,Object> updateArchivoReconcialicionLog(Long id, String status) throws Exception{

    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void testChangeStatusOK() throws Exception{
    String nombreDeArchivo;
    String proceso;
    String tipo;
    String status;
    String futureStatusChange;
    Long id;


    nombreDeArchivo = "Archivo Prueba "+getRandomNumericString(10);
    proceso = "TECNOCOM";
    tipo = "CARGAS";
    status = "OK";
    futureStatusChange = "READING";

    Map<String, Object> insertDataResponse = insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);
    id = (long)insertDataResponse.get("id");

    Map<String, Object> updateDataResponse = updateArchivoReconcialicionLog(id,futureStatusChange);
    System.out.println(String.format("Num Err: %s Msj: %s",updateDataResponse.get("_error_code"),updateDataResponse.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","0", updateDataResponse.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","", updateDataResponse.get("_error_msg"));


    Map<String, Object> searchDataResponse = searchArchivosReconciliacionLog(nombreDeArchivo,proceso, tipo, null);
    List result = (List)searchDataResponse.get("result");
    Map<String, Object> archRecon = (Map)result.get(0);

    Assert.assertEquals("El Cambio de OK A READING fue satisfactorio ",futureStatusChange,archRecon.get("_status"));
  }


  @Test
  public void testChangeStatusFieldIdNull() throws Exception{
    String futureStatusChange;
    futureStatusChange = "READING";

    Map<String, Object> updateDataResponse = updateArchivoReconcialicionLog(null,futureStatusChange);
    System.out.println(String.format("Num Err: %s Msj: %s",updateDataResponse.get("_error_code"),updateDataResponse.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC000", updateDataResponse.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","[prp_actualiza_archivo_conciliacion] El id de registro es obligatoria", updateDataResponse.get("_error_msg"));

  }

  @Test
  public void testChangeStatusFieldStatusNull() throws Exception{
    Long id = new Long(1);

    Map<String, Object> updateDataResponse = updateArchivoReconcialicionLog(id,null);
    System.out.println(String.format("Num Err: %s Msj: %s",updateDataResponse.get("_error_code"),updateDataResponse.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC004", updateDataResponse.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","[prp_actualiza_archivo_conciliacion] El status es obligatorio", updateDataResponse.get("_error_msg"));
  }

  @Test
  public void testChangeStatusFieldIdAndStatusNull() throws Exception{
    Map<String, Object> updateDataResponse = updateArchivoReconcialicionLog(null,null);
    System.out.println(String.format("Num Err: %s Msj: %s",updateDataResponse.get("_error_code"),updateDataResponse.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC006", updateDataResponse.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","[prp_actualiza_archivo_conciliacion] El id y el status son obligatorios", updateDataResponse.get("_error_msg"));
  }

  @Test
  public void testChangeStatusFail() throws Exception{

    String nombreDeArchivo;
    String proceso;
    String tipo;
    String status;
    String futureStatusChange;
    Long id;
    Long wrongId;

    nombreDeArchivo = "Archivo Prueba "+getRandomNumericString(10);
    proceso = "TECNOCOM";
    tipo = "CARGAS";
    status = "READING";
    futureStatusChange = "OK";

    Map<String, Object> insertDataResponse = insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);
    id = (long)insertDataResponse.get("id");
    wrongId = new Long(100001);

    Map<String, Object> updateDataResponse = updateArchivoReconcialicionLog(wrongId,futureStatusChange);
    System.out.println(String.format("Num Err: %s Msj: %s",updateDataResponse.get("_error_code"),updateDataResponse.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC005", updateDataResponse.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","[prp_actualiza_archivo_conciliacion] El id no se encuentra, el registro no se pudo actualizar", updateDataResponse.get("_error_msg"));

    Map<String, Object> searchDataResponse = searchArchivosReconciliacionLog(nombreDeArchivo,proceso, tipo, null);
    List result = (List)searchDataResponse.get("result");
    Map<String, Object> archRecon = (Map)result.get(0);

    Assert.assertNotEquals("El Cambio de OK A READING no fue satisfactorio ",futureStatusChange,archRecon.get("_status"));

  }

}
