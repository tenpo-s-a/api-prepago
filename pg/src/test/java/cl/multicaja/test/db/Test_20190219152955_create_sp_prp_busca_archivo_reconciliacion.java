package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Test_20190219152955_create_sp_prp_busca_archivo_reconciliacion  extends TestDbBasePg {

  private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final String SP_NAME = SCHEMA + ".prp_busca_archivo_reconciliacion";
  private static final String TABLE_NAME = "prp_archivos_reconciliacion";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_archivos_reconciliacion", SCHEMA));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_archivos_reconciliacion", SCHEMA));
  }

  //Opciones Filtros
  /*
   proceso: tecnocom, switch
   tipo: cargas, retiros, retiros_rechazados, cargas_rechazadas, tecnocom_file
   status: reading,ok
  */

  public Map<String, Object> searchArchivosReconciliacionLog(
    String nombreDeArchivo, String proceso, String tipo, String status) throws SQLException {

    Object[] params = {
      nombreDeArchivo != null ? nombreDeArchivo : new NullParam(Types.VARCHAR),
      proceso != null ? proceso : new NullParam(Types.VARCHAR),
      tipo != null ? tipo : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR)
    };

    return dbUtils.execute(SCHEMA + ".prp_busca_archivo_reconciliacion", params);
  }

  /**
   *
   * @param map
   */
  private void checkColumns(Map<String, Object> map) {

    String[] columns = {
      "_id",
      "_nombre_de_archivo",
      "_proceso",
      "_tipo",
      "_status",
      "_created_at",
      "_updated_at"
    };

    for (String column : columns) {
      Assert.assertTrue("Debe contener la columna " + column, map.containsKey(column));
    }

    Assert.assertEquals("Debe contener solamente las columnas definidas", columns.length, map.keySet().size());
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

  @Test
  public void testFindByAllFields() throws Exception{

    String nombreDeArchivo;
    String proceso;
    String tipo;
    String status;


    nombreDeArchivo = "Archivo Prueba "+getRandomNumericString(10);
    proceso = "TECNOCOM";
    tipo = "CARGAS";
    status = "OK";

    Map<String, Object> insertDataResponse = insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);

    Map<String, Object> searchDataResponse = searchArchivosReconciliacionLog(nombreDeArchivo,proceso, tipo, status);
    List result = (List)searchDataResponse.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> archRecon = (Map)result.get(0);

    checkColumns(archRecon);

    String keyToFind;
    for (Map.Entry<String, Object> entry : archRecon.entrySet()) {
      keyToFind = entry.getKey().substring(1,entry.getKey().length()).trim();

      System.out.println("Campo: "+keyToFind+" valorRetInsercion: "+insertDataResponse.get(keyToFind)+" valorRetBusq: "+entry.getValue());
      Assert.assertEquals("Para el campo "+keyToFind+" sus valores coinciden",
        insertDataResponse.get(keyToFind),entry.getValue());
    }

  }

  @Test
  public void testFindByNombreDeArchivo() throws Exception{
    String nombreDeArchivo;
    String proceso;
    String tipo;
    String status;

    nombreDeArchivo = "Archivo Prueba "+getRandomNumericString(10);
    proceso = "TECNOCOM";
    tipo = "CARGAS";
    status = "OK";

    Map<String, Object> insertDataResponse = insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);

    Map<String, Object> searchDataResponse = searchArchivosReconciliacionLog(nombreDeArchivo,null, null, null);
    List result = (List)searchDataResponse.get("result");

    Map<String, Object> archRecon = (Map)result.get(0);

    checkColumns(archRecon);

    String keyToFind;
    for (Map.Entry<String, Object> entry : archRecon.entrySet()) {
      keyToFind = entry.getKey().substring(1,entry.getKey().length()).trim();

      System.out.println("Campo: "+keyToFind+" valorRetInsercion: "+insertDataResponse.get(keyToFind)+" valorRetBusq: "+entry.getValue());
      Assert.assertEquals("Para el campo "+keyToFind+" sus valores coinciden",
        insertDataResponse.get(keyToFind),entry.getValue());
    }

  }


  @Test
  public void testFindByProceso() throws Exception{
    String nombreDeArchivo;
    String proceso;
    String tipo;
    String status;

    nombreDeArchivo = "Archivo Prueba "+getRandomNumericString(10);
    proceso = "SWITCH";
    tipo = "CARGAS";
    status = "OK";

    Map<String,String> mapParams = new HashMap<>();
    mapParams.put("_nombre_de_archivo",nombreDeArchivo);
    mapParams.put("_proceso",proceso);
    mapParams.put("tipo",tipo);
    mapParams.put("status",status);

    Map<String, Object> insertDataResponse = insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);

    Map<String, Object> searchDataResponse = searchArchivosReconciliacionLog(null,proceso, null, null);
    List result = (List)searchDataResponse.get("result");

    Map<String, Object> archRecon = (Map)result.get(0);

    checkColumns(archRecon);

    String keyToFind;
    for (Map.Entry<String, Object> entry : archRecon.entrySet()) {
      keyToFind = entry.getKey().substring(1,entry.getKey().length()).trim();

      System.out.println("Campo: "+keyToFind+" valorRetInsercion: "+insertDataResponse.get(keyToFind)+" valorRetBusq: "+entry.getValue());
      Assert.assertEquals("Para el campo "+keyToFind+" sus valores coinciden",
        insertDataResponse.get(keyToFind),entry.getValue());
    }

  }


  @Test
  public void testFindByTipo() throws Exception{
    String nombreDeArchivo;
    String proceso;
    String tipo;
    String status;

    nombreDeArchivo = "Archivo Prueba "+getRandomNumericString(10);
    proceso = "SWITCH";
    tipo = "RETIROS";
    status = "OK";

    Map<String, Object> insertDataResponse = insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);

    Map<String, Object> searchDataResponse = searchArchivosReconciliacionLog(null,null, tipo, null);
    List result = (List)searchDataResponse.get("result");

    Map<String, Object> archRecon = (Map)result.get(0);

    checkColumns(archRecon);

    String keyToFind;
    for (Map.Entry<String, Object> entry : archRecon.entrySet()) {
      keyToFind = entry.getKey().substring(1,entry.getKey().length()).trim();

      System.out.println("Campo: "+keyToFind+" valorRetInsercion: "+insertDataResponse.get(keyToFind)+" valorRetBusq: "+entry.getValue());
      Assert.assertEquals("Para el campo "+keyToFind+" sus valores coinciden",
        insertDataResponse.get(keyToFind),entry.getValue());
    }

  }


  @Test
  public void testFindByStatus() throws Exception{
    String nombreDeArchivo;
    String proceso;
    String tipo;
    String status;

    nombreDeArchivo = "Archivo Prueba "+getRandomNumericString(10);
    proceso = "SWITCH";
    tipo = "RETIROS";
    status = "READING";

    Map<String, Object> insertDataResponse = insertArchivoReconcialicionLog(nombreDeArchivo,proceso,tipo,status);

    Map<String, Object> searchDataResponse = searchArchivosReconciliacionLog(null,null, null, status);
    List result = (List)searchDataResponse.get("result");

    Map<String, Object> archRecon = (Map)result.get(0);

    checkColumns(archRecon);

    String keyToFind;
    for (Map.Entry<String, Object> entry : archRecon.entrySet()) {
      keyToFind = entry.getKey().substring(1,entry.getKey().length()).trim();

      System.out.println("Campo: "+keyToFind+" valorRetInsercion: "+insertDataResponse.get(keyToFind)+" valorRetBusq: "+entry.getValue());
      Assert.assertEquals("Para el campo "+keyToFind+" sus valores coinciden",
        insertDataResponse.get(keyToFind),entry.getValue());
    }


  }

}
