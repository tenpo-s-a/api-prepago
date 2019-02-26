package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.accounting.ejb.v10.PrepaidAccountingEJBBean10;
import cl.multicaja.accounting.ejb.v10.PrepaidClearingEJBBean10;
import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.ZONEID;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ntp.TimeStamp;

import javax.ejb.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;

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
  private EncryptUtil encryptUtil;


  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10;

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJBBean10;

  @EJB
  private ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10;

  public ReconciliationFilesEJBBean10 getReconciliationFilesEJBBean10() {
    return reconciliationFilesEJBBean10;
  }

  public void setReconciliationFilesEJBBean10(ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10) {
    this.reconciliationFilesEJBBean10 = reconciliationFilesEJBBean10;
  }

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


  private EncryptUtil getEncryptUtil(){
    if(encryptUtil == null){
      encryptUtil = EncryptUtil.getInstance();
    }
    return encryptUtil;
  }
  /**
   * Procesa el archivo de operaciones diarias enviado por Tecnocom
   * @param inputStream
   * @param fileName
   * @throws Exception
   */
  @Override
  public void processFile(InputStream inputStream, String fileName) throws Exception {
    TecnocomReconciliationFile file;
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

    List<TecnocomReconciliationFileDetail> manualTrx = new ArrayList<>();
    List<TecnocomReconciliationFileDetail> otherTrx = new ArrayList<>();

    file.getDetails().forEach(trx -> {
      if(trx.isFromSat()) {
        manualTrx.add(trx);
      } else {
        otherTrx.add(trx);
      }
    });
    // TRX Insertadas por IPM
    insertOrUpdateManualTrx(fileName, manualTrx);


    // Se crea referencia al archivo en la tabla
    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(fileName);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);
    // Insertar movimientos en tecnocom
    this.insertTecnocomMovement(reconciliationFile10.getId(),otherTrx);

    // Se buscan los movimientos en la tabla de tecnocom
    List<MovimientoTecnocom10> movimientoTecnocom10s = this.buscaMovimientosTecnocom(reconciliationFile10.getId());

    // Se procesa el resultado
    this.validateTransactions(fileName, movimientoTecnocom10s);

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

  private void insertTecnocomMovement(Long fileId, List<TecnocomReconciliationFileDetail> reconciliationFileDetailList) throws Exception {

    for (TecnocomReconciliationFileDetail data:reconciliationFileDetailList) {
      MovimientoTecnocom10 movimientoTecnocom10 = buildMovimientoTecnocom(fileId, data);
      movimientoTecnocom10 = insertaMovimientoTecnocom(movimientoTecnocom10);
      if(movimientoTecnocom10.getId() == 0){
        log.error("Verificar por que fallo "+data);
      }
    }
  }
  private MovimientoTecnocom10 buildMovimientoTecnocom(Long fileId,TecnocomReconciliationFileDetail detail){
    MovimientoTecnocom10 movimientoTecnocom10 = new MovimientoTecnocom10();

    // IMPFAC
    NewAmountAndCurrency10 impFac = new NewAmountAndCurrency10();
    impFac.setValue(detail.getImpfac());
    impFac.setCurrencyCode(CodigoMoneda.fromValue(Integer.valueOf(detail.getClamon())));
    movimientoTecnocom10.setImpFac(impFac);

    //IMPDIV
    NewAmountAndCurrency10 impDiv = new NewAmountAndCurrency10();
    impDiv.setValue(detail.getImpDiv());
    impDiv.setCurrencyCode(CodigoMoneda.fromValue(Integer.valueOf(detail.getClamonDiv())));
    movimientoTecnocom10.setImpDiv(impDiv);

    movimientoTecnocom10.setTipoFac(detail.getTipoFac());
    movimientoTecnocom10.setPan(getEncryptUtil().encrypt(detail.getPan()));
    movimientoTecnocom10.setCuenta(detail.getCuenta());
    movimientoTecnocom10.setTipoLin(detail.getTipolin());
    movimientoTecnocom10.setCodPais(getNumberUtils().toInteger(detail.getCodpais()));
    movimientoTecnocom10.setCodAct(Integer.parseInt(detail.getCodact()));
    movimientoTecnocom10.setIndProaje("");
    movimientoTecnocom10.setCmbApli(getNumberUtils().toBigDecimal(detail.getCmbApli()));
    movimientoTecnocom10.setTipoLin(detail.getTipolin());
    movimientoTecnocom10.setNumRefFac("");
    movimientoTecnocom10.setCentAlta(detail.getCentalta());
    movimientoTecnocom10.setCodEnt(detail.getCodent());
    movimientoTecnocom10.setNumAut(detail.getNumaut());
    movimientoTecnocom10.setNumExtCta(getNumberUtils().toLong(detail.getNumextcta()));
    movimientoTecnocom10.setNomPob("");
    movimientoTecnocom10.setLinRef(Integer.parseInt(detail.getLinref()));
    movimientoTecnocom10.setIndNorCor(detail.getTipoFac().getCorrector());
    movimientoTecnocom10.setFecFac(Date.valueOf(detail.getFecfac()));
    movimientoTecnocom10.setIdArchivo(fileId);
    if(movimientoTecnocom10.getOperationType() == TecnocomOperationType.AU){
      movimientoTecnocom10.setFecTrn(Timestamp.valueOf(String.format("%s %s",detail.getFecTrn(),detail.getHorTrn())));
      movimientoTecnocom10.setImpautcon(new NewAmountAndCurrency10(detail.getImpAutCon()));
    }

    return movimientoTecnocom10;
  }

  /**
   * Insertar en la tabla prp_movimientos, las transcacciones realizadas de forma manual por SAT.
   * @param trxs
   */
  private void insertOrUpdateManualTrx(String fileName, List<TecnocomReconciliationFileDetail> trxs) {

    for (TecnocomReconciliationFileDetail trx : trxs) {
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

  /**
   * Proceso que valida el archivo de transacciones diarias
   * OP: Realiza el proceso de conciliacion.
   * AU: Inserta los movimientos de compra.
   * @param fileName
   * @param trxs
   */
  private void validateTransactions(String fileName, List<MovimientoTecnocom10> trxs) {

    for (MovimientoTecnocom10 trx : trxs) {
      try{

        //Se obtiene el pan
        String pan = Utils.replacePan(getEncryptUtil().decrypt(trx.getPan()));

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null, pan, String.format("%s%s%s",trx.getCuenta(),trx.getCentAlta(),trx.getCodEnt()));

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", fileName, String.format("%s%s%s",trx.getCuenta(),trx.getCentAlta(),trx.getCodEnt()));
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }
        // Procesa las operaciones
        if(trx.getOperationType().equals(TecnocomOperationType.OP)) {
          //Se busca el movimiento
          PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(prepaidCard10.getIdUser(),
            trx.getNumAut(), trx.getFecFac() , trx.getTipoFac());

          if(originalMovement == null) {
            TipoFactura tipofac = trx.getTipoFac();
            String msg = String.format("Error processing transaction - Transaction not found in database with userId = [%s], tipofac= [%s], indnorcor = [%s], numaut = [%s], fecfac = [%s], amount = [%s]",
              prepaidCard10.getIdUser(), tipofac.getCode(), tipofac.getCorrector(),  trx.getNumAut(), trx.getFecFac(), trx.getImpFac().getValue());
            log.error(msg);
            trx.setHasError(Boolean.TRUE);
            trx.setErrorDetails(msg);

            log.info("Movimiento no encontrado, no conciliado");
            // Construyendo un Id.
            String researchId = "ExtId:[";
            if (trx.getNumAut() != null) {
              researchId += trx.getNumAut();
            } else {
              researchId += "NoExternalId";
            }

            researchId += "]-";
            getPrepaidMovementEJBBean10().createMovementResearch(null, researchId, ReconciliationOriginType.TECNOCOM, fileName);

            throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);

          } else if(ReconciliationStatusType.PENDING.equals(originalMovement.getConTecnocom())) {
            if(originalMovement.getMonto().compareTo(trx.getImpFac().getValue()) != 0 ){
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

          PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean10().getPrepaidMovementForAut(prepaidCard10.getIdUser(),trx.getTipoFac(), trx.getNumAut());

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
          // Los movimientos se insertan con fecha de conciliacion lejana, esta se debe actualizar cuando el movimiento es conciliado
          accountingData10.setConciliationDate(Timestamp.valueOf(ZonedDateTime.now(ZoneOffset.UTC).plusYears(1000).toLocalDateTime()));

          accountingData10=getPrepaidAccountingEJBBean10().saveAccountingData(null,accountingData10);

          //Build Clearing
          ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().buildClearing(accountingData10.getId(),null);

          clearingData10=getPrepaidClearingEJBBean10().insertClearingData(null,clearingData10);

        }

      } catch (Exception ex) {
        ex.printStackTrace();
        log.error(String.format("Error processing transaction [%s]", trx.getNumAut()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileName, trx);
      }
    }
  }

  private PrepaidMovement10 buildMovementAut(Long userId, String pan, MovimientoTecnocom10 batchTrx) {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    prepaidMovement.setIdMovimientoRef(0L);
    prepaidMovement.setIdPrepaidUser(userId);
    prepaidMovement.setIdTxExterno(batchTrx.getNumAut());
    prepaidMovement.setTipoMovimiento(batchTrx.getMovementType());
    prepaidMovement.setMonto(batchTrx.getImpFac().getValue());
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setCodent(batchTrx.getCodEnt());
    prepaidMovement.setCentalta(batchTrx.getCentAlta());
    prepaidMovement.setCuenta(batchTrx.getCuenta());
    prepaidMovement.setClamon(batchTrx.getImpFac().getCurrencyCode());
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(batchTrx.getTipoFac().getCorrector()));
    prepaidMovement.setTipofac(batchTrx.getTipoFac());
    prepaidMovement.setFecfac(new Date(batchTrx.getFecTrn().getTime()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(pan);
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(batchTrx.getImpDiv().getValue());
    prepaidMovement.setImpfac(batchTrx.getImpautcon().getValue());
    prepaidMovement.setCmbapli(0);
    prepaidMovement.setNumaut(batchTrx.getNumAut());
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom(batchTrx.getCodCom());
    prepaidMovement.setCodact(NumberUtils.getInstance().toInteger(batchTrx.getCodAct()));
    prepaidMovement.setImpliq(BigDecimal.ZERO);
    prepaidMovement.setClamonliq(0);
    prepaidMovement.setCodpais(CodigoPais.fromValue(NumberUtils.getInstance().toInteger(batchTrx.getCodPais())));
    prepaidMovement.setNompob("");
    prepaidMovement.setNumextcta(NumberUtils.getInstance().toInteger(batchTrx.getNumExtCta()));
    prepaidMovement.setNummovext(NumberUtils.getInstance().toInteger(batchTrx.getNumMovExt()));
    prepaidMovement.setClamone(batchTrx.getClamone().getValue());
    prepaidMovement.setTipolin(batchTrx.getTipoLin());
    prepaidMovement.setLinref(NumberUtils.getInstance().toInteger(batchTrx.getLinRef()));
    prepaidMovement.setNumbencta(1);
    prepaidMovement.setNumplastico(0L);
    prepaidMovement.setCodent(batchTrx.getCodEnt());
    prepaidMovement.setOriginType(MovementOriginType.OPE);
    prepaidMovement.setFechaCreacion(Timestamp.valueOf(getDateUtils().localDateTimeInUTC(batchTrx.getFecTrn().toLocalDateTime(), ZONEID.AMERICA_SANTIAGO)));
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

  private void processErrorTrx(String fileName, MovimientoTecnocom10 trx) {
    log.info("processErrorTrx");
    //TODO: definir como informar las transacciones
    Map<String, Object> templateData = new HashMap<>();
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

  /**
   * Permite buscar movientos en la tabla de tecnocom.
   * @param fileId
   * @return
   * @throws Exception
   */
  public List<MovimientoTecnocom10> buscaMovimientosTecnocom(Long fileId) throws Exception{

    if(fileId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileId"));
    }

    Object[] params = {
      new InParam(fileId, Types.BIGINT),
    };
    RowMapper rm = (Map<String, Object> row) -> {
      MovimientoTecnocom10 movimientoTecnocom10 = new MovimientoTecnocom10();
      movimientoTecnocom10.setId(getNumberUtils().toLong(row.get("_id")));
      movimientoTecnocom10.setIdArchivo(getNumberUtils().toLong(row.get("_idarchivo")));
      movimientoTecnocom10.setCuenta(String.valueOf(row.get("_cuenta")));
      movimientoTecnocom10.setPan(String.valueOf(row.get("_pan")));
      movimientoTecnocom10.setCodEnt(String.valueOf(row.get("_codent")));
      movimientoTecnocom10.setCentAlta(String.valueOf(row.get("_centalta")));

      NewAmountAndCurrency10 impFac = new NewAmountAndCurrency10();
      impFac.setValue(getNumberUtils().toBigDecimal(row.get("_impfac")));
      impFac.setCurrencyCode(CodigoMoneda.fromValue(getNumberUtils().toInt(row.get("_clamon"))));
      movimientoTecnocom10.setImpFac(impFac);

      NewAmountAndCurrency10 impDiv = new NewAmountAndCurrency10();
      impDiv.setValue(getNumberUtils().toBigDecimal(row.get("_impdiv")));
      impDiv.setCurrencyCode(CodigoMoneda.fromValue(getNumberUtils().toInt(row.get("_clamondiv"))));
      movimientoTecnocom10.setImpDiv(impDiv);

      NewAmountAndCurrency10 impLiq = new NewAmountAndCurrency10();
      impLiq.setValue(getNumberUtils().toBigDecimal(row.get("_impliq")));
      impLiq.setCurrencyCode(CodigoMoneda.fromValue(getNumberUtils().toInt(row.get("_clamonliq"))));
      movimientoTecnocom10.setImpLiq(impLiq);

      movimientoTecnocom10.setIndNorCor(getNumberUtils().toInteger(row.get("_indnorcor")));
      movimientoTecnocom10.setTipoFac(TipoFactura.fromValue(getNumberUtils().toInteger(row.get("_tipofac"))));
      movimientoTecnocom10.setFecFac((Date)row.get("_fecfac"));
      movimientoTecnocom10.setNumRefFac(String.valueOf(row.get("_numreffac")));

      movimientoTecnocom10.setCmbApli(getNumberUtils().toBigDecimal(row.get("_cmbapli")));
      movimientoTecnocom10.setNumAut(String.valueOf(row.get("_numaut")));
      movimientoTecnocom10.setIndProaje(String.valueOf(row.get("_indproaje")));
      movimientoTecnocom10.setCodCom(String.valueOf(row.get("_codcom")));
      movimientoTecnocom10.setCodAct(getNumberUtils().toInteger(row.get("_codact")));
      movimientoTecnocom10.setCodPais(getNumberUtils().toInteger(row.get("_codpais")));
      movimientoTecnocom10.setNomPob(String.valueOf(row.get("_nompob")));
      movimientoTecnocom10.setNumExtCta(getNumberUtils().toLong(row.get("_numextcta")));
      movimientoTecnocom10.setNumMovExt(getNumberUtils().toLong(row.get("_nummovext")));
      movimientoTecnocom10.setClamone(CodigoMoneda.fromValue(getNumberUtils().toInteger(row.get("_clamone"))));
      movimientoTecnocom10.setTipoLin(String.valueOf(row.get("_tipolin")));
      movimientoTecnocom10.setLinRef(getNumberUtils().toInteger(row.get("_linref")));
      movimientoTecnocom10.setFechaCreacion((Timestamp)row.get("_fecha_creacion"));
      movimientoTecnocom10.setFechaActualizacion((Timestamp)row.get("_fecha_actualizacion"));
      return movimientoTecnocom10;
    };
    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".prp_busca_movimientos_tecnocom_v10", rm,params);

    return (List)resp.get("result");
  }

  /**
   * Permite eliminar Movimientos de la tabla de Tecnocom
   * @param fileId
   * @throws Exception
   */
  public void eliminaMovimientosTecnocom(Long fileId) throws Exception {

    if(fileId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileId"));
    }
    Object[] params = {
      new InParam(fileId, Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_elimina_movimientos_tecnocom_v10", params);

    // Si hay algun error al eliminar se retorna Excepcion
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_prp_elimina_movimientos_tecnocom_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }

  }

  /**
   * Inserta un movimiento de archivo de op diarias en la tabla de movimientos.
   * @param movTc
   * @return
   * @throws Exception
   */
  public MovimientoTecnocom10 insertaMovimientoTecnocom(MovimientoTecnocom10 movTc) throws Exception{

    if(movTc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "empty"));
    }
    if(movTc.getIdArchivo() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "IdArchivo"));
    }
    if(movTc.getCuenta() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "Cuenta"));
    }
    if(movTc.getPan() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "Pan"));
    }
    if(movTc.getTipoFac() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "TipoFac"));
    }
    if(movTc.getImpFac() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "ImpFac"));
    }

    Object[] params = {
      movTc.getIdArchivo() != null ? new InParam(movTc.getIdArchivo(), Types.BIGINT) : new NullParam(Types.BIGINT),
      movTc.getCuenta() != null ? new InParam(movTc.getCuenta(),Types.VARCHAR): new NullParam(Types.BIGINT), // Cuenta
      movTc.getPan() != null ? new InParam(movTc.getPan(),Types.VARCHAR): new NullParam(Types.VARCHAR), // PAN encriptado
      movTc.getCodEnt() != null ? new InParam(movTc.getCodEnt(),Types.VARCHAR): new NullParam(Types.VARCHAR),// COD ENT
      movTc.getCentAlta() != null ? new InParam(movTc.getCentAlta(),Types.VARCHAR): new NullParam(Types.VARCHAR), // CENALTA
      movTc.getImpFac().getCurrencyCode().getValue() != null ? new InParam(movTc.getImpFac().getCurrencyCode().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//clamon
      movTc.getIndNorCor() != null ? new InParam(movTc.getIndNorCor(),Types.NUMERIC): new NullParam(Types.NUMERIC),//indnorcor
      movTc.getTipoFac() != null ? new InParam(movTc.getTipoFac().getCode(),Types.NUMERIC): new NullParam(Types.NUMERIC),//tipofac
      movTc.getFecFac() != null ? new InParam(movTc.getFecFac(),Types.DATE): new NullParam(Types.DATE),//tipofac
      movTc.getNumRefFac() != null ? new InParam(movTc.getNumRefFac(),Types.VARCHAR): new NullParam(Types.VARCHAR), // numreffac
      movTc.getImpDiv().getCurrencyCode() != null ? new InParam(movTc.getImpDiv().getCurrencyCode().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//clamondiv
      movTc.getImpDiv().getValue() != null ? new InParam(movTc.getImpDiv().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//impdiv
      movTc.getImpFac().getValue() != null ? new InParam(movTc.getImpFac().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//impfac
      movTc.getCmbApli() != null ? new InParam(movTc.getCmbApli(),Types.NUMERIC): new NullParam(Types.NUMERIC),//cmbapli
      movTc.getNumAut() != null ? new InParam(movTc.getNumAut(),Types.VARCHAR): new NullParam(Types.VARCHAR), // numaut
      movTc.getIndProaje() != null ? new InParam(movTc.getIndProaje(),Types.VARCHAR): new NullParam(Types.VARCHAR), // indproaje
      movTc.getCodCom() != null ? new InParam(movTc.getCodCom(),Types.VARCHAR): new NullParam(Types.VARCHAR),//codcom
      movTc.getCodAct() != null ? new InParam(movTc.getCodAct(),Types.NUMERIC): new NullParam(Types.NUMERIC),//codact
      movTc.getImpLiq().getValue() != null ? new InParam(movTc.getImpLiq().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//impliq
      movTc.getImpLiq().getCurrencyCode() != null ?new InParam(movTc.getImpLiq().getCurrencyCode().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//clamonliq
      movTc.getCodPais() != null ? new InParam(movTc.getCodPais(),Types.NUMERIC): new NullParam(Types.NUMERIC),//codpais
      movTc.getNomPob() != null ? new InParam(movTc.getNomPob(),Types.VARCHAR): new NullParam(Types.VARCHAR),//nompob
      movTc.getNumExtCta() != null ? new InParam(movTc.getNumExtCta(),Types.NUMERIC): new NullParam(Types.NUMERIC),//numextcta
      movTc.getNumMovExt() != null ? new InParam(movTc.getNumMovExt(),Types.NUMERIC): new NullParam(Types.NUMERIC),//nummovext
      movTc.getClamone() != null ? new InParam(movTc.getClamone().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//clamone
      movTc.getTipoLin() != null ? new InParam(movTc.getTipoLin(),Types.VARCHAR): new NullParam(Types.VARCHAR),//tipolin
      movTc.getLinRef() != null ? new InParam(movTc.getLinRef(),Types.NUMERIC): new NullParam(Types.NUMERIC),//linref
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String, Object> resp = getDbUtils().execute(getSchema()+ ".mc_prp_crea_movimiento_tecnocom_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_prp_crea_movimiento_tecnocom_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    movTc.setId(getNumberUtils().toLong(resp.get("_r_id")));
    return movTc;
  }

}
