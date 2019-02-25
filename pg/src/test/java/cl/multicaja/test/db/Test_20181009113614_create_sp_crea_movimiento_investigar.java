package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Test_20181009113614_create_sp_crea_movimiento_investigar extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_crea_movimiento_investigar_v11";
  private static final String SP_BUSCAR_NAME = SCHEMA + ".mc_prp_busca_movimientos_a_investigar_v11";
  private static final String TABLE_NAME = "prp_archivos_conciliacion";

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", SCHEMA));
    
  }


  public Map<String, Object> searchMovimientoInvestigar(String idArchivoOrigen) throws SQLException {

    Object[] params = {
      new InParam(idArchivoOrigen, Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ResearchMovement researchMovement = new ResearchMovement();
      researchMovement.id = numberUtils.toLong(row.get("_id"));
      //researchMovement.idRef = String.valueOf(row.get("_mov_ref"));
      researchMovement.idArchivoOrigen = String.valueOf(row.get("_id_archivo_origen"));
      researchMovement.origen = String.valueOf(row.get("_origen"));
      researchMovement.nombreArchivo = String.valueOf(row.get("_nombre_archivo"));
      researchMovement.fechaRegistro = (Timestamp) row.get("_fecha_registro");
      researchMovement.fechaDeTransaccion = (Timestamp) row.get("_fecha_de_transaccion");
      researchMovement.responsable = String.valueOf(row.get("_responsable"));
      researchMovement.descripcion = String.valueOf(row.get("_descripcion"));
      researchMovement.movRef = (long) row.get("_mov_ref");

      return researchMovement;
    };

    return dbUtils.execute(SP_BUSCAR_NAME, rm, params);
  }

  @Test
  public void testCreaMovimientoInvestigar() throws SQLException {

    {
      System.out.println("TEST 1");
      Map<String, Object> data = setMovimientoInvestigar(
        null, null, null, null, null, null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales",
        "[mc_prp_crea_movimiento_investigar_v11] id_archivo_origen, origen, nombre_archivo, fecha_de_transaccion, responsable, descripcion y mov_ref son campos obligatorios",
        data.get("_error_msg"));
    }

    {
      System.out.println("TEST 2");
      Map<String, Object> data = setMovimientoInvestigar(
        getRandomString(10), null, null, null, null, null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","[mc_prp_crea_movimiento_investigar_v11] El origen es obligatorio",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 3");
      Map<String, Object> data = setMovimientoInvestigar(
        getRandomString(10), getRandomString(10), null, null, null, null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","[mc_prp_crea_movimiento_investigar_v11] El nombre_archivo es obligatorio",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 4");
      Map<String, Object> data = setMovimientoInvestigar(
        getRandomString(10), getRandomString(10), getRandomString(10), null, null, null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","[mc_prp_crea_movimiento_investigar_v11] La fecha_de_transaccion es obligatoria",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 5");
      System.out.println(new Timestamp((new Date()).getTime()).toString());
      Map<String, Object> data = setMovimientoInvestigar(
        getRandomString(10), getRandomString(10), getRandomString(10), new Timestamp((new Date()).getTime()), null, null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","[mc_prp_crea_movimiento_investigar_v11] El responsable es obligatorio",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 6");
      Map<String, Object> data = setMovimientoInvestigar(
        getRandomString(10), getRandomString(10),
        getRandomString(10), new Timestamp((new Date()).getTime()), getRandomString(10), null,null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","[mc_prp_crea_movimiento_investigar_v11] La descripcion es obligatoria",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 7");
      Map<String, Object> data = setMovimientoInvestigar(
        getRandomString(10), getRandomString(10),
        getRandomString(10), new Timestamp((new Date()).getTime()), getRandomString(10), getRandomString(10),null);
      System.out.println(data.get("_error_msg"));
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("No debe ser 0","101000",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","[mc_prp_crea_movimiento_investigar_v11] El mov_ref es obligatorio",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 8");
      Map<String, Object> data = setMovimientoInvestigar(
        getRandomString(10), getRandomString(10),
        getRandomString(10), new Timestamp((new Date()).getTime()), getRandomString(10), getRandomString(10),new Long(0));
      System.out.println("Se ingreso satisfactoriamente un valor igual a 0");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("Debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
    }

    {
      System.out.println("TEST 9");

      String idArchivoOrigen = getRandomString(10);
      String origen = getRandomString(10);
      String nombreArchivo = getRandomString(10);
      Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
      String responsable = getRandomString(10);
      String descripcion = getRandomString(10);
      Long movRef = new Long(0);

      Map<String, Object> data = setMovimientoInvestigar(
        idArchivoOrigen, origen,
        nombreArchivo, fechaDeTransaccion, responsable, descripcion,movRef);
      System.out.println("Movimiento ingresado satisfactoriamente");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("Debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));

      Map<String, Object> resp = searchMovimientoInvestigar(idArchivoOrigen);
      List results = (List)resp.get("result");
      Assert.assertNotNull("Debe existir", results);
      Assert.assertEquals("Deben encontrar 1 movimiento", 1, results.size());

      ResearchMovement researchMovement = (ResearchMovement) results.get(0);
      Assert.assertEquals("Deben ser iguales",idArchivoOrigen,researchMovement.idArchivoOrigen);
      Assert.assertEquals("Deben ser iguales",origen,researchMovement.origen);
      Assert.assertEquals("Deben ser iguales",nombreArchivo,researchMovement.nombreArchivo);
      Assert.assertEquals("Deben ser iguales",fechaDeTransaccion,researchMovement.fechaDeTransaccion);
      Assert.assertEquals("Deben ser iguales",responsable,researchMovement.responsable);
      Assert.assertEquals("Deben ser iguales",descripcion,researchMovement.descripcion);
      Assert.assertEquals("Deben ser iguales",movRef,researchMovement.movRef);

    }

    {
      System.out.println("TEST 10");

      String idArchivoOrigen = getRandomString(10);
      String origen = getRandomString(10);
      String nombreArchivo = getRandomString(10);
      Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
      String responsable = getRandomString(10);
      String descripcion = getRandomString(10);
      Long movRef = new Long(0);
      Long idCreated;

      Map<String, Object> data = setMovimientoInvestigar(
        idArchivoOrigen, origen,
        nombreArchivo, fechaDeTransaccion, responsable, descripcion,movRef);
      System.out.println("Movimiento ingresado satisfactoriamente");
      Assert.assertNotNull("Data no debe ser null", data);
      Assert.assertEquals("Debe ser 0","0",data.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",data.get("_error_msg"));
      Assert.assertNotEquals("No debe ser igual a 0",0,data.get("_r_id"));

      idCreated = (long)data.get("_r_id");

      Map<String, Object> resp = searchMovimientoInvestigar(idArchivoOrigen);
      List results = (List)resp.get("result");
      Assert.assertNotNull("Debe existir", results);
      Assert.assertEquals("Deben encontrar 1 movimiento", 1, results.size());

      ResearchMovement researchMovement = (ResearchMovement) results.get(0);
      Assert.assertEquals("Deben ser iguales",idArchivoOrigen,researchMovement.idArchivoOrigen);
      Assert.assertEquals("Deben ser iguales",origen,researchMovement.origen);
      Assert.assertEquals("Deben ser iguales",nombreArchivo,researchMovement.nombreArchivo);
      Assert.assertEquals("Deben ser iguales",fechaDeTransaccion,researchMovement.fechaDeTransaccion);
      Assert.assertEquals("Deben ser iguales",responsable,researchMovement.responsable);
      Assert.assertEquals("Deben ser iguales",descripcion,researchMovement.descripcion);
      Assert.assertEquals("Deben ser iguales",movRef,researchMovement.movRef);
      Assert.assertEquals("Id Creado debe ser igual a retornado",idCreated,researchMovement.id);

    }

  }

  public static Map<String, Object> setMovimientoInvestigar (
    String id_archivo_origen,
    String origen,
    String nombre_archivo,
    Timestamp fecha_de_transaccion,
    String responsable,
    String descripcion,
    Long mov_ref
  ) throws SQLException {

    Object[] params = {
      id_archivo_origen != null ? id_archivo_origen : new NullParam(Types.VARCHAR),
      origen != null ? origen : new NullParam(Types.VARCHAR),
      nombre_archivo != null ? nombre_archivo : new NullParam(Types.VARCHAR),
      fecha_de_transaccion != null ? fecha_de_transaccion : new NullParam(Types.TIMESTAMP),
      responsable != null ? responsable : new NullParam(Types.VARCHAR),
      descripcion != null ? descripcion : new NullParam(Types.VARCHAR),
      mov_ref != null ? mov_ref : new NullParam(Types.BIGINT),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return dbUtils.execute(SP_NAME, params);
  };

  public static Map<String, Object> creaMovimientoInvestigar(
    String id_archivo_origen, String origen, String nombreArchivo) throws SQLException {

    //TODO: Estas variables deben colocarse como nuevos parámetros de esta función y asi mismo en implementaciones similares que usen el procedimiento, con sus nuevos cambios
    Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
    String responsable = " ";
    String descripcion = " ";
    Long movRef = new Long(10);

    Object[] params = {
      id_archivo_origen != null ? id_archivo_origen : new NullParam(Types.VARCHAR),
      origen != null ? origen : new NullParam(Types.VARCHAR),
      nombreArchivo != null ? nombreArchivo : new NullParam(Types.VARCHAR),
      fechaDeTransaccion != null ? fechaDeTransaccion : new NullParam(Types.TIMESTAMP),
      responsable != null ? responsable : new NullParam(Types.VARCHAR),
      descripcion != null ? descripcion : new NullParam(Types.VARCHAR),
      movRef != null ? movRef : new NullParam(Types.BIGINT),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR) };
    return dbUtils.execute(SP_NAME, params);
  }

  class ResearchMovement {
    private Long id;
    //private String idRef;
    private String idArchivoOrigen;
    private String origen;
    private String nombreArchivo;
    private Timestamp fechaRegistro;
    private Timestamp fechaDeTransaccion;
    private String responsable;
    private String descripcion;
    private Long movRef;
  }

}
