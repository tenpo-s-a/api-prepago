package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.model.v10.ReconciliationMcRed10;
import org.apache.derby.client.am.DateTime;
import org.junit.Assert;
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

    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setAmount(new BigDecimal(1000));
    reconciliationMcRed10.setClientId(49L);
    reconciliationMcRed10.setDateTrx("03-02-1998 14:23");
    reconciliationMcRed10.setExternalId(88L);
    reconciliationMcRed10.setMcCode("MC23");
    reconciliationMcRed10.setFileId(fileId);

    ReconciliationMcRed10 insertedMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);

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
    String chileFormated = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(chileTime);
    Assert.assertEquals("Debe tener la misma fecha de transaccion", reconciliationMcRed10.getDateTrx(), chileFormated);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_movmentNull() throws Exception {
    getMcRedReconciliationEJBBean10().addFileMovement(null, null);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_fileIdNull() throws Exception {
    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setFileId(null);
    reconciliationMcRed10.setMcCode("MC23");
    reconciliationMcRed10.setClientId(49L);
    reconciliationMcRed10.setExternalId(88L);
    reconciliationMcRed10.setAmount(new BigDecimal(1000));
    reconciliationMcRed10.setDateTrx("03-02-1998 14:23");

    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_mcCodeNull() throws Exception {
    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setFileId(23L);
    reconciliationMcRed10.setMcCode(null);
    reconciliationMcRed10.setClientId(49L);
    reconciliationMcRed10.setExternalId(88L);
    reconciliationMcRed10.setAmount(new BigDecimal(1000));
    reconciliationMcRed10.setDateTrx("03-02-1998 14:23");

    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_clientIdNull() throws Exception {
    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setFileId(23L);
    reconciliationMcRed10.setMcCode("MC23");
    reconciliationMcRed10.setClientId(null);
    reconciliationMcRed10.setExternalId(88L);
    reconciliationMcRed10.setAmount(new BigDecimal(1000));
    reconciliationMcRed10.setDateTrx("03-02-1998 14:23");

    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test
  public void addFileMovement_externaldNull_shouldInsertOk() throws Exception {
    Map<String, Object> fileMap = insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId = numberUtils.toLong(fileMap.get("_r_id"));

    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setAmount(new BigDecimal(1000));
    reconciliationMcRed10.setClientId(49L);
    reconciliationMcRed10.setDateTrx("03-02-1998 14:23");
    reconciliationMcRed10.setExternalId(null);
    reconciliationMcRed10.setMcCode("MC23");
    reconciliationMcRed10.setFileId(fileId);

    ReconciliationMcRed10 insertedMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);

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
    String chileFormated = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(chileTime);
    Assert.assertEquals("Debe tener la misma fecha de transaccion", reconciliationMcRed10.getDateTrx(), chileFormated);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_amountNull() throws Exception {
    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setFileId(23L);
    reconciliationMcRed10.setMcCode("MC23");
    reconciliationMcRed10.setClientId(49L);
    reconciliationMcRed10.setExternalId(88L);
    reconciliationMcRed10.setAmount(null);
    reconciliationMcRed10.setDateTrx("03-02-1998 14:23");

    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test(expected = BadRequestException.class)
  public void addFileMovement_dateTrxNull() throws Exception {
    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setFileId(23L);
    reconciliationMcRed10.setMcCode("MC23");
    reconciliationMcRed10.setClientId(49L);
    reconciliationMcRed10.setExternalId(88L);
    reconciliationMcRed10.setAmount(new BigDecimal(1000));
    reconciliationMcRed10.setDateTrx(null);

    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  @Test(expected = DateTimeParseException.class)
  public void addFileMovement_dateTrxWrongFormat() throws Exception {
    ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
    reconciliationMcRed10.setFileId(23L);
    reconciliationMcRed10.setMcCode("MC23");
    reconciliationMcRed10.setClientId(49L);
    reconciliationMcRed10.setExternalId(88L);
    reconciliationMcRed10.setAmount(new BigDecimal(1000));
    reconciliationMcRed10.setDateTrx("14:23 03-02-1998"); // Mal formato de fecha

    getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10);
  }

  public static Map<String, Object> insertArchivoReconcialicionLog(String nombreArchivo, String proceso, String tipo, String status) throws SQLException {
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
