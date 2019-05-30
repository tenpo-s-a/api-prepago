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
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.async.v10.PrepaidInvoiceDelegate10;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.ejb.v11.PrepaidMovementEJBBean11;
import cl.multicaja.prepaid.helpers.fees.FeeService;
import cl.multicaja.prepaid.helpers.fees.model.Charge;
import cl.multicaja.prepaid.helpers.fees.model.ChargeType;
import cl.multicaja.prepaid.helpers.fees.model.Fee;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationRegisterType;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.tecnocom.constants.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.ejb.*;
import javax.inject.Inject;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
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
  private static final String INSERT_MOVEMENT_SQL = "INSERT INTO %s.%s (" +
  "idArchivo, cuenta, pan, codent, centalta, clamon, indnorcor, tipofac, fecfac, numreffac, clamondiv, impdiv, " +
  "impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, " +
  "clamone, tipolin, linref, fectrn, impautcon, originope, fecha_creacion, fecha_actualizacion, contrato, tiporeg, nomcomred" +
  ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

  @EJB
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

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

  @EJB
  private IpmEJBBean10 ipmEJBBean10;

  @Inject
  private PrepaidInvoiceDelegate10 prepaidInvoiceDelegate10;

  private FeeService feeService;

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

  public IpmEJBBean10 getIpmEJBBean10() { return ipmEJBBean10; }

  public void setIpmEJBBean10(IpmEJBBean10 ipmEJBBean10) { this.ipmEJBBean10 = ipmEJBBean10; }

  public void setFeeService(FeeService feeService) { this.feeService = feeService; }

  public FeeService getFeeService() { return this.feeService; }

  /**
   * Procesa el archivo de operaciones diarias enviado por Tecnocom
   * Ya no se usa en esta proyecto, el que lee el archivo se movio a batch-worker
   *
   * @param inputStream
   * @param fileName
   * @throws Exception
   */
  @Override
  @Deprecated
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

    // Se procesan los conciliados
    List<MovimientoTecnocom10> reconciledList = this.buscaMovimientosTecnocom(fileId, OriginOpeType.CONC_ORIGIN);

    if(autoList != null){
      // TRX Insertadas x Servicio.
      this.insertAutorization(fileId, reconciledList);
    }

    //Elimina Trx de la tabla de Tecnocom.
    this.eliminaMovimientosTecnocom(fileId);

    // Expira los movimientos
    this.getPrepaidMovementEJBBean11().expireNotReconciledMovements(ReconciliationFileType.TECNOCOM_FILE);
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

  //TODO: La lectura del archivo OPD se esta realizando en el proyecto prepaid-batch-worker
  @Deprecated
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
    movimientoTecnocom10.setPan(detail.getPan()); // El movimiento tecnocom guarda el pan hasheado, pero para test se usa plano
    movimientoTecnocom10.setCuenta(detail.getCuenta());
    movimientoTecnocom10.setTipoLin(detail.getTipolin());
    movimientoTecnocom10.setCodPais(getNumberUtils().toInteger(detail.getCodpais()));
    movimientoTecnocom10.setCodAct(Integer.parseInt(detail.getCodact()));
    movimientoTecnocom10.setIndProaje("");
    movimientoTecnocom10.setCmbApli(getNumberUtils().toBigDecimal(detail.getCmbApli()));
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
    movimientoTecnocom10.setFecFac(!detail.getFecTrn().equals("") ? Date.valueOf(detail.getFecTrn()).toLocalDate(): LocalDate.now()); // TODO: cual es el formato del string? Para pasarlo directamente a LocalDate sin tener que pasar por sql.Date
    movimientoTecnocom10.setTipoReg(detail.getTiporeg());
    // Cualquier cosa, solo se usa para test
    movimientoTecnocom10.setFecTrn(Timestamp.valueOf(LocalDateTime.now()));
    movimientoTecnocom10.setImpautcon(new NewAmountAndCurrency10(detail.getImpAutCon()));
    impFac.setValue(BigDecimal.ZERO);
    movimientoTecnocom10.setImpFac(impFac);
    movimientoTecnocom10.setImpLiq(impFac);
    movimientoTecnocom10.setNumMovExt(0L);
    movimientoTecnocom10.setClamone(CodigoMoneda.CLP);
    movimientoTecnocom10.setCodCom("Cualquiera");
    movimientoTecnocom10.setNomcomred("Cualquiera");

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
        String hashedPan = trx.getPan();

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardByPanHashAndAccountNumber(null, hashedPan, trx.getContrato());

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
        PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean11().getPrepaidMovementForTecnocomReconciliation(account.getUserId(), trx.getNumAut(),
          java.sql.Date.valueOf(trx.getFecFac()), trx.getTipoFac());

        if(originalMovement == null) {
          // Movimiento original no existe.
          PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(prepaidCard10.getIdUser(), prepaidCard10.getPan(), trx);
          movement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
          movement10.setConSwitch(ReconciliationStatusType.PENDING);
          movement10.setOriginType(MovementOriginType.SAT);
          movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
          movement10.setIdMovimientoRef(Long.valueOf(0));
          movement10.setIdTxExterno("");
          movement10 = getPrepaidMovementEJBBean11().addPrepaidMovement(null, movement10);

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
            getPrepaidMovementEJBBean11().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ReconciliationStatusType.NOT_RECONCILED);
          } else {
            //Actualiza el estado_con_tecnocom a conciliado
            getPrepaidMovementEJBBean11().updateStatusMovementConTecnocom(null,
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
        String hashedPan = trx.getPan();

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardByPanHashAndAccountNumber(null, hashedPan, trx.getContrato());
        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with hashedPan [%s]", fileId, trx.getPan());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        // Procesa las operaciones
        if(trx.getTipoReg().equals(TecnocomReconciliationRegisterType.OP)) {

          Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
          //Se busca el movimiento
          PrepaidMovement10 originalMovement = getPrepaidMovementEJBBean11().getPrepaidMovementForTecnocomReconciliationV2(prepaidCard10.getId(), trx.getNumAut(), java.sql.Date.valueOf(trx.getFecFac()), trx.getTipoFac());

          if(originalMovement == null) {
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

            getPrepaidMovementEJBBean11().createResearchMovement(
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
            getPrepaidMovementEJBBean11().updateStatusMovementConTecnocom(null,
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
  private void insertAutorization(Long fileId, List<MovimientoTecnocom10> trxs) throws Exception {
    log.info("INSERT AUT IN");
    for (MovimientoTecnocom10 trx : trxs) {
      try {

        //Se obtiene el hashed pan
        String hashedPan = trx.getPan();
        System.out.println(String.format("[%s]  [%s]", hashedPan, trx.getContrato()));

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardByPanHashAndAccountNumber(null, hashedPan, trx.getContrato());
        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with hashedPan [%s]", hashedPan);
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());

        // El estado y el estado_con_tecnocom del movimiento se setearán de acuerdo al tiporeg de la transaccion (AU -> AUTHORIZED + PENDING, OP -> PROCESS_OK + RECONCILED)
        PrepaidMovementStatus newMovementStatus;
        ReconciliationStatusType newTecnocomStatus;
        if (TecnocomReconciliationRegisterType.AU.equals(trx.getTipoReg())) {
          newMovementStatus = PrepaidMovementStatus.AUTHORIZED;
          newTecnocomStatus = ReconciliationStatusType.PENDING;
        } else if (TecnocomReconciliationRegisterType.OP.equals(trx.getTipoReg())) {
          newMovementStatus = PrepaidMovementStatus.PROCESS_OK;
          newTecnocomStatus = ReconciliationStatusType.RECONCILED;
        } else {
          String msg = String.format("Error processing transaction - Transactions' tiporeg is neither AU nor OP, it's unknown type [%s]", trx.getTipoReg());
          log.error(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementForAut(prepaidCard10.getId(), trx.getTipoFac(), IndicadorNormalCorrector.fromValue(trx.getIndNorCor()), trx.getNumAut(), trx.getCodCom());

        if (prepaidMovement10 == null) {
          // No existe en nuestra tabla, debe insertarlo
          prepaidMovement10 = buildMovementAut(prepaidCard10.getPan(), trx, prepaidCard10.getId());

          // Se crea el movimiento con los mismos estados del archivo
          prepaidMovement10.setEstado(newMovementStatus);
          prepaidMovement10.setConTecnocom(newTecnocomStatus);
          prepaidMovement10 = getPrepaidMovementEJBBean11().addPrepaidMovement(null, prepaidMovement10);

          // Se consulta al servicio de comisiones y se insertan las comisiones recibidas
          List<PrepaidMovementFee10> feeList = insertMovementFees(prepaidMovement10);

          // Dado que no esta en la BD, se crean tambien sus campos en las tablas de contabilidad
          insertIntoAccoutingAndClearing(trx.getTipoReg(), prepaidMovement10);

          // Expira cache del saldo de la cuenta
          getAccountEJBBean10().expireBalanceCache(account.getId());

          // Como no se encontro en la BD este movimiento no pasó por el callback
          // Por lo que es necesario levantar el evento de transaccion
          PrepaidUser10 prepaidUser10 = getPrepaidUserEJBBean10().findById(null, account.getUserId());
          TransactionType transactionType;
          switch (prepaidMovement10.getTipoMovimiento()) {
            case PURCHASE:
              transactionType = TransactionType.PURCHASE;
              break;
            case SUSCRIPTION:
              transactionType = TransactionType.SUSCRIPTION;
              break;
            case REFUND:
              transactionType = TransactionType.REFUND;
              break;
            default:
              String msg = String.format("Error - Transaction of type %s came as AUTO in OP file", prepaidMovement10.getTipoMovimiento().toString());
              log.error(msg);
              throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
          }

          // Determinar que tipo de transaccion es, y levantar el evento apropiado
          if (IndicadorNormalCorrector.NORMAL.getValue().equals(trx.getIndNorCor())) {
            getPrepaidMovementEJBBean11().publishTransactionAuthorizedEvent(prepaidUser10.getUuid(), account.getUuid(), prepaidCard10.getUuid(), prepaidMovement10, feeList, transactionType);
          } else {
            getPrepaidMovementEJBBean11().publishTransactionReversedEvent(prepaidUser10.getUuid(), account.getUuid(), prepaidCard10.getUuid(), prepaidMovement10, feeList, transactionType);
          }
        } else {
          PrepaidMovementStatus originalStatus = prepaidMovement10.getEstado();

          // Se actualiza al mismo estado que se encuentre en el archivo
          getPrepaidMovementEJBBean11().updatePrepaidMovementStatus(null, prepaidMovement10.getId(), newMovementStatus);

          // Se actualiza el estado de tecnocom para dejar conciliado o pendiente el movimiento
          getPrepaidMovementEJBBean11().updateStatusMovementConTecnocom(null, prepaidMovement10.getId(), newTecnocomStatus);

          if (originalStatus.equals(PrepaidMovementStatus.NOTIFIED)) {
            // Existe en la tabla pero esta cambiando de NOTIFIED a otro estado, por lo que se crea en la tablas de contabilidad
            insertIntoAccoutingAndClearing(trx.getTipoReg(), prepaidMovement10);
          } else {
            // Existe en la tabla y está cambiando a estado OP (PROCESS_OK), debemos actualizar sus campos en las tablas de contabilidad

            LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = localDateTime.format(formatter);
            
            AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, prepaidMovement10.getId());
            getPrepaidAccountingEJBBean10().updateAccountingStatusAndConciliationDate(null, accountingData10.getId(), AccountingStatusType.OK, formattedDate);

            ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, accountingData10.getId());
            getPrepaidClearingEJBBean10().updateClearingData(null, clearingData10.getId(), AccountingStatusType.PENDING);
          }

          prepaidInvoiceDelegate10.sendInvoice(prepaidInvoiceDelegate10.buildInvoiceData(prepaidMovement10,null));
        }

        // Si el movimiento viene en estado OP (conciliado), se actualiza su valor de acuerdo al archivo IPM
        if (TecnocomReconciliationRegisterType.OP.equals(trx.getTipoReg())) {
          // Se busca el registro "mas parecido" en la tabla IPM
          IpmMovement10 ipmMovement10 = ipmEJBBean10.findByReconciliationSimilarity(prepaidCard10.getPan(), trx.getCodCom(), trx.getImpFac().getValue(), trx.getNumAut());
          if (ipmMovement10 != null) {
            // Actualizar el valor de mastercard en la tablas de contabilidad
            AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, prepaidMovement10.getId());
            accountingData10.getAmountMastercard().setValue(ipmMovement10.getCardholderBillingAmount());
            accountingData10.setConciliationDate(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
            getPrepaidAccountingEJBBean10().updateAccountingDataFull(null, accountingData10);

            // Marcar movimiento tomado en la tabla IPM como conciliado
            getIpmEJBBean10().updateIpmMovementReconciledStatus(ipmMovement10.getId(), true);
          } else {
            String msg = String.format("Error while searching for similar to IPM movement similar to movement [id:%s][truncatedPan: %s][codcom:%s][impFac:%s][numaut:%s], not found", prepaidMovement10.getId(), prepaidCard10.getPan(), trx.getCodCom(), trx.getImpFac().getValue().setScale(2, RoundingMode.HALF_UP).toString(), trx.getNumAut());
            log.error(msg);
            throw new ValidationException(ERROR_DATA_NOT_FOUND.getValue(), msg);
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
    log.info("INSERT AUT OUT");
  }

  private List<PrepaidMovementFee10> insertMovementFees(PrepaidMovement10 prepaidMovement10) throws Exception {
    // Por negocio las devoluciones no generan comisiones de ningun tipo
    if (PrepaidMovementType.REFUND.equals(prepaidMovement10.getTipoMovimiento())) {
      return Collections.emptyList();
    }

    // Pide la lista de comisiones al servicio
    List<Charge> feeCharges;
    try {
      Fee fees = getFeeService().calculateFees(prepaidMovement10.getTipoMovimiento(), prepaidMovement10.getClamon(), prepaidMovement10.getImpfac().longValue());
      feeCharges = fees.getCharges();
    } catch (Exception e) {
      e.printStackTrace();
      log.error(String.format("Error consuming fee service for movement [TipoMovimiento:%s][Clamon:%s][ImpFac:%s]", prepaidMovement10.getTipoMovimiento(), prepaidMovement10.getClamon(), prepaidMovement10.getImpfac().longValue()));
      return Collections.emptyList();
    }

    if (feeCharges != null && !feeCharges.isEmpty()) {
      List<PrepaidMovementFee10> feeList = new ArrayList<>();

      // Por cada comision, almacenarla en la BD
      for (Charge feeCharge : feeCharges) {
        PrepaidMovementFee10 prepaidFee = new PrepaidMovementFee10();
        prepaidFee.setAmount(new BigDecimal(feeCharge.getAmount()));
        prepaidFee.setMovementId(prepaidMovement10.getId());
        prepaidFee.setIva(BigDecimal.ZERO);

        // Convertir el ChargeType (del servicio) a nuestro FeeType
        if (ChargeType.IVA.equals(feeCharge.getChargeType())) {
          prepaidFee.setFeeType(PrepaidMovementFeeType.IVA);
        } else {
          switch (prepaidMovement10.getTipofac()) {
            case COMPRA_INTERNACIONAL:
            case ANULA_COMPRA_INTERNACIONAL:
              prepaidFee.setFeeType(PrepaidMovementFeeType.PURCHASE_INT_FEE);
              break;
            case SUSCRIPCION_INTERNACIONAL:
            case ANULA_SUSCRIPCION_INTERNACIONAL:
              prepaidFee.setFeeType(PrepaidMovementFeeType.SUSCRIPTION_INT_FEE);
              break;
            default:
              prepaidFee.setFeeType(PrepaidMovementFeeType.GENERIC_FEE);
              break;
          }
        }

        // Insertar Fee en BD
        getPrepaidMovementEJBBean11().addPrepaidMovementFee(prepaidFee);
        feeList.add(prepaidFee);
      }

      return feeList;
    } else {
      return Collections.emptyList();
    }
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
      // Los movimientos se insertan con fecha de conciliacion lejana, esta se debe actualizar cuando el movimiento es conciliado
      reconciliationDate = ZonedDateTime.now(ZoneOffset.UTC).plusYears(1000).toLocalDateTime();
    } else {
      accountingStatus = AccountingStatusType.OK;
      clearingStatus = AccountingStatusType.PENDING;
      reconciliationDate = LocalDateTime.now(ZoneId.of("UTC"));
    }

    AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().buildAccounting10(prepaidAccountingMovement, AccountingStatusType.PENDING, accountingStatus);
    accountingData10.setConciliationDate(Timestamp.valueOf(reconciliationDate));
    accountingData10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    //Build Clearing
    ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().buildClearing(accountingData10.getId(),null);
    clearingData10.setStatus(clearingStatus);
    getPrepaidClearingEJBBean10().insertClearingData(null,clearingData10);
  }

  private PrepaidMovement10 buildMovementAut(String pan, MovimientoTecnocom10 batchTrx, Long cardId) throws BadRequestException {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(0L);
    prepaidMovement.setIdPrepaidUser(0L);
    prepaidMovement.setIdTxExterno(batchTrx.getNumAut());
    prepaidMovement.setTipoMovimiento(batchTrx.getMovementType());
    prepaidMovement.setMonto(batchTrx.getImpFac().getValue());
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setCodent(batchTrx.getCodEnt());
    prepaidMovement.setCentalta(batchTrx.getCentAlta());
    prepaidMovement.setCuenta(batchTrx.getCuenta());
    prepaidMovement.setClamon(batchTrx.getImpFac().getCurrencyCode());
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(batchTrx.getIndNorCor()));
    prepaidMovement.setTipofac(TipoFactura.valueOfEnumByCodeAndCorrector(batchTrx.getTipoFac().getCode(), batchTrx.getIndNorCor()));
    Instant utcInstant = batchTrx.getFecFac().atStartOfDay().atZone(ZoneId.of("America/Santiago")).toInstant();
    prepaidMovement.setFecfac(java.util.Date.from(utcInstant));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(pan);
    prepaidMovement.setClamondiv(batchTrx.getImpDiv().getCurrencyCode().getValue());
    prepaidMovement.setImpdiv(batchTrx.getImpDiv().getValue());
    prepaidMovement.setImpfac(batchTrx.getImpFac().getValue());
    prepaidMovement.setCmbapli(batchTrx.getCmbApli().intValue());
    prepaidMovement.setNumaut(batchTrx.getNumAut());
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom(batchTrx.getCodCom());
    prepaidMovement.setCodact(NumberUtils.getInstance().toInteger(batchTrx.getCodAct()));
    prepaidMovement.setImpliq(batchTrx.getImpLiq().getValue());
    prepaidMovement.setClamonliq(batchTrx.getImpLiq().getCurrencyCode().getValue());
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
    prepaidMovement.setNomcomred(batchTrx.getNomcomred()); //FIXME: MovimientoTecnocom debe traer el merchant name. Si lo debe traer. Se agrego el campo en la tabla prp_movimiento_tecnocom
    prepaidMovement.setCardId(cardId);

    return prepaidMovement;
  }

  //TODO: Implementado en prepaid-batch-worker
  @Deprecated
  private void processErrorSuspiciousFile(String fileName) {
    log.info(String.format("processErrorSuspiciousFile - %s", fileName));

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("fileName", fileName);
    //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TECNOCOM_FILE_SUSPICIOUS, templateData);
  }

  //FIXME: Revisar con negocio, como informar o que hacer con un error de transaccion
  private void processErrorTrx(Long fileId, MovimientoTecnocom10 trx) {
    log.info("processErrorTrx");
    Map<String, Object> templateData = new HashMap<>();
    //templateData.put("fileName", fileName);
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

  public MovimientoTecnocom10 buscaMovimientoTecnocomById(Long id) throws BaseException {
    if (id == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    String searchQuery = String.format("SELECT * FROM %s.prp_movimientos_tecnocom WHERE id = %d", getSchema(), id);
    log.info("query: " + searchQuery);
    List<MovimientoTecnocom10> movimientoTecnocom10s = getDbUtils().getJdbcTemplate().query(searchQuery, getMovimientoTCMapper());
    return (movimientoTecnocom10s == null || movimientoTecnocom10s.isEmpty()) ? null : movimientoTecnocom10s.get(0);
  }

  /**
   * Permite buscar movientos en la tabla de tecnocom mediante query
   * @param fileId
   * @return
   * @throws Exception
   */
  public List<MovimientoTecnocom10>  buscaMovimientosTecnocom(String tableName, Long fileId, OriginOpeType originOpeType, String encryptedPan, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut) throws Exception {

    List<MovimientoTecnocom10> movimientoTecnocom10s = new ArrayList<>();

    StringBuilder sqlQuery = new StringBuilder();
    sqlQuery.append("SELECT ");
    sqlQuery.append(" id, ");
    sqlQuery.append(" idArchivo, ");
    sqlQuery.append(" cuenta, ");
    sqlQuery.append(" pan, ");
    sqlQuery.append(" codent, ");
    sqlQuery.append(" centalta, ");
    sqlQuery.append(" clamon, ");
    sqlQuery.append(" indnorcor, ");
    sqlQuery.append(" tipofac, ");
    sqlQuery.append(" fecfac, ");
    sqlQuery.append(" numreffac, ");
    sqlQuery.append(" clamondiv, ");
    sqlQuery.append(" impdiv, ");
    sqlQuery.append(" impfac, ");
    sqlQuery.append(" cmbapli, ");
    sqlQuery.append(" numaut, ");
    sqlQuery.append(" indproaje, ");
    sqlQuery.append(" codcom, ");
    sqlQuery.append(" codact, ");
    sqlQuery.append(" impliq, ");
    sqlQuery.append(" clamonliq, ");
    sqlQuery.append(" codpais, ");
    sqlQuery.append(" nompob, ");
    sqlQuery.append(" numextcta, ");
    sqlQuery.append(" nummovext, ");
    sqlQuery.append(" clamone, ");
    sqlQuery.append(" tipolin, ");
    sqlQuery.append(" linref, ");
    sqlQuery.append(" fectrn, ");
    sqlQuery.append(" impautcon, ");
    sqlQuery.append(" originope, ");
    sqlQuery.append(" fecha_creacion, ");
    sqlQuery.append(" fecha_actualizacion, ");
    sqlQuery.append(" contrato, ");
    sqlQuery.append(" tiporeg, ");
    sqlQuery.append(" nomcomred ");
    sqlQuery.append(  String.format(" FROM %s.%s ", getSchema(), tableName));
    sqlQuery.append(" WHERE ");
    sqlQuery.append(  fileId != null ?        String.format("idArchivo = %d   AND ", fileId) : "");
    sqlQuery.append(  originOpeType != null ? String.format("originope = '%s' AND ", originOpeType.getValue()) : "");
    sqlQuery.append(  encryptedPan != null ?  String.format("pan = '%s'       AND ", encryptedPan) : "");
    sqlQuery.append(  indnorcor != null ?     String.format("indnorcor = %d   AND ", indnorcor.getValue()) : "");
    sqlQuery.append(  tipofac != null ?       String.format("tipofac = %d     AND ", tipofac.getCode()) : "");
    sqlQuery.append(  numaut != null ?        String.format("numaut = '%s'    AND ", numaut) : "");
    if (fecfac != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
      String fecfacString = sdf.format(fecfac);
      sqlQuery.append(String.format("fecfac = to_date('%s', 'YYYYMMDD') AND ", fecfacString));
    }
    sqlQuery.append(" 1 = 1 "); // En caso de que todos sean nulos
    sqlQuery.append(" ORDER BY id ASC ");

    try {
      movimientoTecnocom10s =  getDbUtils().getJdbcTemplate().query(sqlQuery.toString(), this.getMovimientoTCMapper());
      if (movimientoTecnocom10s != null && movimientoTecnocom10s.size() == 0) {
        return null;
      }

    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[buscaMovimientosTecnocom] Movimiento no existe"));
      return null;
    }
    return  movimientoTecnocom10s;
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
      // Borrame
      TipoFactura tipofac = TipoFactura.valueOfEnumByCodeAndCorrector(rs.getInt("tipofac"), movimientoTecnocom10.getIndNorCor());
      if (tipofac == null) {
        log.error(String.format("No se encontro tipo factura: %s, indnorcor: %s", rs.getInt("tipofac"), movimientoTecnocom10.getIndNorCor()));
        tipofac = TipoFactura.valueOfEnumByCodeAndCorrector(TipoFactura.COMPRA_INTERNACIONAL.getCode(), movimientoTecnocom10.getIndNorCor());
      }
      movimientoTecnocom10.setTipoFac(tipofac);

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
      movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.valueOfEnum(rs.getString("tiporeg")));
      movimientoTecnocom10.setNomcomred(rs.getString("nomcomred"));
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
  public MovimientoTecnocom10 insertaMovimientoTecnocom(MovimientoTecnocom10 movTc) throws Exception {

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

    log.info(String.format("[addPrepaidMovement] Guardando Movimiento con [%s]", movTc.toString()));

    KeyHolder keyHolder = new GeneratedKeyHolder();

    String insertIntoMovimientosTecnocom = String.format(INSERT_MOVEMENT_SQL, getSchema(), "prp_movimientos_tecnocom");
    getDbUtils().getJdbcTemplate().update(getInsertMovementCreator(insertIntoMovimientosTecnocom, movTc), keyHolder);

    String insertIntoMovimientosTecnocomHist = String.format(INSERT_MOVEMENT_SQL, getSchema(), "prp_movimientos_tecnocom_hist");
    getDbUtils().getJdbcTemplate().update(getInsertMovementCreator(insertIntoMovimientosTecnocomHist, movTc));

    return this.buscaMovimientoTecnocomById((long) keyHolder.getKey());
  }

  private PreparedStatementCreator getInsertMovementCreator(String insertQuery, MovimientoTecnocom10 movTc) {
    return (connection -> {
      PreparedStatement ps = connection.prepareStatement(insertQuery, new String[]{"id"});
      ps.setLong(1, movTc.getIdArchivo());
      ps.setString(2, movTc.getCuenta());
      ps.setString(3, movTc.getPan());
      ps.setString(4, movTc.getCodEnt());
      ps.setString(5, movTc.getCentAlta());
      ps.setInt(6, movTc.getImpFac().getCurrencyCode().getValue());
      ps.setInt(7, movTc.getIndNorCor());
      ps.setInt(8, movTc.getTipoFac().getCode());
      ps.setDate(9, java.sql.Date.valueOf(movTc.getFecFac()));
      ps.setString(10, movTc.getNumRefFac());
      ps.setInt(11, movTc.getImpDiv().getCurrencyCode().getValue());
      ps.setBigDecimal(12, movTc.getImpDiv().getValue());
      ps.setBigDecimal(13, movTc.getImpFac().getValue());
      ps.setBigDecimal(14, movTc.getCmbApli());
      ps.setString(15, movTc.getNumAut());
      ps.setString(16, movTc.getIndProaje());
      ps.setString(17, movTc.getCodCom());
      ps.setInt(18, movTc.getCodAct());
      ps.setBigDecimal(19, movTc.getImpLiq().getValue());
      ps.setInt(20, movTc.getImpLiq().getCurrencyCode().getValue());
      ps.setInt(21, movTc.getCodPais());
      ps.setString(22, movTc.getNomPob());
      ps.setLong(23, movTc.getNumExtCta());
      ps.setLong(24, movTc.getNumMovExt());
      ps.setInt(25, movTc.getClamone().getValue());
      ps.setString(26, movTc.getTipoLin());
      ps.setInt(27, movTc.getLinRef());
      ps.setTimestamp(28, movTc.getFecTrn());
      ps.setBigDecimal(29, movTc.getImpLiq() != null ? movTc.getImpLiq().getValue(): new BigDecimal(Types.NUMERIC));
      ps.setString(30, movTc.getOriginOpe());
      ps.setTimestamp(31, Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
      ps.setTimestamp(32, Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
      ps.setString(33, movTc.getContrato());
      ps.setString(34, movTc.getTipoReg() != null ? movTc.getTipoReg().getValue() : "");
      ps.setString(35, movTc.getNomcomred() != null ? movTc.getNomcomred() : "");
      return ps;
    });
  }

}
