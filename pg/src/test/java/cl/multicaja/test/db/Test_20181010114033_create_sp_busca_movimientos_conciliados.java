package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import com.sun.xml.internal.ws.wsdl.writer.document.ParamType;
import org.apache.commons.lang3.RandomUtils;
import org.junit.*;
import cl.multicaja.test.TestDbBasePg;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class Test_20181010114033_create_sp_busca_movimientos_conciliados extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_busca_movimientos_conciliados_v10";

  @Before
  public void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @After
  public void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @Test
  public void testBuscaMovimientosConciliados_idOk() throws SQLException {

    Map<String,Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long movementId = numberUtils.toLong(mov.get("_id"));
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId,"TEST_ACTION","TEST_STATUS");

    Object[] params = {
      new InParam(movementId, Types.BIGINT),
      new OutParam("id", Types.BIGINT),
      new OutParam("id_mov_ref", Types.BIGINT),
      new OutParam("fecha_registro", Types.TIMESTAMP),
      new OutParam("accion", Types.VARCHAR),
      new OutParam("estado", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    System.out.println("Resp: " + resp);
    Assert.assertNotNull("Debe existir", resp);
    Assert.assertEquals("Deben tener el mismo id", movementId, numberUtils.toLong(resp.get("id_mov_ref")));
    Assert.assertEquals("Deben tener la misma accion", "TEST_ACTION", String.valueOf(resp.get("accion")));
    Assert.assertEquals("Deben tener el mismo status", "TEST_STATUS", String.valueOf(resp.get("estado")));
  }

  @Test
  public void testBuscaMovimientosConciliados_idNull() throws SQLException {

    Map<String,Object> mov1 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long movementId1 = numberUtils.toLong(mov1.get("_id"));
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId1,"TEST_ACTION","TEST_STATUS");

    Map<String,Object> mov2 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long movementId2 = numberUtils.toLong(mov2.get("_id"));
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId2,"TEST_ACTION","TEST_STATUS");

    Map<String,Object> mov3 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long movementId3 = numberUtils.toLong(mov2.get("_id"));
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId3,"TEST_ACTION","TEST_STATUS");

    Object[] params = {
      new NullParam(Types.BIGINT)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ReconciledMovement reconciledMovement = new ReconciledMovement();
      reconciledMovement.id = numberUtils.toLong(row.get("_id"));
      reconciledMovement.idMovRef = numberUtils.toLong(row.get("_id_mov_ref"));
      reconciledMovement.actionType = String.valueOf(row.get("_action"));
      reconciledMovement.actionType = String.valueOf(row.get("_estado"));
      return reconciledMovement;
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, rm, params);

    List results = (List)resp.get("result");
    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Deben encontrar 3 movimientos", 3, results.size());
  }

  @Test
  public void testBuscaMovimientosConciliados_idRandom() throws SQLException {

    Map<String,Object> mov1 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long movementId1 = numberUtils.toLong(mov1.get("_id"));
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId1,"TEST_ACTION","TEST_STATUS");

    Map<String,Object> mov2 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long movementId2 = numberUtils.toLong(mov2.get("_id"));
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId2,"TEST_ACTION","TEST_STATUS");

    Map<String,Object> mov3 = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long movementId3 = numberUtils.toLong(mov2.get("_id"));
    Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId3,"TEST_ACTION","TEST_STATUS");

    Object[] params = {
      RandomUtils.nextLong(1000, 2000)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ReconciledMovement reconciledMovement = new ReconciledMovement();
      reconciledMovement.id = numberUtils.toLong(row.get("_id"));
      reconciledMovement.idMovRef = numberUtils.toLong(row.get("_id_mov_ref"));
      reconciledMovement.actionType = String.valueOf(row.get("_action"));
      reconciledMovement.actionType = String.valueOf(row.get("_estado"));
      return reconciledMovement;
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, rm, params);

    List results = (List)resp.get("result");
    Assert.assertNull("No debe existir", results);
  }

  class ReconciledMovement {
    public Long id;
    public Long idMovRef;
    public String reconciliationStatusType;
    public String actionType;
    public Timestamp createdAt;
  }
}


