package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.Times;

import javax.ejb.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class McRedReconciliationEJBBean10 extends PrepaidBaseEJBBean10 implements McRedReconciliationEJB10 {

  private static Log log = LogFactory.getLog(McRedReconciliationEJBBean10.class);

  private static final String dateFormat = "yyyyMMdd";
  private static final ZoneId switchZone = ZoneId.of("America/Santiago");

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  @Override
  public void processFile(InputStream inputStream, String fileName) throws Exception {
    List<ReconciliationMcRed10> lstReconciliationMcRed10s = getCsvData(fileName, inputStream);
    if (fileName.contains("rendicion_cargas_mcpsa_mc")) {
      log.info("IN rendicion_cargas_mcpsa_mc");
      conciliation(lstReconciliationMcRed10s, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 26, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_cargas_mcpsa_mc");
    }
    else if (fileName.contains("rendicion_cargas_rechazadas_mcpsa_mc")) {
      //conciliation(lstReconciliationMcRed10s, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, fileName);
    }
    else if (fileName.contains("rendicion_cargas_reversadas_mcpsa_mc")) {
      log.info("IN rendicion_cargas_reversadas_mcpsa_mc");
      conciliation(lstReconciliationMcRed10s, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 37, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_cargas_reversadas_mcpsa_mc");
    }
    else if (fileName.contains("rendicion_retiros_mcpsa_mc")) {
      log.info("IN rendicion_retiros_mcpsa_mc");
      conciliation(lstReconciliationMcRed10s, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 27, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_retiros_mcpsa_mc");
    }
    else if (fileName.contains("rendicion_retiros_rechazados_mcpsa_mc")) {
      //conciliation(lstReconciliationMcRed10s, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, fileName);
    }
    else if (fileName.contains("rendicion_retiros_reversados_mcpsa_mc")) {
      log.info("IN rendicion_retiros_reversados_mcpsa_mc");
      conciliation(lstReconciliationMcRed10s, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 38, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_retiros_reversados_mcpsa_mc");
    }
  }

  private void conciliation(List<ReconciliationMcRed10> lstReconciliationMcRed10s, PrepaidMovementType movementType, IndicadorNormalCorrector indicadorNormalCorrector, String fileName) throws Exception{
    try {
      for (ReconciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
        PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode(),movementType,indicadorNormalCorrector);
        log.info(prepaidMovement10);
        if (prepaidMovement10 == null)
        {
          log.info("Movimiento no encontrado, no conciliado");
          // Construyendo un Id.
          String researchId = "ExtId:[";
          if (recTmp.getExternalId() != null) {
            researchId += recTmp.getExternalId().toString();
          } else {
            researchId += "NoExternalId";
          }
          researchId += "]-";
          researchId += "McCode:[" + recTmp.getMcCode() + "]";

          getPrepaidMovementEJBBean10().createMovementResearch(null, researchId, ReconciliationOriginType.SWITCH, fileName);
          continue;
        }
        else
          {
            if (recTmp.getAmount().compareTo(prepaidMovement10.getMonto()) != 0) {
              log.error("No conciliado");
              getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, prepaidMovement10.getId(), ReconciliationStatusType.NOT_RECONCILED);
              continue;
            }
            else {
              log.info("Conciliado");
              getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, prepaidMovement10.getId(), ReconciliationStatusType.RECONCILED);
            }

        }
      }
    }catch (Exception e){
      e.printStackTrace();
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
  }


  /**
   * Lee los archivos CSV
   * @param fileName
   * @param is
   * @return
   */
  private List<ReconciliationMcRed10> getCsvData(String fileName, InputStream is) throws Exception {
    List<ReconciliationMcRed10> lstReconciliationMcRed10;
    log.info("IN");
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      CSVReader csvReader = new CSVReader(reader,';');
      csvReader.readNext();
      String[] record;
      lstReconciliationMcRed10 = new ArrayList<>();

      while ((record = csvReader.readNext()) != null) {
        log.debug(Arrays.toString(record));
        ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
        reconciliationMcRed10.setMcCode(record[0]);
        reconciliationMcRed10.setDateTrx(record[1]);
        reconciliationMcRed10.setClientId(Long.valueOf(record[2]));
        reconciliationMcRed10.setAmount(getNumberUtils().toBigDecimal(record[3]));
        if(!fileName.contains("reversa")) {
          reconciliationMcRed10.setExternalId(Long.valueOf(record[4]));
        }
        lstReconciliationMcRed10.add(reconciliationMcRed10);
      }
      is.close();
    }catch (Exception e){
      is.close();
      log.error("Exception: "+e);
      e.printStackTrace();
      System.out.println("Exception: "+e);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
    log.info("OUT");
    return lstReconciliationMcRed10;
  }

  /**
   * Usa el nombre del archivo para extraer dos timestamps UTC, de comienzo y fin.
   *
   * @param fileName el nombre del archivo
   * @param dateIndex donde comienza la fecha en el nombre de archivo
   * @param dateFormat en que formato viene la fecha
   */
  private StringDateInterval convertFileNameToUTCInterval(String fileName, int dateIndex, String dateFormat) {
    // Extrae la fecha del nombre de archivo
    String fileDate = fileName.substring(dateIndex, dateIndex + dateFormat.length());

    // Usar el dia completo para calcular el comienzo y fin del intervalo
    String beginDate = convertStringDateToYesterdayAtUTC(fileDate, dateFormat, 0, 0, 0, 0);
    String endDate = convertStringDateToYesterdayAtUTC(fileDate, dateFormat, 23, 59, 59, 999);

    StringDateInterval result = new StringDateInterval();
    result.beginDate = beginDate;
    result.endDate = endDate;
    return result;
  }

  /**
   * Recibe una timestamp en string, en cierto horario. La convierte a ayer y en horario UTC.
   *
   * @param date
   * @param format
   * @param hour
   * @param minute
   * @param second
   * @param nano
   * @return Una timestamp como string.
   */
  private String convertStringDateToYesterdayAtUTC(String date, String format, int hour, int minute, int second, int nano) {
    LocalDate localDate = DateUtils.getInstance().dateStringToLocalDate(date, format); // De String a local date.
    localDate = localDate.minusDays(1); // Ir a ayer
    LocalDateTime localDateTime = localDate.atTime(hour, minute, second, nano); // Agregarle las horas y minutos
    ZonedDateTime zonedDateTime = localDateTime.atZone(switchZone); // Marcarla como que pertenece al horario del switch
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")); // Convertir a UTC
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    return zonedDateTime.format(formatter);
  }

  class StringDateInterval {
    private String beginDate;
    private String endDate;
  }

  @Override
  public ReconciliationMcRed10 addFileMovement(Map<String,Object> header, ReconciliationMcRed10 newSwitchMovement) throws Exception {
    if(newSwitchMovement == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement"));
    }

    if(newSwitchMovement.getFileId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.fileId"));
    }

    if(newSwitchMovement.getMcCode() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.McCode"));
    }

    if(newSwitchMovement.getClientId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.clientId"));
    }

    if(newSwitchMovement.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.amount"));
    }

    if(newSwitchMovement.getDateTrx() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.dateTrx"));
    }

    // La fecha viene en string hora chile, hay que convertirla a timestamp hora utc
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    LocalDateTime dateTime = LocalDateTime.parse(newSwitchMovement.getDateTrx(), formatter);
    ZonedDateTime chileTime = dateTime.atZone(ZoneId.of("America/Santiago"));
    ZonedDateTime utcTime = chileTime.withZoneSameInstant(ZoneId.of("UTC"));
    Timestamp fechaTrxUTC = Timestamp.valueOf(utcTime.toLocalDateTime());

    Object[] params = {
      new InParam(newSwitchMovement.getFileId(), Types.BIGINT),
      new InParam(newSwitchMovement.getMcCode(), Types.VARCHAR),
      new InParam(newSwitchMovement.getClientId(), Types.BIGINT),
      newSwitchMovement.getExternalId() != null ? new InParam(newSwitchMovement.getExternalId(), Types.BIGINT) : new NullParam(Types.BIGINT),
      new InParam(newSwitchMovement.getAmount(), Types.NUMERIC),
      new InParam(fechaTrxUTC, Types.TIMESTAMP),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_r_id_int", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".prp_crea_movimiento_switch_v10", params);

    if("0".equals(resp.get("_error_code"))) {
      newSwitchMovement.setId(getNumberUtils().toLong(resp.get("_r_id")));
      return newSwitchMovement;
    } else {
      log.error("addPrepaidMovement resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public List<ReconciliationMcRed10> getFileMovements(Map<String,Object> header, Long fileId, Long movementId, String mcId) throws Exception {
    Object[] params = {
      new InParam("prp_movimiento_switch", Types.VARCHAR),
      movementId != null ? new InParam(movementId, Types.BIGINT) : new NullParam(Types.BIGINT),
      fileId != null ? new InParam(fileId, Types.BIGINT) : new NullParam(Types.BIGINT),
      mcId != null ? new InParam(mcId, Types.VARCHAR) : new NullParam(Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      ReconciliationMcRed10 reconciliationMcRed10 = new ReconciliationMcRed10();
      reconciliationMcRed10.setId(getNumberUtils().toLong(row.get("_id")));
      reconciliationMcRed10.setFileId(getNumberUtils().toLong(row.get("_id_archivo")));
      reconciliationMcRed10.setMcCode(row.get("_id_multicaja").toString());
      reconciliationMcRed10.setClientId(getNumberUtils().toLong(row.get("_id_cliente")));
      reconciliationMcRed10.setExternalId(getNumberUtils().toLong(row.get("_id_multicaja_ref")));
      reconciliationMcRed10.setAmount(getNumberUtils().toBigDecimal(row.get("_monto")));

      Timestamp storedTimestamp = (Timestamp) row.get("_fecha_trx");
      LocalDateTime storedLocalDatetime = storedTimestamp.toLocalDateTime();
      ZonedDateTime utcTime = storedLocalDatetime.atZone(ZoneId.of("UTC"));
      ZonedDateTime chileTime = utcTime.withZoneSameInstant(ZoneId.of("America/Santiago"));
      String chileFormated = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(chileTime);
      reconciliationMcRed10.setDateTrx(chileFormated);

      return reconciliationMcRed10;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_movimientos_switch_v10", rm,params);
    return (List)resp.get("result");
  }
}
