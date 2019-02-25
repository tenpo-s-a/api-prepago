package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import com.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

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
    List<McRedReconciliationFileDetail> lstMcRedReconciliationFileDetails = getCsvData(fileName, inputStream);
    if (fileName.contains("rendicion_cargas_mcpsa_mc")) {
      log.info("IN rendicion_cargas_mcpsa_mc");
      conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 26, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_cargas_mcpsa_mc");
    }
    else if (fileName.contains("rendicion_cargas_rechazadas_mcpsa_mc")) {
      //conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, fileName);
    }
    else if (fileName.contains("rendicion_cargas_reversadas_mcpsa_mc")) {
      log.info("IN rendicion_cargas_reversadas_mcpsa_mc");
      conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 37, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_cargas_reversadas_mcpsa_mc");
    }
    else if (fileName.contains("rendicion_retiros_mcpsa_mc")) {
      log.info("IN rendicion_retiros_mcpsa_mc");
      conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 27, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_retiros_mcpsa_mc");
    }
    else if (fileName.contains("rendicion_retiros_rechazados_mcpsa_mc")) {
      //conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, fileName);
    }
    else if (fileName.contains("rendicion_retiros_reversados_mcpsa_mc")) {
      log.info("IN rendicion_retiros_reversados_mcpsa_mc");
      conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA, fileName);
      StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 38, dateFormat);
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA, ReconciliationStatusType.NOT_RECONCILED);
      log.info("OUT rendicion_retiros_reversados_mcpsa_mc");
    }
  }

  private void conciliation(List<McRedReconciliationFileDetail> lstMcRedReconciliationFileDetails, PrepaidMovementType movementType, IndicadorNormalCorrector indicadorNormalCorrector, String fileName) throws Exception{
    try {
      for (McRedReconciliationFileDetail recTmp : lstMcRedReconciliationFileDetails) {
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
  private List<McRedReconciliationFileDetail> getCsvData(String fileName, InputStream is) throws Exception {
    List<McRedReconciliationFileDetail> lstMcRedReconciliationFileDetail;
    log.info("IN");
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      CSVReader csvReader = new CSVReader(reader,';');
      csvReader.readNext();
      String[] record;
      lstMcRedReconciliationFileDetail = new ArrayList<>();

      while ((record = csvReader.readNext()) != null) {
        log.debug(Arrays.toString(record));
        McRedReconciliationFileDetail mcRedReconciliationFileDetail = new McRedReconciliationFileDetail();
        mcRedReconciliationFileDetail.setMcCode(record[0]);
        mcRedReconciliationFileDetail.setDateTrx(record[1]);
        mcRedReconciliationFileDetail.setClientId(Long.valueOf(record[2]));
        mcRedReconciliationFileDetail.setAmount(getNumberUtils().toBigDecimal(record[3]));
        if(!fileName.contains("reversa")) {
          mcRedReconciliationFileDetail.setExternalId(Long.valueOf(record[4]));
        }
        lstMcRedReconciliationFileDetail.add(mcRedReconciliationFileDetail);
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
    return lstMcRedReconciliationFileDetail;
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
}
