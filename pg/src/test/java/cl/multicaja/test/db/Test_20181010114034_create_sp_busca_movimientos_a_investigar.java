package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Test_20181010114034_create_sp_busca_movimientos_a_investigar extends TestDbBasePg {
  private static final String SP_NAME = SCHEMA + ".mc_prp_busca_movimientos_a_investigar_v11";

  @Before
  public void before() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  @After
  public void after() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  public Map<String, Object> searchMovimientoInvestigar(String idArchivoOrigen) throws SQLException {

    Object[] params = {
      new InParam(idArchivoOrigen, Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ResearchMovement researchMovement = new ResearchMovement();
      researchMovement.id = numberUtils.toLong(row.get("_id"));
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

    return dbUtils.execute(SP_NAME, rm, params);
  }

  @Test
  public void testBuscaMovimientosAInvestigar_idOk() throws SQLException {

    String idArchivoOrigen = "idMov=48";//+ getRandomString(10);
    String origen = "Testing";
    String nombreArchivo = "archivo.txt";
    Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
    String responsable = "Conciliaciones Prepago";
    String descripcion = "Movimiento no encontrado en archivo";
    Long movRef = new Long(0);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen, origen,nombreArchivo,fechaDeTransaccion,responsable,descripcion,movRef);

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

  @Test
  public void testBuscaMovimientosAInvestigar_idNull() throws SQLException {

    String idArchivoOrigen1 = "idMov=48";//+ getRandomString(10);
    String idArchivoOrigen2 = "idMov=49";//+ getRandomString(10);
    String idArchivoOrigen3 = "idMov=50";//+ getRandomString(10);
    String idArchivoOrigenNull = null;
    String origen = "Testing";
    String nombreArchivo = "archivo.txt";
    Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
    String responsable = "Conciliaciones Prepago";
    String descripcion = "Movimiento no encontrado en archivo";
    Long movRef = new Long(0);


    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen1, origen,nombreArchivo,fechaDeTransaccion,responsable,descripcion,movRef);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen2, origen,nombreArchivo,fechaDeTransaccion,responsable,descripcion,movRef);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen3, origen,nombreArchivo,fechaDeTransaccion,responsable,descripcion,movRef);

    Map<String, Object> resp = searchMovimientoInvestigar(idArchivoOrigenNull);
    List results = (List)resp.get("result");
    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Deben encontrar 3 movimientos", 3, results.size());

  }

  @Test
  public void testBuscaMovimientosAInvestigar_otherId() throws SQLException {

    String idArchivoOrigen = "idMov=10";
    String origen = "Testing";
    String nombreArchivo = "archivo.txt";
    Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
    String responsable = "Conciliaciones Prepago";
    String descripcion = "Movimiento no encontrado en archivo";
    Long movRef = new Long(0);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen, origen,nombreArchivo,fechaDeTransaccion,responsable,descripcion,movRef);

    Map<String, Object> resp = searchMovimientoInvestigar("otro");
    List results = (List)resp.get("result");
    Assert.assertNull("No debe existir", results);
  }


  class ResearchMovement {
    private Long id;
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

