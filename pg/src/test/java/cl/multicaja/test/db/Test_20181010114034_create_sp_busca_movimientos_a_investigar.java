package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import org.apache.commons.lang3.RandomUtils;
import org.apache.ibatis.jdbc.Null;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class Test_20181010114034_create_sp_busca_movimientos_a_investigar extends TestDbBasePg {
  private static final String SP_NAME = SCHEMA + ".mc_prp_busca_movimientos_a_investigar_v10";

  @Before
  public void before() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  @After
  public void after() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  @Test
  public void testBuscaMovimientosAInvestigar_idOk() throws SQLException {

    Test_20181009113614_create_sp_crea_movimiento_investigar.creaMovimientoInvestigar("idMov=48", "Testing", "archivo.txt");

    Object[] params = {
      new InParam("idMov=48", Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ResearchMovement researchMovement = new ResearchMovement();
      researchMovement.id = numberUtils.toLong(row.get("_id"));
      researchMovement.idRef = String.valueOf(row.get("_mov_ref"));
      researchMovement.origin = String.valueOf(row.get("_origen"));
      researchMovement.fileName = String.valueOf(row.get("_nombre_archivo"));
      researchMovement.createdAt = (Timestamp) row.get("_fecha_registro");
      return researchMovement;
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, rm, params);

    List results = (List)resp.get("result");
    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Deben encontrar 1 movimiento", 1, results.size());

    ResearchMovement researchMovement = (ResearchMovement) results.get(0);
    Assert.assertEquals("Debe tener el mismo mov ref", "idMov=48", researchMovement.idRef);
    Assert.assertEquals("Debe tener mismo origen", "Testing", researchMovement.origin);
    Assert.assertEquals("Debe tener el mismo archivo", "archivo.txt", researchMovement.fileName);
    Assert.assertNotNull("Debe tener una fecha de creacion", researchMovement.createdAt);
  }

  @Test
  public void testBuscaMovimientosAInvestigar_idNull() throws SQLException {

    Test_20181009113614_create_sp_crea_movimiento_investigar.creaMovimientoInvestigar("idMov=48", "Testing", "archivo1.txt");
    Test_20181009113614_create_sp_crea_movimiento_investigar.creaMovimientoInvestigar("idMov=49", "Testong", "archivo2.txt");
    Test_20181009113614_create_sp_crea_movimiento_investigar.creaMovimientoInvestigar("idMov=50", "Testung", "archivo3.txt");

    Object[] params = {
      new NullParam(Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ResearchMovement researchMovement = new ResearchMovement();
      researchMovement.id = numberUtils.toLong(row.get("_id"));
      researchMovement.idRef = String.valueOf(row.get("_mov_ref"));
      researchMovement.origin = String.valueOf(row.get("_origen"));
      researchMovement.fileName = String.valueOf(row.get("_nombre_archivo"));
      researchMovement.createdAt = (Timestamp) row.get("_fecha_registro");
      return researchMovement;
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, rm, params);

    List results = (List)resp.get("result");
    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Deben encontrar 3 movimientos", 3, results.size());
  }

  @Test
  public void testBuscaMovimientosAInvestigar_otherId() throws SQLException {

    Test_20181009113614_create_sp_crea_movimiento_investigar.creaMovimientoInvestigar("idMov=10", "Testing", "archivo1.txt");

    Object[] params = {
      new InParam("otro", Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ResearchMovement researchMovement = new ResearchMovement();
      researchMovement.id = numberUtils.toLong(row.get("_id"));
      researchMovement.idRef = String.valueOf(row.get("_mov_ref"));
      researchMovement.origin = String.valueOf(row.get("_origen"));
      researchMovement.fileName = String.valueOf(row.get("_nombre_archivo"));
      researchMovement.createdAt = (Timestamp) row.get("_fecha_registro");
      return researchMovement;
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, rm, params);

    List results = (List)resp.get("result");
    Assert.assertNull("No debe existir", results);
  }

  class ResearchMovement {
    private Long id;
    private String idRef;
    private String fileName;
    private String origin;
    private Timestamp createdAt;
  }
}

