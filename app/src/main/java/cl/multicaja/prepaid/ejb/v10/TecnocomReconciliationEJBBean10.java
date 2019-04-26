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
import cl.multicaja.prepaid.async.v10.PrepaidInvoiceDelegate10;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.ejb.v11.PrepaidMovementEJBBean11;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationRegisterType;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
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
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private PrepaidMovementEJBBean11 prepaidMovementEJBBean11;

  @EJB
  private PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10;

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJBBean10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10;

  @EJB
  private AccountEJBBean10 accountEJBBean10;


  @Inject
  private PrepaidInvoiceDelegate10 prepaidInvoiceDelegate10;

  public void setPrepaidInvoiceDelegate10(PrepaidInvoiceDelegate10 prepaidInvoiceDelegate10) {
    this.prepaidInvoiceDelegate10 = prepaidInvoiceDelegate10;
  }
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

  public PrepaidCardEJBBean11 getPrepaidCardEJBBean11() {
    return prepaidCardEJBBean11;
  }

  public void setPrepaidCardEJBBean11(PrepaidCardEJBBean11 prepaidCardEJBBean11) {
    this.prepaidCardEJBBean11 = prepaidCardEJBBean11;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  public PrepaidMovementEJBBean11 getPrepaidMovementEJBBean11() {
    return prepaidMovementEJBBean11;
  }

  public void setPrepaidMovementEJBBean11(PrepaidMovementEJBBean11 prepaidMovementEJBBean11) {
    this.prepaidMovementEJBBean11 = prepaidMovementEJBBean11;
  }

  public AccountEJBBean10 getAccountEJBBean10() {
    return accountEJBBean10;
  }

  public void setAccountEJBBean10(AccountEJBBean10 accountEJBBean10) {
    this.accountEJBBean10 = accountEJBBean10;
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
  public Long processFile(InputStream inputStream, String fileName) throws Exception {

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

    // Se crea referencia al archivo en la tabla
    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(fileName);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    // Insertar movimientos en tecnocom
    this.insertTecnocomMovement(reconciliationFile10.getId(),file.getDetails());

    getReconciliationFilesEJBBean10().updateFileStatus(null, reconciliationFile10.getId(), FileStatus.OK);
    return reconciliationFile10.getId();
  }

  public void processTecnocomTableData(Long fileId) throws Exception {
    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = this.buscaMovimientosTecnocom(fileId,OriginOpeType.SAT_ORIGIN);

    if(satList != null){
      // Se procesan TRX Insertadas por SAT
      this.insertOrUpdateManualTrx(fileId, satList);
    }

    // Se buscan movimientos MAUT
    List<MovimientoTecnocom10> mautList = this.buscaMovimientosTecnocom(fileId, OriginOpeType.API_ORIGIN);

    if(mautList != null){
      // TRX Insertadas x Servicio.
      this.processReconciliation(fileId, mautList);
    }

    // Se procesan las autorizaciones
    List<MovimientoTecnocom10> autoList = this.buscaMovimientosTecnocom(fileId, OriginOpeType.AUT_ORIGIN);

    if(autoList != null){
      // TRX Insertadas x Servicio.
      this.insertAutorization(fileId, autoList);
    }

    //Elimina Trx de la tabla de Tecnocom.
    this.eliminaMovimientosTecnocom(fileId);

    // Expira los movimientos
    this.getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.TECNOCOM_FILE);
    this.getPrepaidMovementEJBBean11().expireNotReconciledAuthorizations(); //expira los movimientos con estado NOTIFIED y AUTHORIZED

  }

  public void insertTecnocomMovement(Long fileId, List<TecnocomReconciliationFileDetail> reconciliationFileDetailList) throws Exception {

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
    movimientoTecnocom10.setLinRef(getNumberUtils().toInt(detail.getLinref(),0));
    movimientoTecnocom10.setIndNorCor(detail.getTipoFac().getCorrector());
    movimientoTecnocom10.setIdArchivo(fileId);
    movimientoTecnocom10.setOriginOpe(detail.getOrigenope());
    movimientoTecnocom10.setContrato(detail.getContrato());
    movimientoTecnocom10.setFecFac(Date.valueOf(detail.getFecTrn()).toLocalDate()); // TODO: cual es el formato del string? Para pasarlo directamente a LocalDate sin tener que pasar por sql.Date
    movimientoTecnocom10.setRegisterType(detail.getRegisterType());
    if(movimientoTecnocom10.getOperationType() == TecnocomOperationType.PURCHASES){
      movimientoTecnocom10.setFecTrn(Timestamp.valueOf(String.format("%s %s",detail.getFecTrn(),detail.getHorTrn())));
      movimientoTecnocom10.setImpautcon(new NewAmountAndCurrency10(detail.getImpAutCon()));
      impFac.setValue(BigDecimal.ZERO);
      movimientoTecnocom10.setImpFac(impFac);
    }

    return movimientoTecnocom10;
  }

  /**
   * Insertar en la tabla prp_movimientos, las transcacciones realizadas de forma manual por SAT.
   * @param trxs
   */
  private void insertOrUpdateManualTrx(Long fileId, List<MovimientoTecnocom10> trxs) {

    for (MovimientoTecnocom10 trx : trxs) {
      try{
        //Se obtiene el pan
        String pan = Utils.replacePan(getEncryptUtil().decrypt(trx.getPan()));

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardByPanAndProcessorUserId(null, pan, trx.getContrato());

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - FileID [%s] PrepaidCard not found with processorUserId [%s]", fileId, trx.getContrato());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
        if(account == null) {
          String msg = String.format("Error processing transaction - FileID [%s] PrepaidCard not found with processorUserId [%s]", fileId, trx.getContrato());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        //Se busca el movimiento
        PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(account.getUserId(), trx.getNumAut(),
          java.sql.Date.valueOf(trx.getFecFac()), trx.getTipoFac());

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

          // Expira cache del saldo de la cuenta
          //getAccountEJBBean10().expireBalanceCache(account.getId());

          String msg = String.format("Error processing transaction - Transaction not found in database with userId = [%s], tipofac= [%s], indnorcor = [%s], numaut = [%s], fecfac = [%s], amount = [%s]",
            prepaidCard10.getIdUser(), trx.getTipoFac().getCode(), trx.getTipoFac().getCorrector(),  trx.getNumAut(), trx.getFecFac(), trx.getImpFac());
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
          //getPrepaidMovementEJBBean10().createMovementResearch(null, researchId, ReconciliationOriginType.TECNOCOM, "");

        } else if(ReconciliationStatusType.PENDING.equals(originalMovement.getConTecnocom())) {
          if(!originalMovement.getMonto().equals(trx.getImpFac().getValue())){
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
        ex.printStackTrace();
        log.error(String.format("Error processing transaction [%s]", trx.toString()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileId, trx);
      }
    }
  }

  /**
   * Proceso que valida el archivo de transacciones diarias
   * OP: Realiza el proceso de conciliacion.
   * AU: Inserta los movimientos de compra.
   * @param fileId
   * @param trxs
   */
  private void processReconciliation(Long fileId, List<MovimientoTecnocom10> trxs) {

    for (MovimientoTecnocom10 trx : trxs) {
      try{

        //Se obtiene el pan
        String pan = Utils.replacePan(getEncryptUtil().decrypt(trx.getPan()));

        //Busca cuenta

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardByPanAndProcessorUserId(null, pan, trx.getContrato());

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", fileId, trx.getContrato());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }
        // Procesa las operaciones
        if(trx.getRegisterType().equals(TecnocomReconciliationRegisterType.OP)) {

          Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
          //Se busca el movimiento
          PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(account.getUserId(),
            trx.getNumAut(), java.sql.Date.valueOf(trx.getFecFac()), trx.getTipoFac());

          if(originalMovement == null) {
            TipoFactura tipofac = trx.getTipoFac();
            String msg = String.format("Error processing transaction - Transaction not found in database with userId = [%s], tipofac= [%s], indnorcor = [%s], numaut = [%s], fecfac = [%s], amount = [%s]",
              prepaidCard10.getIdUser(), trx.getTipoFac().getCode(), trx.getIndNorCor(),  trx.getNumAut(), trx.getFecFac(), trx.getImpFac().getValue());
            log.error(msg);
            trx.setHasError(Boolean.TRUE);
            trx.setErrorDetails(msg);

            log.info("Movimiento no encontrado, no conciliado");

            List<ReconciliationFile10> fileList = getReconciliationFilesEJBBean10().getReconciliationFile(null, fileId, null, null, null, null);
            ReconciliationFile10 file = fileList.get(0);

            List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
            ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
            researchMovementInformationFiles.setIdArchivo(fileId);
            researchMovementInformationFiles.setIdEnArchivo(trx.getIdForResearch());
            researchMovementInformationFiles.setNombreArchivo(file.getFileName());
            researchMovementInformationFiles.setTipoArchivo(file.getType().toString());
            researchMovementInformationFilesList.add(researchMovementInformationFiles);

            getPrepaidMovementEJBBean10().createResearchMovement(
              null,
              new ObjectMapper().writeValueAsString(researchMovementInformationFilesList),
              ReconciliationOriginType.TECNOCOM.toString(),
              trx.getFecTrn().toLocalDateTime(),
              ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
              ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB.getValue(),
              0L,
              trx.getMovementType().toString(),
              ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
            );

            throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);

          } else if(ReconciliationStatusType.PENDING.equals(originalMovement.getConTecnocom())) {
            getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                originalMovement.getId(),
                ReconciliationStatusType.RECONCILED);
          } else {
            log.info(String.format("Transaction already processed  id -> [%s]", originalMovement.getId()));
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        log.error(String.format("Error processing transaction [%s]", trx.getNumAut()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileId, trx);
      }
    }
  }
  private void insertAutorization(Long fileId, List<MovimientoTecnocom10> trxs){
    log.info("INSERT AUT IN");
    for (MovimientoTecnocom10 trx : trxs) {
      try {

        //Se obtiene el pan
        String pan = Utils.replacePan(getEncryptUtil().decrypt(trx.getPan()));
        System.out.println(String.format("[%s]  [%s]",pan,trx.getContrato()));
        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardByPanAndProcessorUserId(null, pan,trx.getContrato());

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", fileId, String.format("%s%s%s",trx.getCuenta(),trx.getCentAlta(),trx.getCodEnt()));
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());

        // Expira cache del saldo de la cuenta
        //getAccountEJBBean10().expireBalanceCache(account.getId());

        // El estado del movimiento se estandariza en base al tipo de operacion (AU -> AUTHORIZED, OP -> PROCESS_OK)
        PrepaidMovementStatus movementStatus = TecnocomReconciliationRegisterType.AU.equals(trx.getOperationType()) ? PrepaidMovementStatus.AUTHORIZED : PrepaidMovementStatus.PROCESS_OK;

        // El estado con tecnocom se estandariza para dejar conciliado o pendiente el movimiento
        ReconciliationStatusType tecnocomStatus = TecnocomReconciliationRegisterType.AU.equals(trx.getOperationType()) ? ReconciliationStatusType.PENDING : ReconciliationStatusType.RECONCILED;

        PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementForAut(account.getUserId(), trx.getTipoFac(), trx.getNumAut(), trx.getCodCom());

        if (prepaidMovement10 == null) {
          // No existe en nuestra tabla, debe insertarlo
          prepaidMovement10 = buildMovementAut(prepaidCard10.getIdUser(), prepaidCard10.getPan(), trx);

          // Se crea con el mismo estado del archivo
          prepaidMovement10.setEstado(movementStatus);
          prepaidMovement10.setConTecnocom(tecnocomStatus);
          getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);

          // Dado que no esta en la BD, se crean tambien sus campos en las tablas de contabilidad
          insertIntoAccoutingAndClearing(trx.getRegisterType(), prepaidMovement10);

          if (IndicadorNormalCorrector.fromValue(trx.getIndNorCor()).equals(IndicadorNormalCorrector.NORMAL)) {
            // Como no se encontro en la BD este movimiento no pasó por el callback
            // Por lo que es necesario levantar el evento de transaccion
            PrepaidUser10 prepaidUser10 = getPrepaidUserEJBBean10().findById(null, account.getUserId());
            TransactionType transactionType = prepaidMovement10.getTipoMovimiento().equals(PrepaidMovementType.SUSCRIPTION) ? TransactionType.SUSCRIPTION : TransactionType.PURCHASE;
            getPrepaidMovementEJBBean11().publishTransactionAuthorizedEvent(prepaidUser10.getUuid(), account.getUuid(), prepaidCard10.getUuid(), prepaidMovement10, Collections.emptyList(), transactionType);
          }
        } else {
          PrepaidMovementStatus originalStatus = prepaidMovement10.getEstado();

          // Se actualiza al mismo estado que se encuentre en el archivo
          getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement10.getId(), movementStatus);

          // Se actualiza el estado de tecnocom para dejar conciliado o pendiente el movimiento
          getPrepaidMovementEJBBean11().updateStatusMovementConTecnocom(null, prepaidMovement10.getId(), tecnocomStatus);

          if (originalStatus.equals(PrepaidMovementStatus.NOTIFIED)) {
            // Existe en la tabla pero esta cambiando de NOTIFIED a otro estado, por lo que se crea en la tablas de contabilidad
            insertIntoAccoutingAndClearing(trx.getRegisterType(), prepaidMovement10);
          } else {
            // Existe en la tabla y está cambiando a estado OP (PROCESS_OK), debemos actualizar sus campos en las tablas de contabilidad

            LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = localDateTime.format(formatter);
            getPrepaidAccountingEJBBean10().updateAccountingStatusAndConciliationDate(null, prepaidMovement10.getId(), AccountingStatusType.OK, formattedDate);
            getPrepaidClearingEJBBean10().updateClearingData(null, prepaidMovement10.getId(), AccountingStatusType.PENDING);
          }

          prepaidInvoiceDelegate10.sendInvoice(prepaidInvoiceDelegate10.buildInvoiceData(prepaidMovement10,null));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        log.error(String.format("Error processing transaction [%s]", trx.getNumAut()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileId, trx);
      }
    }
    log.info("INSERT AUT OUT");
  }


  private void insertIntoAccoutingAndClearing(TecnocomReconciliationRegisterType tecnocomReconciliationRegisterType, PrepaidMovement10 prepaidMovement10) throws Exception {
    // Crear accounting
    PrepaidAccountingMovement prepaidAccountingMovement = new PrepaidAccountingMovement();
    prepaidAccountingMovement.setPrepaidMovement10(prepaidMovement10);

    AccountingStatusType accountingStatus;
    AccountingStatusType clearingStatus;
    LocalDateTime reconciliationDate;

    if (TecnocomReconciliationRegisterType.AU.equals(tecnocomReconciliationRegisterType)) {
      accountingStatus = AccountingStatusType.PENDING;
      clearingStatus = AccountingStatusType.INITIAL;
      reconciliationDate = ZonedDateTime.now(ZoneOffset.UTC).plusYears(1000).toLocalDateTime();
    } else {
      accountingStatus = AccountingStatusType.OK;
      clearingStatus = AccountingStatusType.PENDING;
      reconciliationDate = LocalDateTime.now(ZoneId.of("UTC"));
    }

    AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().buildAccounting10(prepaidAccountingMovement, AccountingStatusType.PENDING, accountingStatus);
    // Los movimientos se insertan con fecha de conciliacion lejana, esta se debe actualizar cuando el movimiento es conciliado
    accountingData10.setConciliationDate(Timestamp.valueOf(reconciliationDate));
    accountingData10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    //Build Clearing
    ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().buildClearing(accountingData10.getId(),null);
    clearingData10.setStatus(clearingStatus);
    getPrepaidClearingEJBBean10().insertClearingData(null,clearingData10);
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
    prepaidMovement.setNomcomred(""); // Todo: MovimientoTecnocom debe traer el merchant name

    return prepaidMovement;
  }

  private void processErrorSuspiciousFile(String fileName) {
    log.info(String.format("processErrorSuspiciousFile - %s", fileName));

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("fileName", fileName);
    //TODO: definir template de correo
    //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TECNOCOM_FILE_SUSPICIOUS, templateData);
  }

  private void processErrorTrx(Long fileId, MovimientoTecnocom10 trx) {
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

  public List<MovimientoTecnocom10> buscaMovimientosTecnocom(Long fileId, OriginOpeType originOpeType) throws Exception {
    return buscaMovimientosTecnocom(fileId, originOpeType, null, null, null, null, null);
  }

  public List<MovimientoTecnocom10> buscaMovimientosTecnocom(Long fileId, OriginOpeType originOpeType, String encryptedPan, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut) throws Exception {
    return buscaMovimientosTecnocom("prp_movimientos_tecnocom", fileId, originOpeType, encryptedPan, indnorcor, tipofac, fecfac, numaut);
  }

  public List<MovimientoTecnocom10> buscaMovimientosTecnocomHist(Long fileId, OriginOpeType originOpeType, String encryptedPan, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut) throws Exception {
    return buscaMovimientosTecnocom("prp_movimientos_tecnocom_hist", fileId, originOpeType, encryptedPan, indnorcor, tipofac, fecfac, numaut);
  }

  public List<MovimientoTecnocom10> buscaMovimientosTecnocom(Long fileId, OriginOpeType originOpeType, String tipoFacturas) throws Exception {
    if (fileId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileId"));
    }
    if (originOpeType == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "originOpeType"));
    }
    if (tipoFacturas == null || tipoFacturas.isEmpty()) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipoFacturas"));
    }

    String searchQuery = String.format("SELECT * FROM %s.prp_movimientos_tecnocom WHERE idArchivo = %d AND originope = '%s' AND tipofac IN (%s)", getSchema(), fileId, originOpeType.getValue(), tipoFacturas);
    log.info("query: " + searchQuery);
    return getDbUtils().getJdbcTemplate().query(searchQuery, getMovimientoTCMapper());
  }

  /**
   * Permite buscar movientos en la tabla de tecnocom.
   * @param fileId
   * @return
   * @throws Exception
   */
  public List<MovimientoTecnocom10> buscaMovimientosTecnocom(String tableName, Long fileId, OriginOpeType originOpeType, String encryptedPan, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut) throws Exception {
    if(tableName == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tableName"));
    }

    Object[] params = {
      new InParam(tableName, Types.VARCHAR),
      fileId != null ? new InParam(fileId, Types.BIGINT) : new NullParam(Types.BIGINT),
      originOpeType != null ? new InParam(originOpeType.getValue(), Types.VARCHAR) : new NullParam(Types.VARCHAR),
      encryptedPan != null ? new InParam(encryptedPan, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      indnorcor != null ? new InParam(indnorcor.getValue(), Types.NUMERIC) : new NullParam(Types.NUMERIC),
      tipofac != null ? new InParam(tipofac.getCode(), Types.NUMERIC) : new NullParam(Types.NUMERIC),
      fecfac != null ? new InParam(fecfac, Types.DATE) : new NullParam(Types.DATE),
      numaut != null ? new InParam(numaut, Types.VARCHAR) : new NullParam(Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".prp_busca_movimientos_tecnocom_v11", getMovimientoTecnocomRowMapper(), params);

    return (List)resp.get("result");
  }

  private org.springframework.jdbc.core.RowMapper<MovimientoTecnocom10> getMovimientoTCMapper() {
    return (ResultSet rs, int rowNum) -> {
      MovimientoTecnocom10 movimientoTecnocom10 = new MovimientoTecnocom10();
      movimientoTecnocom10.setId(rs.getLong("id"));
      movimientoTecnocom10.setIdArchivo(rs.getLong("idarchivo"));
      movimientoTecnocom10.setCuenta(rs.getString("cuenta"));
      movimientoTecnocom10.setPan(rs.getString("pan"));
      movimientoTecnocom10.setCodEnt(rs.getString("codent"));
      movimientoTecnocom10.setCentAlta(rs.getString("centalta"));

      NewAmountAndCurrency10 impFac = new NewAmountAndCurrency10();
      impFac.setValue(rs.getBigDecimal("impfac"));
      impFac.setCurrencyCode(CodigoMoneda.fromValue(rs.getInt("clamon")));
      movimientoTecnocom10.setImpFac(impFac);

      NewAmountAndCurrency10 impDiv = new NewAmountAndCurrency10();
      impDiv.setValue(rs.getBigDecimal("impdiv"));
      impDiv.setCurrencyCode(CodigoMoneda.fromValue(rs.getInt("clamondiv")));
      movimientoTecnocom10.setImpDiv(impDiv);

      NewAmountAndCurrency10 impLiq = new NewAmountAndCurrency10();
      impLiq.setValue(rs.getBigDecimal("impliq"));
      impLiq.setCurrencyCode(CodigoMoneda.fromValue(rs.getInt("clamonliq")));
      movimientoTecnocom10.setImpLiq(impLiq);

      movimientoTecnocom10.setIndNorCor(rs.getInt("indnorcor"));
      movimientoTecnocom10.setTipoFac(TipoFactura.valueOfEnumByCodeAndCorrector(rs.getInt("tipofac"), movimientoTecnocom10.getIndNorCor()));

      movimientoTecnocom10.setFecFac(rs.getDate("fecfac").toLocalDate());
      movimientoTecnocom10.setNumRefFac(rs.getString("numreffac"));

      movimientoTecnocom10.setCmbApli(rs.getBigDecimal("cmbapli"));
      movimientoTecnocom10.setNumAut(rs.getString("numaut"));
      movimientoTecnocom10.setIndProaje(rs.getString("indproaje"));
      movimientoTecnocom10.setCodCom(rs.getString("codcom"));
      movimientoTecnocom10.setCodAct(rs.getInt("codact"));
      movimientoTecnocom10.setCodPais(rs.getInt("codpais"));
      movimientoTecnocom10.setNomPob(rs.getString("nompob"));
      movimientoTecnocom10.setNumExtCta(rs.getLong("numextcta"));
      movimientoTecnocom10.setNumMovExt(rs.getLong("nummovext"));
      movimientoTecnocom10.setClamone(CodigoMoneda.fromValue(rs.getInt("clamone")));
      movimientoTecnocom10.setTipoLin(rs.getString("tipolin"));
      movimientoTecnocom10.setLinRef(rs.getInt("linref"));
      movimientoTecnocom10.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
      movimientoTecnocom10.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
      movimientoTecnocom10.setFecTrn(rs.getTimestamp("fectrn"));
      movimientoTecnocom10.setImpautcon(new NewAmountAndCurrency10(rs.getBigDecimal("impautcon")));
      movimientoTecnocom10.setOriginOpe(rs.getString("originope"));
      movimientoTecnocom10.setContrato(rs.getString("contrato"));
      return movimientoTecnocom10;
    };
  }

  private RowMapper getMovimientoTecnocomRowMapper() {
    return (Map<String, Object> row) -> {
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
      movimientoTecnocom10.setTipoFac(TipoFactura.valueOfEnumByCodeAndCorrector(getNumberUtils().toInteger(row.get("_tipofac")), movimientoTecnocom10.getIndNorCor()));

      movimientoTecnocom10.setFecFac(((java.sql.Date) row.get("_fecfac")).toLocalDate());
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
      movimientoTecnocom10.setFechaCreacion((Timestamp) row.get("_fecha_creacion"));
      movimientoTecnocom10.setFechaActualizacion((Timestamp) row.get("_fecha_actualizacion"));
      movimientoTecnocom10.setFecTrn((Timestamp) row.get("_fectrn"));
      movimientoTecnocom10.setImpautcon(new NewAmountAndCurrency10(getNumberUtils().toBigDecimal(row.get("_impautcon"))));
      movimientoTecnocom10.setOriginOpe(String.valueOf(row.get("_originope")));
      movimientoTecnocom10.setContrato(String.valueOf(row.get("_contrato")));
      return movimientoTecnocom10;
    };
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
    if(movTc.getCuenta() == null || movTc.getCuenta().trim().isEmpty()){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "Cuenta"));
    }
    if(movTc.getPan() == null || movTc.getPan().trim().isEmpty()){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "Pan"));
    }
    if(movTc.getTipoFac() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "TipoFac"));
    }
    if(movTc.getImpFac() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "ImpFac"));
    }
    if(movTc.getNumAut() == null || movTc.getNumAut().trim().isEmpty()){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "NumAut"));
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
      movTc.getImpDiv() != null ? new InParam(movTc.getImpDiv().getCurrencyCode().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//clamondiv
      movTc.getImpDiv() != null ? new InParam(movTc.getImpDiv().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//impdiv
      movTc.getImpFac() != null ? new InParam(movTc.getImpFac().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//impfac
      movTc.getCmbApli() != null ? new InParam(movTc.getCmbApli(),Types.NUMERIC): new NullParam(Types.NUMERIC),//cmbapli
      movTc.getNumAut() != null ? new InParam(movTc.getNumAut(),Types.VARCHAR): new NullParam(Types.VARCHAR), // numaut
      movTc.getIndProaje() != null ? new InParam(movTc.getIndProaje(),Types.VARCHAR): new NullParam(Types.VARCHAR), // indproaje
      movTc.getCodCom() != null ? new InParam(movTc.getCodCom(),Types.VARCHAR): new NullParam(Types.VARCHAR),//codcom
      movTc.getCodAct() != null ? new InParam(movTc.getCodAct(),Types.NUMERIC): new NullParam(Types.NUMERIC),//codact
      movTc.getImpLiq() != null ? new InParam(movTc.getImpLiq().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//impliq
      movTc.getImpLiq() != null ?new InParam(movTc.getImpLiq().getCurrencyCode().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//clamonliq
      movTc.getCodPais() != null ? new InParam(movTc.getCodPais(),Types.NUMERIC): new NullParam(Types.NUMERIC),//codpais
      movTc.getNomPob() != null ? new InParam(movTc.getNomPob(),Types.VARCHAR): new NullParam(Types.VARCHAR),//nompob
      movTc.getNumExtCta() != null ? new InParam(movTc.getNumExtCta(),Types.NUMERIC): new NullParam(Types.NUMERIC),//numextcta
      movTc.getNumMovExt() != null ? new InParam(movTc.getNumMovExt(),Types.NUMERIC): new NullParam(Types.NUMERIC),//nummovext
      movTc.getClamone() != null ? new InParam(movTc.getClamone().getValue(),Types.NUMERIC): new NullParam(Types.NUMERIC),//clamone
      movTc.getTipoLin() != null ? new InParam(movTc.getTipoLin(),Types.VARCHAR): new NullParam(Types.VARCHAR),//tipolin
      movTc.getLinRef() != null ? new InParam(movTc.getLinRef(),Types.NUMERIC): new NullParam(Types.NUMERIC),//linref
      movTc.getFecTrn() != null ? new InParam(movTc.getFecTrn(),Types.TIMESTAMP): new NullParam(Types.TIMESTAMP),
      movTc.getImpautcon() != null ? new InParam(movTc.getLinRef(),Types.NUMERIC) :  new NullParam(Types.NUMERIC),
      movTc.getOriginOpe() != null ? new InParam(movTc.getOriginOpe(),Types.VARCHAR): new NullParam(Types.VARCHAR),
      movTc.getContrato() != null ? new InParam(movTc.getContrato(),Types.VARCHAR): new NullParam(Types.VARCHAR),
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
