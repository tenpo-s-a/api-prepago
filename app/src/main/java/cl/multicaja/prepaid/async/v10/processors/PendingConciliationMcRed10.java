package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.util.DateUtil;
import com.opencsv.CSVReader;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.Local;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

public class PendingConciliationMcRed10 extends BaseProcessor10  {

  private static Log log = LogFactory.getLog(PendingConciliationMcRed10.class);

  private static final String dateFormat = "yyyyMMdd";
  private static final ZoneId switchZone = ZoneId.of("America/Santiago");

  public PendingConciliationMcRed10(BaseRoute10 route) {
    super(route);
  }

  public Processor processReconciliationsMcRed(){
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info("Proces Files Multicaja Red");
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        log.error(exchange.getIn().getBody());
        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info("Filename: "+fileName);
        List<ReconciliationMcRed10> lstReconciliationMcRed10s = getCsvData(fileName, inputStream);
        if (fileName.contains("rendicion_cargas_mcpsa_mc")) {
          log.info("IN rendicion_cargas_mcpsa_mc");
          conciliation(lstReconciliationMcRed10s, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, fileName);
          StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 26, dateFormat);
          getRoute().getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, ConciliationStatusType.NOT_RECONCILED);
          log.info("OUT rendicion_cargas_mcpsa_mc");
        } else if (fileName.contains("rendicion_cargas_rechazadas_mcpsa_mc")) {
          conciliation(lstReconciliationMcRed10s, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, fileName);
        } else if (fileName.contains("rendicion_cargas_reversadas_mcpsa_mc")) {
          conciliation(lstReconciliationMcRed10s, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA, fileName);
          StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 37, dateFormat);
          getRoute().getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA, ConciliationStatusType.NOT_RECONCILED);
        } else if (fileName.contains("rendicion_retiros_mcpsa_mc")) {
          conciliation(lstReconciliationMcRed10s, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, fileName);
          StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 27, dateFormat);
          getRoute().getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, ConciliationStatusType.NOT_RECONCILED);
        } else if (fileName.contains("rendicion_retiros_rechazados_mcpsa_mc")) {
          conciliation(lstReconciliationMcRed10s, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL, fileName);
        } else if (fileName.contains("rendicion_retiros_reversados_mcpsa_mc")) {
          conciliation(lstReconciliationMcRed10s, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA, fileName);
          StringDateInterval utcInterval = convertFileNameToUTCInterval(fileName, 38, dateFormat);
          getRoute().getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, utcInterval.beginDate, utcInterval.endDate, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA, ConciliationStatusType.NOT_RECONCILED);
        }
      }
    };
  }
<<<<<<< HEAD
  private void conciliation(List<ReconciliationMcRed10> lstReconciliationMcRed10s, PrepaidMovementType movementType, IndicadorNormalCorrector indicadorNormalCorrector) throws Exception{
=======

  private void conciliation(List<ConciliationMcRed10> lstReconciliationMcRed10s, PrepaidMovementType movementType, IndicadorNormalCorrector indicadorNormalCorrector, String fileName) throws Exception{
>>>>>>> master
     try {
       for (ReconciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
         PrepaidMovement10 prepaidMovement10 = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode(),movementType,indicadorNormalCorrector);
         log.info(prepaidMovement10);
         if (prepaidMovement10 == null) {
           log.info("No conciliado");

           // Construyendo un Id.
           String researchId = "ExtId:[";
           if (recTmp.getExternalId() != null) {
             researchId += recTmp.getExternalId().toString();
           } else {
             researchId += "NoExternalId";
           }
           researchId += "]-";
           researchId += "McCode:[" + recTmp.getMcCode() + "]";

           getRoute().getPrepaidMovementEJBBean10().createMovementResearch(null, researchId, ConciliationOriginType.SWITCH, fileName);
           continue;
         }
         else {
            if (!recTmp.getAmount().equals(prepaidMovement10.getMonto().longValue())) {
              log.error("No conciliado");
              getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, prepaidMovement10.getId(), ReconciliationStatusType.NOT_RECONCILED);
              continue;
            }
            log.info("Conciliado");
            getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, prepaidMovement10.getId(), ReconciliationStatusType.RECONCILED);
         }
       }
     }catch (Exception e){
       throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
     }
  }


  /**
   * Lee los archivos CSV
   * @param fileName
   * @param is
   * @return
   */
  public static List<ReconciliationMcRed10> getCsvData(String fileName, InputStream is) throws Exception {
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
        reconciliationMcRed10.setAmount(Long.valueOf(record[3]));
        if(!fileName.contains("reversa")) {
          reconciliationMcRed10.setExternalId(Long.valueOf(record[4]));
        }
        lstReconciliationMcRed10.add(reconciliationMcRed10);
      }
    }catch (Exception e){
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
  StringDateInterval convertFileNameToUTCInterval(String fileName, int dateIndex, String dateFormat) {
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
