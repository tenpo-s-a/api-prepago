package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Test_20181010114035_create_sp_busca_movimientos_a_investigar_v12 extends TestDbBasePg {
  private static final String SP_NAME = SCHEMA + ".mc_prp_busca_movimientos_a_investigar_v12";

  @Before
  public void before() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  @After
  public void after() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  public Map<String, Object> searchMovimientoInvestigar(String idArchivoOrigen, Timestamp beginDate, Timestamp endDate) throws SQLException {

    Object[] params = {
      idArchivoOrigen != null ? new InParam(idArchivoOrigen, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      beginDate != null ? new InParam(beginDate, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP),
      endDate != null ? new InParam(endDate, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP)
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

    String idArchivoOrigen = "idMov=48";
    String origen = "Testing";
    String nombreArchivo = "archivo.txt";
    Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
    String responsable = "Conciliaciones Prepago";
    String descripcion = "Movimiento no encontrado en archivo";
    Long movRef = new Long(0);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen, origen,nombreArchivo,fechaDeTransaccion,responsable,descripcion,movRef);

    Map<String, Object> resp = searchMovimientoInvestigar(idArchivoOrigen, null, null);
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
  public void testBuscaMovimientosAInvestigar_fechaOk() throws SQLException {

    String idArchivoOrigen1 = "idMov=10";
    String idArchivoOrigen2 = "idMov=11";
    String idArchivoOrigen3 = "idMov=12";
    String idArchivoOrigen4 = "idMov=13";
    String origen = "Testing";
    String nombreArchivo = "archivo.txt";
    Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
    String responsable = "Conciliaciones Prepago";
    String descripcion = "Movimiento no encontrado en archivo";
    Long movRef = new Long(0);

    ArrayList<String> insertedResearchIds = new ArrayList<>();

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen1, origen,nombreArchivo, fechaDeTransaccion, responsable,descripcion,movRef);
    changeResearch("idMov=10", "2012-05-05 05:05:05.00"); // Change to another date outside of range
    insertedResearchIds.add(idArchivoOrigen1);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen2, origen,nombreArchivo, fechaDeTransaccion, responsable,descripcion,movRef);
    insertedResearchIds.add(idArchivoOrigen2);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen3, origen,nombreArchivo, fechaDeTransaccion, responsable,descripcion,movRef);
    insertedResearchIds.add(idArchivoOrigen3);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen4, origen,nombreArchivo, fechaDeTransaccion, responsable,descripcion,movRef);
    changeResearch("idMov=13", "3012-05-05 05:05:05.00"); // Change to another date outside of range
    insertedResearchIds.add(idArchivoOrigen4);

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);

    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    endDateTime = endDateTime.plusHours(1);

    Map<String, Object> resp = searchMovimientoInvestigar(null, Timestamp.valueOf(beginDateTime), Timestamp.valueOf(endDateTime));
    List results = (List)resp.get("result");
    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Deben encontrar 2 movimientos", 2, results.size());

    int comparedMovements = 0;
    for(String id : insertedResearchIds) {
      for(Object foundObject : results) {
        ResearchMovement foundResearch = (ResearchMovement) foundObject;
        System.out.println("Foun research: " + foundResearch);
        if(id.equals(foundResearch.idArchivoOrigen)) {
          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Deben compararse los 2 movimientos", 2, comparedMovements);


    // Check all before some date
    LocalDateTime endDateTime2 = LocalDateTime.now(ZoneId.of("UTC"));
    endDateTime2 = endDateTime2.minusHours(1);

    resp = searchMovimientoInvestigar(null, null, Timestamp.valueOf(endDateTime2));
    results = (List)resp.get("result");
    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Deben encontrar 1 movimientos", 1, results.size());

    comparedMovements = 0;
    for(String id : insertedResearchIds) {
      for(Object foundObject : results) {
        ResearchMovement foundResearch = (ResearchMovement) foundObject;
        if(id.equals(foundResearch.idArchivoOrigen)) {
          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Deben compararse 1 movimiento", 1, comparedMovements);

    // Check all after some date
    resp = searchMovimientoInvestigar(null, Timestamp.valueOf(beginDateTime), null);
    results = (List)resp.get("result");
    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Deben encontrar 3 movimientos", 3, results.size());

    comparedMovements = 0;
    for(String id : insertedResearchIds) {
      for(Object foundObject : results) {
        ResearchMovement foundResearch = (ResearchMovement) foundObject;
        if(id.equals(foundResearch.idArchivoOrigen)) {
          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Deben compararse 3 movimiento", 3, comparedMovements);
  }

  @Test
  public void testBuscaMovimientosAInvestigar_idNull() throws SQLException {

    String idArchivoOrigen1 = "idMov=48";
    String idArchivoOrigen2 = "idMov=49";
    String idArchivoOrigen3 = "idMov=50";
    String origen = "Testing";
    String nombreArchivo = "archivo.txt";
    Timestamp fechaDeTransaccion = new Timestamp((new Date()).getTime());
    String responsable = "Conciliaciones Prepago";
    String descripcion = "Movimiento no encontrado en archivo";
    Long movRef = new Long(0);


    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen1, origen,nombreArchivo, fechaDeTransaccion, responsable,descripcion,movRef);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen2, origen,nombreArchivo, fechaDeTransaccion, responsable,descripcion,movRef);

    Test_20181009113614_create_sp_crea_movimiento_investigar.setMovimientoInvestigar(
      idArchivoOrigen3, origen,nombreArchivo, fechaDeTransaccion, responsable,descripcion,movRef);

    Map<String, Object> resp = searchMovimientoInvestigar(null, null, null);
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

    Map<String, Object> resp = searchMovimientoInvestigar("otro", null, null);
    List results = (List)resp.get("result");
    Assert.assertNull("No debe existir", results);
  }

  static public void changeResearch(String idMovimiento, String newDate)  {
    dbUtils.getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento_investigar SET fecha_registro = "
        + "(TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone) "
        + "WHERE id_archivo_origen = '" + idMovimiento + "'");
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

