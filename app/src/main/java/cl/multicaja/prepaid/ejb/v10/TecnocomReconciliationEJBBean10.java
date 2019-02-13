package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.accounting.ejb.v10.PrepaidAccountingEJBBean10;
import cl.multicaja.accounting.ejb.v10.PrepaidClearingEJBBean10;
import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.ZONEID;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

/**
 * @author abarazarte
 **/

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class TecnocomReconciliationEJBBean10 extends PrepaidBaseEJBBean10 implements TecnocomReconciliationEJB10 {

  private static Log log = LogFactory.getLog(TecnocomReconciliationEJBBean10.class);

  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss Z");
  private static DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static ZoneId here = ZoneId.of("America/Santiago");

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10;

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJBBean10;

  public PrepaidClearingEJBBean10 getPrepaidClearingEJBBean10() {
    return prepaidClearingEJBBean10;
  }

  public void setPrepaidClearingEJBBean10(PrepaidClearingEJBBean10 prepaidClearingEJBBean10) {
    this.prepaidClearingEJBBean10 = prepaidClearingEJBBean10;
  }

  public PrepaidAccountingEJBBean10 getPrepaidAccountingEJBBean10() {
    return prepaidAccountingEJBBean10;
  }

  public void setPrepaidAccountingEJBBean10(PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10) {
    this.prepaidAccountingEJBBean10 = prepaidAccountingEJBBean10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJBBean10() {
    return prepaidCardEJBBean10;
  }

  public void setPrepaidCardEJBBean10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  /**
   * Procesa el archivo de operaciones diarias enviado por Tecnocom
   * @param inputStream
   * @param fileName
   * @throws Exception
   */
  @Override
  public void processFile(InputStream inputStream, String fileName) throws Exception {
    ReconciliationFile file;
    try {
      file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    } catch (Exception ex) {
      String msg = String.format("Error processing file [%s]", fileName);
      log.error(msg, ex);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
    }

    if(file.isSuspicious()) {
      String msg = String.format("Error processing file [%s]. File seems suspicious", fileName);
      log.error(msg);
      processErrorSuspiciousFile(fileName);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
    }

    List<ReconciliationFileDetail> manualTrx = new ArrayList<>();
    List<ReconciliationFileDetail> otherTrx = new ArrayList<>();

    file.getDetails().forEach(trx -> {
      if(trx.isFromSat()) {
        manualTrx.add(trx);
      } else {
        otherTrx.add(trx);
      }
    });

    insertOrUpdateManualTrx(fileName, manualTrx);

    validateTransactions(fileName, otherTrx);

    /**
     * Se toma la fecha de envio del archivo y se marcan como NOT_RECONCILED los movimientos de 1 dia antes que no vinieron
     * el archivo actual o anterior.
     */

    String fileDate = getDateForNotReconciledTransactions(file.getHeader().getFecenvio(), file.getHeader().getHoraenvio());

    List<TipoFactura> tipFacs = Arrays.asList(TipoFactura.CARGA_TRANSFERENCIA,
      TipoFactura.ANULA_CARGA_TRANSFERENCIA,
      TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA,
      TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA,
      TipoFactura.RETIRO_TRANSFERENCIA,
      TipoFactura.ANULA_RETIRO_TRANSFERENCIA,
      TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA,
      TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    for (TipoFactura type : tipFacs) {
      log.info(String.format("Changing status to not reconciled transaction from date [%s] and tipofac [%s]", fileDate, type.getDescription()));
      getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, fileDate, fileDate, type, IndicadorNormalCorrector.fromValue(type.getCorrector()), ReconciliationStatusType.NOT_RECONCILED);
    }
  }

  /**
   * Insertar en la tabla prp_movimientos, las transcacciones realizadas de forma manual por SAT.
   * @param trxs
   */
  private void insertOrUpdateManualTrx(String fileName, List<ReconciliationFileDetail> trxs) {
    for (ReconciliationFileDetail trx : trxs) {
      try{
        //Se obtiene el pan
        String pan = Utils.replacePan(trx.getPan());

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null,
          pan,
          trx.getContrato());

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", fileName, trx.getContrato());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        //Se busca el movimiento
        PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(prepaidCard10.getIdUser(),
          trx.getNumaut(), Date.valueOf(trx.getFecfac()), trx.getTipoFac());

        if(originalMovement == null) {
          // Movimiento original no existe.
          PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(prepaidCard10.getIdUser(), pan, trx);
          movement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
          movement10.setConSwitch(ReconciliationStatusType.PENDING);
          movement10.setOriginType(MovementOriginType.SAT);
          movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
          movement10.setIdMovimientoRef(Long.valueOf(0));
          movement10.setIdTxExterno("");
          movement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

          String msg = String.format("Error processing transaction - Transaction not found in database with userId = [%s], tipofac= [%s], indnorcor = [%s], numaut = [%s], fecfac = [%s], amount = [%s]",
            prepaidCard10.getIdUser(), trx.getTipoFac().getCode(), trx.getTipoFac().getCorrector(),  trx.getNumaut(), trx.getFecfac(), trx.getImpfac());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);

          log.info("Movimiento no encontrado, no conciliado");
          // Construyendo un Id.
          String researchId = "ExtId:[";
          if (trx.getNumaut() != null) {
            researchId += trx.getNumaut();
          } else {
            researchId += "NoExternalId";
          }

          researchId += "]-";
          getPrepaidMovementEJBBean10().createMovementResearch(null, researchId, ReconciliationOriginType.TECNOCOM, fileName);

        } else if(ReconciliationStatusType.PENDING.equals(originalMovement.getConTecnocom())) {
          if(!originalMovement.getMonto().equals(trx.getImpfac())){
            getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ReconciliationStatusType.NOT_RECONCILED);
          } else {
            //Actualiza el estado_con_tecnocom a conciliado
            getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ReconciliationStatusType.RECONCILED);
          }
        } else {
          log.info(String.format("Transaction already processed  id -> [%s]", originalMovement.getId()));
        }
      } catch (Exception ex) {
        log.error(String.format("Error processing transaction [%s]", trx.toString()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileName, trx);
      }
    }
  }

  private void validateTransactions(String fileName, List<ReconciliationFileDetail> trxs) {
    for (ReconciliationFileDetail trx : trxs) {
      try{
        //Se obtiene el pan
        String pan = Utils.replacePan(trx.getPan());

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null, pan, trx.getContrato());

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", fileName, trx.getContrato());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        // Procesa las operaciones
        if(trx.getOperationType().equals(TecnocomOperationType.OP)) {
          //Se busca el movimiento
          PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(prepaidCard10.getIdUser(),
            trx.getNumaut(), Date.valueOf(trx.getFecfac()), trx.getTipoFac());

          if(originalMovement == null) {
            TipoFactura tipofac = trx.getTipoFac();
            String msg = String.format("Error processing transaction - Transaction not found in database with userId = [%s], tipofac= [%s], indnorcor = [%s], numaut = [%s], fecfac = [%s], amount = [%s]",
              prepaidCard10.getIdUser(), tipofac.getCode(), tipofac.getCorrector(),  trx.getNumaut(), trx.getFecfac(), trx.getImpfac());
            log.error(msg);
            trx.setHasError(Boolean.TRUE);
            trx.setErrorDetails(msg);

            log.info("Movimiento no encontrado, no conciliado");
            // Construyendo un Id.
            String researchId = "ExtId:[";
            if (trx.getNumaut() != null) {
              researchId += trx.getNumaut();
            } else {
              researchId += "NoExternalId";
            }

            researchId += "]-";
            getPrepaidMovementEJBBean10().createMovementResearch(null, researchId, ReconciliationOriginType.TECNOCOM, fileName);

            throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);

          } else if(ReconciliationStatusType.PENDING.equals(originalMovement.getConTecnocom())) {
            if(originalMovement.getMonto().compareTo(trx.getImpfac()) != 0 ){
              log.info("Movimiento no conciliado");
              getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                originalMovement.getId(),
                ReconciliationStatusType.NOT_RECONCILED);
            }
            else{
               getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                    originalMovement.getId(),
                    ReconciliationStatusType.RECONCILED);
            }
          } else  {
            log.info(String.format("Transaction already processed  id -> [%s]", originalMovement.getId()));
          }
        }
        // Procesa las autorizaciones
        else if(trx.getOperationType().equals(TecnocomOperationType.AU)) {

          PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean10().getPrepaidMovementForAut(prepaidCard10.getIdUser(),trx.getTipoFac(), trx.getNumaut());

          // Si la autorizacion ya fue creada, no se vuelve a insertar
          if(originalMovement != null) {
            // Autorozacion ya insertada
            log.info("Autorizacion ya insertada.");
            continue;
          }
          // Build Movement
          PrepaidMovement10 prepaidMovement10 =buildMovementAut(prepaidCard10.getIdUser(),prepaidCard10.getPan(),trx);

          getPrepaidMovementEJBBean10().addPrepaidMovement(null,prepaidMovement10);

          //Build Accounting
          PrepaidAccountingMovement prepaidAccountingMovement = new PrepaidAccountingMovement();
          prepaidAccountingMovement.setPrepaidMovement10(prepaidMovement10);
          AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().buildAccounting10(prepaidAccountingMovement, AccountingStatusType.PENDING,AccountingStatusType.PENDING);

          accountingData10=getPrepaidAccountingEJBBean10().saveAccountingData(null,accountingData10);

          //Build Clearing
          ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().buildClearing(accountingData10.getId(),null);
          clearingData10=getPrepaidClearingEJBBean10().insertClearingData(null,clearingData10);


        }

      } catch (Exception ex) {
        ex.printStackTrace();
        log.error(String.format("Error processing transaction [%s]", trx.getNumaut()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileName, trx);
      }
    }
  }

  private PrepaidMovement10 buildMovementAut(Long userId, String pan, ReconciliationFileDetail batchTrx) {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    prepaidMovement.setIdMovimientoRef(0L);
    prepaidMovement.setIdPrepaidUser(userId);
    prepaidMovement.setIdTxExterno(batchTrx.getNumaut());
    prepaidMovement.setTipoMovimiento(batchTrx.getMovementType());
    prepaidMovement.setMonto(batchTrx.getImpfac());
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setCodent(batchTrx.getCodent());
    prepaidMovement.setCentalta(batchTrx.getCentalta());
    prepaidMovement.setCuenta(batchTrx.getCuenta());
    prepaidMovement.setClamon(CodigoMoneda.fromValue(NumberUtils.getInstance().toInteger(batchTrx.getClamon())));
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(batchTrx.getTipoFac().getCorrector()));
    prepaidMovement.setTipofac(batchTrx.getTipoFac());
    prepaidMovement.setFecfac(Date.valueOf(batchTrx.getFecTrn()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(pan);
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(batchTrx.getImpDiv());
    prepaidMovement.setImpfac(batchTrx.getImpAutCon());
    prepaidMovement.setCmbapli(0);
    prepaidMovement.setNumaut(batchTrx.getNumaut());
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom(batchTrx.getCodcom());
    prepaidMovement.setCodact(NumberUtils.getInstance().toInteger(batchTrx.getCodact()));
    prepaidMovement.setImpliq(BigDecimal.ZERO);
    prepaidMovement.setClamonliq(0);
    prepaidMovement.setCodpais(CodigoPais.fromValue(NumberUtils.getInstance().toInteger(batchTrx.getCodpais())));
    prepaidMovement.setNompob("");
    prepaidMovement.setNumextcta(NumberUtils.getInstance().toInteger(batchTrx.getNumextcta()));
    prepaidMovement.setNummovext(NumberUtils.getInstance().toInteger(batchTrx.getNummovext()));
    prepaidMovement.setClamone(NumberUtils.getInstance().toInteger(batchTrx.getClamon()));
    prepaidMovement.setTipolin(batchTrx.getTipolin());
    prepaidMovement.setLinref(NumberUtils.getInstance().toInteger(batchTrx.getLinref()));
    prepaidMovement.setNumbencta(1);
    prepaidMovement.setNumplastico(0L);
    prepaidMovement.setCodent(batchTrx.getCodent());
    prepaidMovement.setOriginType(MovementOriginType.OPE);
    String dateTrn = String.format("%s %s",batchTrx.getFecTrn(),batchTrx.getHorTrn());
    LocalDateTime date= getDateUtils().dateStringToLocalDateTime(dateTrn,"yyyy-MM-dd HH:mm:ss");
    prepaidMovement.setFechaCreacion(Timestamp.valueOf(getDateUtils().localDateTimeInUTC(date, ZONEID.AMERICA_SANTIAGO)));
    //Tecnocom No conciliado
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    // Switch Conciliado ya que no pasa por switch
    prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);

    return prepaidMovement;
  }

  private void processErrorSuspiciousFile(String fileName) {
    log.info(String.format("processErrorSuspiciousFile - %s", fileName));

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("fileName", fileName);
    //TODO: definir template de correo
    //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TECNOCOM_FILE_SUSPICIOUS, templateData);
  }

  private void processErrorTrx(String fileName, ReconciliationFileDetail trx) {
    log.info("processErrorTrx");
    //TODO: definir como informar las transacciones
    Map<String, Object> templateData = new HashMap<String, Object>();
    //templateData.put("fileName", fileName);
    //TODO: definir template de correo
    //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TECNOCOM_FILE_SUSPICIOUS, templateData);
  }

  private String getDateForNotReconciledTransactions(String date, String time) {
    ZonedDateTime hereAndNow = Instant.now().atZone(here);
    String timezoneOffset = String.format("%tz", hereAndNow);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(String.format("%s %s %s", date, time, timezoneOffset), formatter);

    Instant instant = zonedDateTime.toInstant().minus(1, ChronoUnit.DAYS);

    //get date time only
    LocalDateTime result = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));

    return result.toLocalDate().format(dbFormatter);
  }
}
