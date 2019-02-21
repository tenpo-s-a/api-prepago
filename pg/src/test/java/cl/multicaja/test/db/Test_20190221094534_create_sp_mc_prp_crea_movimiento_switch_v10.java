package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Test_20190221094534_create_sp_mc_prp_crea_movimiento_switch_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".prp_crea_movimiento_switch_v10";

  public static Map<String, Object> insertSwitchMovement(Long fileId, String multicajaId, Long clientId, Long idMulticajaRef, BigDecimal amount, Timestamp trxDate) throws SQLException {
    Object[] params = {
      fileId != null ? fileId : new NullParam(Types.BIGINT),
      multicajaId != null ? multicajaId : new NullParam(Types.VARCHAR),
      clientId != null ? clientId : new NullParam(Types.BIGINT),
      idMulticajaRef != null ? idMulticajaRef : new NullParam(Types.BIGINT),
      amount != null ? amount : new NullParam(Types.NUMERIC),
      trxDate != null ? trxDate : new NullParam(Types.TIMESTAMP),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_r_id_hist", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Map<String, Object> map = new HashMap<>();
    map.put("id", numberUtils.toLong(resp.get("_r_id")));
    map.put("id_archivo", fileId);
    map.put("id_multicaja", multicajaId);
    map.put("id_cliente", clientId);
    map.put("id_multicaja_ref", idMulticajaRef);
    map.put("monto", amount);
    map.put("fecha_trx", trxDate);
    map.put("id_hist", resp.get("_r_id_hist"));
    map.put("_error_code", resp.get("_error_code"));
    map.put("_error_msg", resp.get("_error_msg"));
    return map;
  }

  @Test
  public void insertSwitchMovement_allOK() throws SQLException {
    Map<String, Object> fileData = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre.txt", "SWITCH", "retiros_rechazados", "OK");

    // Insertar movimiento switch
    Long fileId = numberUtils.toLong(fileData.get("_r_id"));
    String multicajaId = "multiId38";
    Long clienId = 55L;
    Long idMulticajaRef = 88L;
    BigDecimal amount = new BigDecimal(500);
    Timestamp todayTimestamp = Timestamp.valueOf(LocalDateTime.now());
    Map<String, Object> switchMovement = insertSwitchMovement(fileId, multicajaId, clienId, idMulticajaRef, amount, todayTimestamp);

    // Buscar movmiento para chequear que se guardo correctamente
    Map<String, Object> storedSwitchMovement = getSwitchMovement("prp_movimiento_switch", numberUtils.toLong(switchMovement.get("id")));
    Assert.assertNotNull("Debe existir", storedSwitchMovement);
    Assert.assertEquals("Debe tener mismo id", numberUtils.toLong(switchMovement.get("id")), numberUtils.toLong(storedSwitchMovement.get("id")));
    Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(storedSwitchMovement.get("id_archivo")));
    Assert.assertEquals("Debe tener mismo multicaja id", multicajaId, storedSwitchMovement.get("id_multicaja").toString());
    Assert.assertEquals("Debe tener mismo cliente_id", clienId, numberUtils.toLong(storedSwitchMovement.get("id_cliente")));
    Assert.assertEquals("Debe tener mismo id_multicaja_ref", idMulticajaRef, numberUtils.toLong(storedSwitchMovement.get("id_multicaja_ref")));
    Assert.assertEquals("Debe tener mismo monto", amount.stripTrailingZeros(), ((BigDecimal)storedSwitchMovement.get("monto")).stripTrailingZeros());
    Assert.assertEquals("Debe tener misma fecha", todayTimestamp, (Timestamp)storedSwitchMovement.get("fecha_trx"));

    // Revisar que tb se guardo en la tabla hist
    Map<String, Object> storedSwitchHistMovement = getSwitchMovement("prp_movimiento_switch_hist", numberUtils.toLong(switchMovement.get("id_hist")));
    Assert.assertNotNull("Debe existir", storedSwitchHistMovement);
    Assert.assertEquals("Debe tener mismo id", numberUtils.toLong(switchMovement.get("id_hist")), numberUtils.toLong(storedSwitchHistMovement.get("id")));
    Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(storedSwitchHistMovement.get("id_archivo")));
    Assert.assertEquals("Debe tener mismo multicaja id", multicajaId, storedSwitchHistMovement.get("id_multicaja").toString());
    Assert.assertEquals("Debe tener mismo cliente_id", clienId, numberUtils.toLong(storedSwitchHistMovement.get("id_cliente")));
    Assert.assertEquals("Debe tener mismo id_multicaja_ref", idMulticajaRef, numberUtils.toLong(storedSwitchHistMovement.get("id_multicaja_ref")));
    Assert.assertEquals("Debe tener mismo monto", amount.stripTrailingZeros(), ((BigDecimal)storedSwitchHistMovement.get("monto")).stripTrailingZeros());
    Assert.assertEquals("Debe tener misma fecha", todayTimestamp, (Timestamp)storedSwitchHistMovement.get("fecha_trx"));
  }

  @Test
  public void insertSwitchMovement_fileNull() throws SQLException {
    Map<String, Object> fileData = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre.txt", "SWITCH", "retiros_rechazados", "OK");

    // Insertar movimiento switch
    Map<String, Object> switchMovement = insertSwitchMovement(null, "multiId38", 55L, 88L, new BigDecimal(500), Timestamp.valueOf(LocalDateTime.now()));

    Assert.assertNotEquals("Debe tener codigo de error != 0", "0", switchMovement.get("_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", switchMovement.get("_error_msg").toString());
  }

  @Test
  public void insertSwitchMovement_multicajaIdNull() throws SQLException {
    Map<String, Object> fileData = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre.txt", "SWITCH", "retiros_rechazados", "OK");

    // Insertar movimiento switch
    Map<String, Object> switchMovement = insertSwitchMovement(numberUtils.toLong(fileData.get("_r_id")), null, 55L, 88L, new BigDecimal(500), Timestamp.valueOf(LocalDateTime.now()));

    Assert.assertNotEquals("Debe tener codigo de error != 0", "0", switchMovement.get("_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", switchMovement.get("_error_msg").toString());
  }

  @Test
  public void insertSwitchMovement_clienteIdNull() throws SQLException {
    Map<String, Object> fileData = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre.txt", "SWITCH", "retiros_rechazados", "OK");

    // Insertar movimiento switch
    Map<String, Object> switchMovement = insertSwitchMovement(numberUtils.toLong(fileData.get("_r_id")), "multiId38", null, 88L, new BigDecimal(500), Timestamp.valueOf(LocalDateTime.now()));

    Assert.assertNotEquals("Debe tener codigo de error != 0", "0", switchMovement.get("_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", switchMovement.get("_error_msg").toString());
  }

  @Test
  public void insertSwitchMovement_idMulticajaRefNull() throws SQLException {
    Map<String, Object> fileData = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre.txt", "SWITCH", "retiros_rechazados", "OK");

    // Insertar movimiento switch
    Long fileId = numberUtils.toLong(fileData.get("_r_id"));
    String multicajaId = "multiId38";
    Long clienId = 55L;
    BigDecimal amount = new BigDecimal(500);
    Timestamp todayTimestamp = Timestamp.valueOf(LocalDateTime.now());
    Map<String, Object> switchMovement = insertSwitchMovement(fileId, multicajaId, clienId, null, amount, todayTimestamp);

    // Buscar movmiento para chequear que se guardo correctamente
    Map<String, Object> storedSwitchMovement = getSwitchMovement("prp_movimiento_switch", numberUtils.toLong(switchMovement.get("id")));
    Assert.assertNotNull("Debe existir", storedSwitchMovement);
    Assert.assertEquals("Debe tener mismo id", numberUtils.toLong(switchMovement.get("id")), numberUtils.toLong(storedSwitchMovement.get("id")));
    Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(storedSwitchMovement.get("id_archivo")));
    Assert.assertEquals("Debe tener mismo multicaja id", multicajaId, storedSwitchMovement.get("id_multicaja").toString());
    Assert.assertEquals("Debe tener mismo cliente_id", clienId, numberUtils.toLong(storedSwitchMovement.get("id_cliente")));
    Assert.assertNull("Debe tener id_multicaja_ref null", storedSwitchMovement.get("id_multicaja_ref"));
    Assert.assertEquals("Debe tener mismo monto", amount.stripTrailingZeros(), ((BigDecimal)storedSwitchMovement.get("monto")).stripTrailingZeros());
    Assert.assertEquals("Debe tener misma fecha", todayTimestamp, (Timestamp)storedSwitchMovement.get("fecha_trx"));

    // Revisar que tb se guardo en la tabla hist
    Map<String, Object> storedSwitchHistMovement = getSwitchMovement("prp_movimiento_switch_hist", numberUtils.toLong(switchMovement.get("id_hist")));
    Assert.assertNotNull("Debe existir", storedSwitchHistMovement);
    Assert.assertEquals("Debe tener mismo id", numberUtils.toLong(switchMovement.get("id_hist")), numberUtils.toLong(storedSwitchHistMovement.get("id")));
    Assert.assertEquals("Debe tener mismo archivo_id", fileId, numberUtils.toLong(storedSwitchHistMovement.get("id_archivo")));
    Assert.assertEquals("Debe tener mismo multicaja id", multicajaId, storedSwitchHistMovement.get("id_multicaja").toString());
    Assert.assertEquals("Debe tener mismo cliente_id", clienId, numberUtils.toLong(storedSwitchHistMovement.get("id_cliente")));
    Assert.assertNull("Debe tener id_multicaja_ref null", storedSwitchHistMovement.get("id_multicaja_ref"));
    Assert.assertEquals("Debe tener mismo monto", amount.stripTrailingZeros(), ((BigDecimal)storedSwitchHistMovement.get("monto")).stripTrailingZeros());
    Assert.assertEquals("Debe tener misma fecha", todayTimestamp, (Timestamp)storedSwitchHistMovement.get("fecha_trx"));
  }

  @Test
  public void insertSwitchMovement_amountNull() throws SQLException {
    Map<String, Object> fileData = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre.txt", "SWITCH", "retiros_rechazados", "OK");

    // Insertar movimiento switch
    Map<String, Object> switchMovement = insertSwitchMovement(numberUtils.toLong(fileData.get("_r_id")), "multiId38", 55L, 88L, null, Timestamp.valueOf(LocalDateTime.now()));

    Assert.assertNotEquals("Debe tener codigo de error != 0", "0", switchMovement.get("_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", switchMovement.get("_error_msg").toString());
  }

  @Test
  public void insertSwitchMovement_dateNull() throws SQLException {
    Map<String, Object> fileData = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog("nombre.txt", "SWITCH", "retiros_rechazados", "OK");

    // Insertar movimiento switch
    Map<String, Object> switchMovement = insertSwitchMovement(numberUtils.toLong(fileData.get("_r_id")), "multiId38", 55L, 88L, new BigDecimal(500), null);

    Assert.assertNotEquals("Debe tener codigo de error != 0", "0", switchMovement.get("_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", switchMovement.get("_error_msg").toString());
  }

  private Map<String, Object> getSwitchMovement(String tableName, Long id) {
    return dbUtils.getJdbcTemplate().queryForList(
      " SELECT " +
        "     id, " +
        "     id_archivo, " +
        "     id_multicaja, " +
        "     id_cliente, " +
        "     id_multicaja_ref, " +
        "     monto, " +
        "     fecha_trx " +
        " FROM " +
        "   " + SCHEMA + "." + tableName +
        " WHERE " +
        " id = " + id
    ).get(0);
  }
}
