package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.Date;

import static cl.multicaja.test.db.Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard;
import static cl.multicaja.test.db.Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement;

/**
 * @autor vutreras
 */
public class Test_20190221171243_create_sp_mc_prp_borrar_movimientos_switch_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_borrar_movimientos_switch_v10";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento_switch cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_archivos_conciliacion cascade", SCHEMA));
  }

  @Test
  public void deleteSwitchMovements_all() throws SQLException {
    Map<String, Object> fileData1 = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre1.txt", "SWITCH", "retiros_rechazados", "OK");
    Map<String, Object> fileData2 = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre2.txt", "SWITCH", "retiros_rechazados", "OK");

    Long fileId = numberUtils.toLong(fileData1.get("_r_id"));
    Timestamp todayTimestamp = Timestamp.valueOf(LocalDateTime.now());

    ArrayList<Map<String, Object>> createdMovements = new ArrayList<>();
    createdMovements.add(Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(fileId, "multiId33", 10L, 100L, new BigDecimal(500), todayTimestamp));
    createdMovements.add(Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(fileId, "multiId34", 11L, 101L, new BigDecimal(501), todayTimestamp));
    createdMovements.add(Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(fileId, "multiId35", 12L, 102L, new BigDecimal(502), todayTimestamp));

    // Insertar uno mas de otro archivo
    Map<String, Object> insertedMovement = Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(numberUtils.toLong(fileData2.get("_r_id")), "multiId35", 12L, 102L, new BigDecimal(502), todayTimestamp);

    Object[] params = {
      fileId
    };
    dbUtils.execute(SP_NAME, params);

    // Borrar por fileId
    {
      Map<String, Object> foundSwitchMovement = Test_20190221143451_create_sp_mc_prp_buscar_movimientos_switch_v10.searchSwitchMovements("prp_movimiento_switch", null, fileId, null);
      List result = (List) foundSwitchMovement.get("result");

      Assert.assertNull("No deben existir", result);
    }

    // Buscar por id
    {
      Map<String, Object> resp = Test_20190221143451_create_sp_mc_prp_buscar_movimientos_switch_v10.searchSwitchMovements("prp_movimiento_switch", numberUtils.toLong(insertedMovement.get("id")), null, null);
      List result = (List) resp.get("result");

      Assert.assertNotNull("Debe existir", result);
      Assert.assertEquals("Debe tener 1 elemento", 1, result.size());
      Map<String, Object> foundSwitchMovement = (Map<String, Object>) result.get(0);

      Assert.assertEquals("Debe tener mismo archivo_id", numberUtils.toLong(insertedMovement.get("id_archivo")), numberUtils.toLong(foundSwitchMovement.get("_id_archivo")));
      Assert.assertEquals("Debe tener mismo multicaja id", insertedMovement.get("id_multicaja").toString(), foundSwitchMovement.get("_id_multicaja").toString());
      Assert.assertEquals("Debe tener mismo cliente_id", numberUtils.toLong(insertedMovement.get("id_cliente")), numberUtils.toLong(foundSwitchMovement.get("_id_cliente")));
      Assert.assertEquals("Debe tener mismo id_multicaja_ref", numberUtils.toLong(insertedMovement.get("id_multicaja_ref")), numberUtils.toLong(foundSwitchMovement.get("_id_multicaja_ref")));
      Assert.assertEquals("Debe tener mismo monto", ((BigDecimal)insertedMovement.get("monto")).stripTrailingZeros(), ((BigDecimal)foundSwitchMovement.get("_monto")).stripTrailingZeros());
      Assert.assertEquals("Debe tener misma fecha", insertedMovement.get("fecha_trx"), foundSwitchMovement.get("_fecha_trx"));
    }
  }

  @Test
  public void deleteSwitchMovements_notOK_noFileId() throws SQLException {
    Object[] params = {
      new NullParam(Types.BIGINT)
    };
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
    List result = (List) resp.get("result");
    Map<String, Object> mapResult = (Map)result.get(0);

    System.out.println(resp);
    Assert.assertEquals("Debe tener codigo de error MC001", "MC001", mapResult.get("_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", mapResult.get("_error_msg").toString());
  }
}
