package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import org.apache.commons.net.ntp.TimeStamp;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class Test_McRedReconciliationEJB10_addFileMovement extends TestBaseUnit {

  @Test
  public void addFileMovement_allOk() throws Exception {
    Map<String, Object> fileMap = insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId = numberUtils.toLong(fileMap.get("_r_id"));

    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(fileId, "MC23", 49L, 88L, new BigDecimal(1000), new Timestamp(System.currentTimeMillis()));
    McRedReconciliationFileDetail insertedMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);

    // Buscar movmiento para chequear que se guardo correctamente
    Map<String, Object> storedSwitchMovement = getSwitchMovement("prp_movimiento_switch", insertedMovement.getId());
    Assert.assertNotNull("Debe existir", storedSwitchMovement);
    Assert.assertEquals("Debe tener mismo id", insertedMovement.getId(), numberUtils.toLong(storedSwitchMovement.get("id")));
    Assert.assertEquals("Debe tener mismo archivo_id", insertedMovement.getFileId(), numberUtils.toLong(storedSwitchMovement.get("id_archivo")));
    Assert.assertEquals("Debe tener mismo multicaja id", insertedMovement.getMcCode(), storedSwitchMovement.get("id_multicaja").toString());
    Assert.assertEquals("Debe tener mismo cliente_id", insertedMovement.getClientId(), numberUtils.toLong(storedSwitchMovement.get("id_cliente")));
    Assert.assertEquals("Debe tener mismo id_multicaja_ref", insertedMovement.getExternalId(), numberUtils.toLong(storedSwitchMovement.get("id_multicaja_ref")));
    Assert.assertEquals("Debe tener mismo monto", insertedMovement.getAmount().stripTrailingZeros(), ((BigDecimal)storedSwitchMovement.get("monto")).stripTrailingZeros());

    Timestamp storedTimestamp = (Timestamp)storedSwitchMovement.get("fecha_trx");
    LocalDateTime storedLocalDatetime = storedTimestamp.toLocalDateTime();
    ZonedDateTime utcTime = storedLocalDatetime.atZone(ZoneId.of("UTC"));
    ZonedDateTime chileTime = utcTime.withZoneSameInstant(ZoneId.of("America/Santiago"));
    Assert.assertEquals("Debe tener la misma fecha de transaccion", insertedMovement.getDateTrx(), Timestamp.from(chileTime.toInstant()));
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_movmentNull() throws Exception {
    getMcRedReconciliationEJBBean10().addFileMovement(null, null);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_fileIdNull() throws Exception {
    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(null, "MC23", 49L, 88L, new BigDecimal(1000), new Timestamp(System.currentTimeMillis()));
    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_mcCodeNull() throws Exception {
    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(23L, null, 49L, 88L, new BigDecimal(1000), new Timestamp(System.currentTimeMillis()));
    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_clientIdNull() throws Exception {
    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(23L, "MC23", null, 88L, new BigDecimal(1000), new Timestamp(System.currentTimeMillis()));
    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test
  public void addFileMovement_externaldNull_shouldInsertOk() throws Exception {
    Map<String, Object> fileMap = insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId = numberUtils.toLong(fileMap.get("_r_id"));
    Timestamp fechaTrx = new Timestamp(System.currentTimeMillis());
    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(fileId, "MC23", 49L, null, new BigDecimal(1000), fechaTrx);
    McRedReconciliationFileDetail insertedMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);

    // Buscar movmiento para chequear que se guardo correctamente
    Map<String, Object> storedSwitchMovement = getSwitchMovement("prp_movimiento_switch", insertedMovement.getId());
    Assert.assertNotNull("Debe existir", storedSwitchMovement);
    Assert.assertEquals("Debe tener mismo id", insertedMovement.getId(), numberUtils.toLong(storedSwitchMovement.get("id")));
    Assert.assertEquals("Debe tener mismo archivo_id", insertedMovement.getFileId(), numberUtils.toLong(storedSwitchMovement.get("id_archivo")));
    Assert.assertEquals("Debe tener mismo multicaja id", insertedMovement.getMcCode(), storedSwitchMovement.get("id_multicaja").toString());
    Assert.assertEquals("Debe tener mismo cliente_id", insertedMovement.getClientId(), numberUtils.toLong(storedSwitchMovement.get("id_cliente")));
    Assert.assertEquals("Debe tener mismo id_multicaja_ref", insertedMovement.getExternalId(), storedSwitchMovement.get("id_multicaja_ref"));
    Assert.assertEquals("Debe tener mismo monto", insertedMovement.getAmount().stripTrailingZeros(), ((BigDecimal)storedSwitchMovement.get("monto")).stripTrailingZeros());

    Timestamp storedTimestamp = (Timestamp)storedSwitchMovement.get("fecha_trx");
    LocalDateTime storedLocalDatetime = storedTimestamp.toLocalDateTime();
    ZonedDateTime utcTime = storedLocalDatetime.atZone(ZoneId.of("UTC"));
    ZonedDateTime chileTime = utcTime.withZoneSameInstant(ZoneId.of("America/Santiago"));
    Assert.assertEquals("Debe tener la misma fecha de transaccion", insertedMovement.getDateTrx(), Timestamp.from(chileTime.toInstant()));
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_amountNull() throws Exception {
    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(23L, "MC23", 49L, 88L, null, new Timestamp(System.currentTimeMillis()));
    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_dateTrxNull() throws Exception {
    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(23L, "MC23", 49L, 88L, new BigDecimal(1000), null);
    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  // Se comenta ya que la fecha siempre ira en Timestamp
  @Ignore
  @Test(expected = DateTimeParseException.class)
  public void addFileMovement_dateTrxWrongFormat() throws Exception {
    McRedReconciliationFileDetail reconciliationMcRed10 = buildReconciliationMcRed10(23L, "MC23", 49L, 88L, new BigDecimal(1000), null);
    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  static public McRedReconciliationFileDetail buildReconciliationMcRed10(Long fileId, String mcCode, Long clientId, Long externalId, BigDecimal amount, Timestamp dateTrx) {
    McRedReconciliationFileDetail reconciliationMcRed10 = new McRedReconciliationFileDetail();
    reconciliationMcRed10.setFileId(fileId);
    reconciliationMcRed10.setMcCode(mcCode);
    reconciliationMcRed10.setClientId(clientId);
    reconciliationMcRed10.setExternalId(externalId);
    reconciliationMcRed10.setAmount(amount);
    reconciliationMcRed10.setDateTrx(dateTrx);
    return reconciliationMcRed10;
  }

  static public Map<String, Object> insertArchivoReconcialicionLog(String nombreArchivo, String proceso, String tipo, String status) throws SQLException {
    Object[] params = {
      nombreArchivo != null ? nombreArchivo : new NullParam(Types.VARCHAR),
      proceso != null ? proceso : new NullParam(Types.VARCHAR),
      tipo != null ? tipo : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return getDbUtils().execute(getSchema() + ".prp_inserta_archivo_conciliacion", params);
  }

  private Map<String, Object> getSwitchMovement(String tableName, Long id) {
    return getDbUtils().getJdbcTemplate().queryForList(
      " SELECT " +
        "     id, " +
        "     id_archivo, " +
        "     id_multicaja, " +
        "     id_cliente, " +
        "     id_multicaja_ref, " +
        "     monto, " +
        "     fecha_trx " +
        " FROM " +
        "   " + getSchema() + "." + tableName +
        " WHERE " +
        " id = " + id
    ).get(0);
  }
}
