package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
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
public class Test_20190221143451_create_sp_mc_prp_buscar_movimientos_switch_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_buscar_movimientos_switch_v10";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento_switch cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_archivos_conciliacion cascade", SCHEMA));
  }

  public static Map<String, Object> searchSwitchMovements(String tableName, Long id, Long fileId, String multicajaID) throws SQLException {
    Object[] params = {
      tableName != null ? tableName : new NullParam(Types.VARCHAR),
      id != null ? id : new NullParam(Types.BIGINT),
      fileId != null ? fileId : new NullParam(Types.BIGINT),
      multicajaID != null ? multicajaID : new NullParam(Types.VARCHAR)
    };
    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void searchSwitchMovements_all() throws SQLException {
    Map<String, Object> fileData1 = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre1.txt", "SWITCH", "retiros_rechazados", "OK");
    Map<String, Object> fileData2 = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre2.txt", "SWITCH", "retiros_rechazados", "OK");

    Long fileId = numberUtils.toLong(fileData1.get("_r_id"));
    Timestamp todayTimestamp = Timestamp.valueOf(LocalDateTime.now());

    ArrayList<Map<String, Object>> createdMovements = new ArrayList<>();
    Map<String, Object> insertedMovement = Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(fileId, "multiId33", 10L, 100L, new BigDecimal(500), todayTimestamp);
    createdMovements.add(insertedMovement);
    createdMovements.add(Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(fileId, "multiId34", 11L, 101L, new BigDecimal(501), todayTimestamp));
    createdMovements.add(Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(fileId, "multiId35", 12L, 102L, new BigDecimal(502), todayTimestamp));

    // Insertar uno mas de otro archivo
    createdMovements.add(Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10.insertSwitchMovement(numberUtils.toLong(fileData2.get("_r_id")), "multiId35", 12L, 102L, new BigDecimal(502), todayTimestamp));

    // Buscar por fileId
    {
      Map<String, Object> foundSwitchMovement = searchSwitchMovements("prp_movimiento_switch", null, fileId, null);
      List result = (List) foundSwitchMovement.get("result");

      Assert.assertNotNull("Debe existir", result);
      Assert.assertEquals("Debe tener 3 elementos", 3, result.size());

      int comparedMovements = 0;
      for (Map<String, Object> createdMovement : createdMovements) { // Por cada movimiento insertado
        for (Object foundMovObj : result) { // Por cada movimiento encontrado
          Map<String, Object> foundMovMap = (Map) foundMovObj;
          if (foundMovMap.get("_id").equals(createdMovement.get("id"))) { // Buscar si tienen el mismo id
            Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(foundMovMap.get("_id_archivo")));
            Assert.assertEquals("Debe tener mismo multicaja id", createdMovement.get("id_multicaja").toString(), foundMovMap.get("_id_multicaja").toString());
            Assert.assertEquals("Debe tener mismo cliente_id", numberUtils.toLong(createdMovement.get("id_cliente")), numberUtils.toLong(foundMovMap.get("_id_cliente")));
            Assert.assertEquals("Debe tener mismo id_multicaja_ref", numberUtils.toLong(createdMovement.get("id_multicaja_ref")), numberUtils.toLong(foundMovMap.get("_id_multicaja_ref")));
            Assert.assertEquals("Debe tener mismo monto", ((BigDecimal)createdMovement.get("monto")).stripTrailingZeros(), ((BigDecimal)foundMovMap.get("_monto")).stripTrailingZeros());
            Assert.assertEquals("Debe tener misma fecha", createdMovement.get("fecha_trx"), foundMovMap.get("_fecha_trx"));

            comparedMovements++;
          }
        }
      }
      Assert.assertEquals("Debe comparar 3 elementos", 3, comparedMovements);
    }

    // Buscar en tabla nula, busca en la tabla por defecto
    {
      Map<String, Object> foundSwitchMovement = searchSwitchMovements(null, null, fileId, null);
      List result = (List) foundSwitchMovement.get("result");

      Assert.assertNotNull("Debe existir", result);
      Assert.assertEquals("Debe tener 3 elementos", 3, result.size());

      int comparedMovements = 0;
      for (Map<String, Object> createdMovement : createdMovements) { // Por cada movimiento insertado
        for (Object foundMovObj : result) { // Por cada movimiento encontrado
          Map<String, Object> foundMovMap = (Map) foundMovObj;
          if (foundMovMap.get("_id").equals(createdMovement.get("id"))) { // Buscar si tienen el mismo id
            Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(foundMovMap.get("_id_archivo")));
            Assert.assertEquals("Debe tener mismo multicaja id", createdMovement.get("id_multicaja").toString(), foundMovMap.get("_id_multicaja").toString());
            Assert.assertEquals("Debe tener mismo cliente_id", numberUtils.toLong(createdMovement.get("id_cliente")), numberUtils.toLong(foundMovMap.get("_id_cliente")));
            Assert.assertEquals("Debe tener mismo id_multicaja_ref", numberUtils.toLong(createdMovement.get("id_multicaja_ref")), numberUtils.toLong(foundMovMap.get("_id_multicaja_ref")));
            Assert.assertEquals("Debe tener mismo monto", ((BigDecimal)createdMovement.get("monto")).stripTrailingZeros(), ((BigDecimal)foundMovMap.get("_monto")).stripTrailingZeros());
            Assert.assertEquals("Debe tener misma fecha", createdMovement.get("fecha_trx"), foundMovMap.get("_fecha_trx"));

            comparedMovements++;
          }
        }
      }
      Assert.assertEquals("Debe comparar 3 elementos", 3, comparedMovements);
    }

    // Buscar por fileId en tabla hist
    {
      Map<String, Object> foundSwitchMovement = searchSwitchMovements("prp_movimiento_switch_hist", null, fileId, null);
      List result = (List) foundSwitchMovement.get("result");

      Assert.assertNotNull("Debe existir", result);
      Assert.assertEquals("Debe tener 3 elementos", 3, result.size());

      int comparedMovements = 0;
      for (Map<String, Object> createdMovement : createdMovements) { // Por cada movimiento insertado
        for (Object foundMovObj : result) { // Por cada movimiento encontrado
          Map<String, Object> foundMovMap = (Map) foundMovObj;
          if (foundMovMap.get("_id").equals(createdMovement.get("id_hist"))) { // Buscar si tienen el mismo id
            Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(foundMovMap.get("_id_archivo")));
            Assert.assertEquals("Debe tener mismo multicaja id", createdMovement.get("id_multicaja").toString(), foundMovMap.get("_id_multicaja").toString());
            Assert.assertEquals("Debe tener mismo cliente_id", numberUtils.toLong(createdMovement.get("id_cliente")), numberUtils.toLong(foundMovMap.get("_id_cliente")));
            Assert.assertEquals("Debe tener mismo id_multicaja_ref", numberUtils.toLong(createdMovement.get("id_multicaja_ref")), numberUtils.toLong(foundMovMap.get("_id_multicaja_ref")));
            Assert.assertEquals("Debe tener mismo monto", ((BigDecimal)createdMovement.get("monto")).stripTrailingZeros(), ((BigDecimal)foundMovMap.get("_monto")).stripTrailingZeros());
            Assert.assertEquals("Debe tener misma fecha", createdMovement.get("fecha_trx"), foundMovMap.get("_fecha_trx"));

            comparedMovements++;
          }
        }
      }
      Assert.assertEquals("Debe comparar 3 elementos", 3, comparedMovements);
    }

    // Buscar por id
    {
      Map<String, Object> resp = searchSwitchMovements("prp_movimiento_switch", numberUtils.toLong(insertedMovement.get("id")), null, null);
      List result = (List) resp.get("result");

      Assert.assertNotNull("Debe existir", result);
      Assert.assertEquals("Debe tener 1 elemento", 1, result.size());
      Map<String, Object> foundSwitchMovement = (Map<String, Object>) result.get(0);

      Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(foundSwitchMovement.get("_id_archivo")));
      Assert.assertEquals("Debe tener mismo multicaja id", insertedMovement.get("id_multicaja").toString(), foundSwitchMovement.get("_id_multicaja").toString());
      Assert.assertEquals("Debe tener mismo cliente_id", numberUtils.toLong(insertedMovement.get("id_cliente")), numberUtils.toLong(foundSwitchMovement.get("_id_cliente")));
      Assert.assertEquals("Debe tener mismo id_multicaja_ref", numberUtils.toLong(insertedMovement.get("id_multicaja_ref")), numberUtils.toLong(foundSwitchMovement.get("_id_multicaja_ref")));
      Assert.assertEquals("Debe tener mismo monto", ((BigDecimal)insertedMovement.get("monto")).stripTrailingZeros(), ((BigDecimal)foundSwitchMovement.get("_monto")).stripTrailingZeros());
      Assert.assertEquals("Debe tener misma fecha", insertedMovement.get("fecha_trx"), foundSwitchMovement.get("_fecha_trx"));
    }

    // Buscar por id multicaja
    {
      Map<String, Object> resp = searchSwitchMovements("prp_movimiento_switch", null, null, insertedMovement.get("id_multicaja").toString());
      List result = (List) resp.get("result");

      Assert.assertNotNull("Debe existir", result);
      Assert.assertEquals("Debe tener 1 elemento", 1, result.size());
      Map<String, Object> foundSwitchMovement = (Map<String, Object>) result.get(0);

      Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(foundSwitchMovement.get("_id_archivo")));
      Assert.assertEquals("Debe tener mismo multicaja id", insertedMovement.get("id_multicaja").toString(), foundSwitchMovement.get("_id_multicaja").toString());
      Assert.assertEquals("Debe tener mismo cliente_id", numberUtils.toLong(insertedMovement.get("id_cliente")), numberUtils.toLong(foundSwitchMovement.get("_id_cliente")));
      Assert.assertEquals("Debe tener mismo id_multicaja_ref", numberUtils.toLong(insertedMovement.get("id_multicaja_ref")), numberUtils.toLong(foundSwitchMovement.get("_id_multicaja_ref")));
      Assert.assertEquals("Debe tener mismo monto", ((BigDecimal)insertedMovement.get("monto")).stripTrailingZeros(), ((BigDecimal)foundSwitchMovement.get("_monto")).stripTrailingZeros());
      Assert.assertEquals("Debe tener misma fecha", insertedMovement.get("fecha_trx"), foundSwitchMovement.get("_fecha_trx"));
    }

    // Buscar todos
    {
      Map<String, Object> foundSwitchMovement = searchSwitchMovements("prp_movimiento_switch", null, null, null);
      List result = (List) foundSwitchMovement.get("result");

      Assert.assertNotNull("Debe existir", result);
      Assert.assertEquals("Debe tener 4 elementos", 4, result.size());

      int comparedMovements = 0;
      for (Map<String, Object> createdMovement : createdMovements) { // Por cada movimiento insertado
        for (Object foundMovObj : result) { // Por cada movimiento encontrado
          Map<String, Object> foundMovMap = (Map) foundMovObj;
          if (foundMovMap.get("_id").equals(createdMovement.get("id"))) { // Buscar si tienen el mismo id
            Assert.assertEquals("Debe tener mismo archivo_id", numberUtils.toLong(createdMovement.get("id_archivo")), numberUtils.toLong(foundMovMap.get("_id_archivo")));
            Assert.assertEquals("Debe tener mismo multicaja id", createdMovement.get("id_multicaja").toString(), foundMovMap.get("_id_multicaja").toString());
            Assert.assertEquals("Debe tener mismo cliente_id", numberUtils.toLong(createdMovement.get("id_cliente")), numberUtils.toLong(foundMovMap.get("_id_cliente")));
            Assert.assertEquals("Debe tener mismo id_multicaja_ref", numberUtils.toLong(createdMovement.get("id_multicaja_ref")), numberUtils.toLong(foundMovMap.get("_id_multicaja_ref")));
            Assert.assertEquals("Debe tener mismo monto", ((BigDecimal)createdMovement.get("monto")).stripTrailingZeros(), ((BigDecimal)foundMovMap.get("_monto")).stripTrailingZeros());
            Assert.assertEquals("Debe tener misma fecha", createdMovement.get("fecha_trx"), foundMovMap.get("_fecha_trx"));

            comparedMovements++;
          }
        }
      }
      Assert.assertEquals("Debe comparar 4 elementos", 4, comparedMovements);
    }
  }
}
