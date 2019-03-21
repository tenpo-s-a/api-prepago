package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.helpers.mastercard.MastercardIpmFileHelper;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.accounting.helpers.mastercard.model.IpmMessage;
import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Base64Utils;

import javax.ejb.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static cl.multicaja.core.model.Errors.*;

/**
 * Todos los metodos para el nuevo esquema de contabilidad.
 *
 * @author JOG
 */

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidAccountingEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidAccountingEJB10 {

  private static Log log = LogFactory.getLog(PrepaidAccountingEJBBean10.class);

  private CalculationsHelper calculationsHelper;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJBBean10;

  @EJB
  private PrepaidAccountingFileEJBBean10 prepaidAccountingFileEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  private EncryptUtil encryptUtil;

  public CalculationsHelper getCalculationsHelper(){
    if(calculationsHelper == null){
      calculationsHelper = CalculationsHelper.getInstance();
    }
    return calculationsHelper;
  }

  public MailPrepaidEJBBean10 getMailPrepaidEJBBean10() {
    return mailPrepaidEJBBean10;
  }

  public void setMailPrepaidEJBBean10(MailPrepaidEJBBean10 mailPrepaidEJBBean10) {
    this.mailPrepaidEJBBean10 = mailPrepaidEJBBean10;
  }

  public PrepaidClearingEJBBean10 getPrepaidClearingEJBBean10() {
    return prepaidClearingEJBBean10;
  }

  public void setPrepaidClearingEJBBean10(PrepaidClearingEJBBean10 prepaidClearingEJBBean10) {
    this.prepaidClearingEJBBean10 = prepaidClearingEJBBean10;
  }

  public PrepaidAccountingFileEJBBean10 getPrepaidAccountingFileEJBBean10() {
    return prepaidAccountingFileEJBBean10;
  }

  public void setPrepaidAccountingFileEJBBean10(PrepaidAccountingFileEJBBean10 prepaidAccountingFileEJBBean10) {
    this.prepaidAccountingFileEJBBean10 = prepaidAccountingFileEJBBean10;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJB10() {
    return prepaidCardEJBBean10;
  }

  public void setPrepaidCardEJB10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
  }

  public EncryptUtil getEncryptUtil(){
    if(encryptUtil == null){
      encryptUtil = EncryptUtil.getInstance();
    }
    return encryptUtil;
  }

  public AccountingData10 searchAccountingByIdTrx(Map<String, Object> header, Long  idTrx) throws Exception {

    if(idTrx == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idTrx"));
    }
    List<AccountingData10> data = this.searchAccountingData(null, null, idTrx);
    return (data == null || data.isEmpty()) ? null : data.get(0);
  }

  public List<AccountingData10> searchAccountingData(Map<String, Object> header, LocalDateTime dateToSearch) throws Exception {

    if(dateToSearch == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "dateToSearch"));
    }

    Date date = Date.from(dateToSearch.atZone(ZoneId.of("UTC")).toInstant());
    return this.searchAccountingData(null, date, null);
  }

  private List<AccountingData10> searchAccountingData(Map<String, Object> header, Date dateToSearch, Long idTrx) throws Exception {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    String dateString = null;

    if(dateToSearch != null){
      dateString = dateFormatter.format(dateToSearch);
    }


    Object[] params = {
      dateString == null ? new NullParam(Types.VARCHAR): new InParam(dateString, Types.VARCHAR),
      idTrx == null ? new NullParam(Types.BIGINT) : new InParam(idTrx, Types.BIGINT)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      AccountingData10 account = new AccountingData10();
      account.setId(getNumberUtils().toLong(row.get("_id")));

      account.setIdTransaction(getNumberUtils().toLong(row.get("_id_tx")));
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setValue(getNumberUtils().toBigDecimal(row.get("_amount")));
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      account.setAmount(amount);

      NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
      amountUsd.setValue(getNumberUtils().toBigDecimal(row.get("_amount_usd")));
      amountUsd.setCurrencyCode(CodigoMoneda.USA_USD);
      account.setAmountUsd(amountUsd);

      account.setExchangeRateDif(getNumberUtils().toBigDecimal(row.get("_exchange_rate_dif")));
      account.setFee(getNumberUtils().toBigDecimal(row.get("_fee")));
      account.setFeeIva(getNumberUtils().toBigDecimal(row.get("_fee_iva")));
      account.setType(AccountingTxType.fromValue(String.valueOf(row.get("_type"))));
      account.setOrigin(AccountingOriginType.fromValue(String.valueOf(row.get("_origin"))));
      account.setTransactionDate((Timestamp) row.get("_transaction_date"));
      account.setAccountingMovementType(AccountingMovementType.fromValue(String.valueOf(row.get("_accounting_mov"))));
      account.setConciliationDate((Timestamp) row.get("_conciliation_date"));
      account.setCollectorFee(getNumberUtils().toBigDecimal(row.get("_collector_fee")));
      account.setCollectorFeeIva(getNumberUtils().toBigDecimal(row.get("_collector_fee_iva")));
      account.setAmountMastercard(new NewAmountAndCurrency10(getNumberUtils().toBigDecimal(row.get("_amount_mcar"))));
      account.setAmountBalance(new NewAmountAndCurrency10(getNumberUtils().toBigDecimal(row.get("_amount_balance"))));
      account.setStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_status"))));
      account.setAccountingStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_accounting_status"))));
      account.setFileId(getNumberUtils().toLong(row.get("file_id")));
      return account;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_prp_search_accounting_data_v10", rm, params);
    log.info("Respuesta Busca Movimiento: "+resp);
    return (List)resp.get("result");
  }

  public List<AccountingData10> saveAccountingData (Map<String, Object> header, List<AccountingData10> accounting10s) throws Exception {
    if(accounting10s == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accounting10s"));
    }
    List<AccountingData10> accounting10sFinal = new ArrayList<>();
    for(AccountingData10 account : accounting10s) {
      account = saveAccountingData(null, account);
      accounting10sFinal.add(account);
    }
    return accounting10sFinal;
  }


  public AccountingData10 updateAccountingDataFull(Map<String, Object> header, AccountingData10 accounting10) throws Exception {

    if(accounting10.getIdTransaction() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getIdTransaction"));
    }
    // La fecha ya esta en UTC
    LocalDateTime date = accounting10.getTransactionDate().toLocalDateTime();
    String transactionDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    //
    LocalDateTime rD = accounting10.getConciliationDate().toLocalDateTime();
    String conciliationDate = rD.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Object[] params = {
      new InParam(accounting10.getIdTransaction(), Types.BIGINT),
      accounting10.getAmount() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getAmount().getValue(), Types.NUMERIC),
      accounting10.getAmount().getCurrencyCode() == null ?  new NullParam(Types.NUMERIC) : new InParam(accounting10.getAmount().getCurrencyCode().getValue(), Types.NUMERIC),
      accounting10.getAmountUsd().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( accounting10.getAmountUsd().getValue(), Types.NUMERIC),
      accounting10.getAmountMastercard().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( accounting10.getAmountMastercard().getValue(), Types.NUMERIC),
      accounting10.getExchangeRateDif() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getExchangeRateDif(), Types.NUMERIC),
      accounting10.getFee() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getFee(), Types.NUMERIC),
      accounting10.getFeeIva() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getFeeIva(),Types.NUMERIC),
      accounting10.getCollectorFee() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getCollectorFee(), Types.NUMERIC),
      accounting10.getCollectorFeeIva() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getCollectorFeeIva(),Types.NUMERIC),
      accounting10.getAmountBalance().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( accounting10.getAmountBalance().getValue(),Types.NUMERIC),
      new InParam(transactionDate,Types.VARCHAR),
      new InParam(conciliationDate,Types.VARCHAR),
      new InParam(accounting10.getStatus().getValue(), Types.VARCHAR),
      new InParam(accounting10.getAccountingStatus().getValue(), Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_acc_update_accounting_full_data_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_update_accounting_full_data_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    accounting10 = searchAccountingByIdTrx(header,accounting10.getIdTransaction());
    return accounting10;
  }

  public AccountingData10 saveAccountingData(Map<String, Object> header, AccountingData10 accounting10) throws Exception {
    if(accounting10.getIdTransaction() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getIdTransaction"));
    }
    if(accounting10.getType() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getType"));
    }
    if(accounting10.getOrigin() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getOrigin"));
    }
    if(accounting10.getTransactionDate() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getTransactionDate"));
    }
    if(accounting10.getStatus() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getStatus"));
    }

    // La fecha ya esta en UTC
    LocalDateTime date = accounting10.getTransactionDate().toLocalDateTime();
    String transactionDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    //
    LocalDateTime rD = accounting10.getConciliationDate().toLocalDateTime();
    String conciliationDate = rD.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Object[] params = {
      new InParam(accounting10.getIdTransaction(), Types.BIGINT),
      new InParam(accounting10.getType().getValue(), Types.VARCHAR),
      new InParam(accounting10.getAccountingMovementType().getValue(), Types.VARCHAR),
      new InParam(accounting10.getOrigin().getValue(), Types.VARCHAR),
      accounting10.getAmount() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getAmount().getValue(), Types.NUMERIC),
      accounting10.getAmount().getCurrencyCode() == null ?  new NullParam(Types.NUMERIC) : new InParam(accounting10.getAmount().getCurrencyCode().getValue(), Types.NUMERIC),
      accounting10.getAmountUsd().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( accounting10.getAmountUsd().getValue(), Types.NUMERIC),
      accounting10.getAmountMastercard().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( accounting10.getAmountMastercard().getValue(), Types.NUMERIC),
      accounting10.getExchangeRateDif() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getExchangeRateDif(), Types.NUMERIC),
      accounting10.getFee() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getFee(), Types.NUMERIC),
      accounting10.getFeeIva() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getFeeIva(),Types.NUMERIC),
      accounting10.getCollectorFee() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getCollectorFee(), Types.NUMERIC),
      accounting10.getCollectorFeeIva() == null ? new NullParam(Types.NUMERIC) : new InParam(accounting10.getCollectorFeeIva(),Types.NUMERIC),
      accounting10.getAmountBalance().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( accounting10.getAmountBalance().getValue(),Types.NUMERIC),
      new InParam(transactionDate,Types.VARCHAR),
      new InParam(conciliationDate,Types.VARCHAR),
      new InParam(accounting10.getStatus().getValue(), Types.VARCHAR),
      new InParam(accounting10.getFileId(), Types.BIGINT),
      new InParam(accounting10.getAccountingStatus().getValue(), Types.VARCHAR),
      new OutParam("_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_prp_insert_accounting_data_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_prp_insert_accounting_data_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    accounting10.setId(getNumberUtils().toLong(resp.get("_id")));
    log.info("Accounting Insertado Id: "+getNumberUtils().toLong(resp.get("_id")));

    return accounting10;
  }

  /**
   * Busca los movimientos conciliados para agregarlos en la tabla de contabilidad.
   *
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  public List<PrepaidAccountingMovement> getReconciledPrepaidMovementsForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception {
    if(date == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    String ts = date.minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Object[] params = {
      ts,
      ReconciliationStatusType.RECONCILED.getValue(),
      BusinessStatusType.CONFIRMED.getValue(),
      AccountingOriginType.MOVEMENT.getValue()
    };

    RowMapper rm = (Map<String, Object> row) -> {
      try{
        PrepaidAccountingMovement pA = new PrepaidAccountingMovement();

        PrepaidMovement10 p = new PrepaidMovement10();
        p.setId(getNumberUtils().toLong(row.get("_id")));
        p.setIdMovimientoRef(getNumberUtils().toLong(row.get("_id_movimiento_ref")));
        p.setIdPrepaidUser(getNumberUtils().toLong(row.get("_id_usuario")));
        p.setIdTxExterno(String.valueOf(row.get("_id_tx_externo")));
        p.setTipoMovimiento(PrepaidMovementType.valueOfEnum(String.valueOf(row.get("_tipo_movimiento"))));
        p.setMonto(getNumberUtils().toBigDecimal(row.get("_monto")));
        p.setEstado(PrepaidMovementStatus.valueOfEnum(String.valueOf(row.get("_estado"))));
        p.setEstadoNegocio(BusinessStatusType.fromValue(String.valueOf(row.get("_estado_de_negocio"))));
        p.setConSwitch(ReconciliationStatusType.fromValue(String.valueOf(row.get("_estado_con_switch"))));
        p.setConTecnocom(ReconciliationStatusType.fromValue(String.valueOf(row.get("_estado_con_tecnocom"))));
        p.setOriginType(MovementOriginType.fromValue(String.valueOf(row.get("_origen_movimiento"))));
        p.setFechaCreacion((Timestamp) row.get("_fecha_creacion"));
        p.setFechaActualizacion((Timestamp) row.get("_fecha_actualizacion"));
        p.setCodent(String.valueOf(row.get("_codent")));
        p.setCentalta(String.valueOf(row.get("_centalta")));
        p.setCuenta(String.valueOf(row.get("_cuenta")));
        p.setClamon(CodigoMoneda.fromValue(getNumberUtils().toInteger(row.get("_clamon"))));
        p.setIndnorcor(IndicadorNormalCorrector.fromValue(getNumberUtils().toInteger(row.get("_indnorcor"))));
        p.setTipofac(TipoFactura.valueOfEnumByCodeAndCorrector(getNumberUtils().toInteger(row.get("_tipofac")), p.getIndnorcor().getValue()));
        p.setFecfac((java.sql.Date)row.get("_fecfac"));
        p.setNumreffac(String.valueOf(row.get("_numreffac")));
        p.setPan(String.valueOf(row.get("_pan")));
        p.setClamondiv(getNumberUtils().toInteger(row.get("_clamondiv")));
        p.setImpdiv(getNumberUtils().toBigDecimal(row.get("_impdiv")));
        p.setImpfac(getNumberUtils().toBigDecimal(row.get("_impfac")));
        p.setCmbapli(getNumberUtils().toInteger(row.get("_cmbapli")));
        p.setNumaut(String.valueOf(row.get("_numaut")));
        p.setIndproaje(IndicadorPropiaAjena.fromValue(String.valueOf(row.get("_indproaje"))));
        p.setCodcom(String.valueOf(row.get("_codcom")));
        p.setCodact(getNumberUtils().toInteger(row.get("_codact")));
        p.setImpliq(getNumberUtils().toBigDecimal(row.get("_impliq")));
        p.setClamonliq(getNumberUtils().toInteger(row.get("_clamonliq")));
        p.setCodpais(CodigoPais.fromValue(getNumberUtils().toInteger(row.get("_codpais"))));
        p.setNompob(String.valueOf(row.get("_nompob")));
        p.setNumextcta(getNumberUtils().toInteger(row.get("_numextcta")));
        p.setNummovext(getNumberUtils().toInteger(row.get("_nummovext")));
        p.setClamone(getNumberUtils().toInteger(row.get("_clamone")));
        p.setTipolin(String.valueOf(row.get("_tipolin")));
        p.setLinref(getNumberUtils().toInteger(row.get("_linref")));
        p.setNumbencta(getNumberUtils().toInteger(row.get("_numbencta")));
        p.setNumplastico(getNumberUtils().toLong(row.get("_numplastico")));

        pA.setPrepaidMovement10(p);
        pA.setReconciliationDate((Timestamp) row.get("_fecha_conciliacion"));
        log.info("RowMapper getPrepaidMovements");
        log.info(p);

        return pA;
      }catch (Exception e){
        e.printStackTrace();
        log.info("RowMapper Error: "+e);
        return null;
      }
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_movimientos_conciliados_para_contabilidad_v10", rm, params);
    log.info("Respuesta buscar movimientos para accounting: "+resp);
    return (List)resp.get("result");
  }

  /**
   * Procesa los movimientos conciliados para agregarlos en la tabla de contabilidad
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @throws Exception
   */
  public List<AccountingData10> processMovementForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception {
    if(date == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    log.info("Buscando movimientos a insertar en tabla de contabilidad. -> [fecha]: " + date.toString());

    //Obtiene los movimientos
    List<PrepaidAccountingMovement> movements = this.getReconciledPrepaidMovementsForAccounting(headers, date);

    if(movements != null) {

      log.info("Se filtran los movimientos por tipo de factura -> Cargas y Retiros");
      movements = movements.stream().filter(movement -> (TipoFactura.CARGA_TRANSFERENCIA.equals(movement.getPrepaidMovement10().getTipofac()) ||
        TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(movement.getPrepaidMovement10().getTipofac()) ||
        TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(movement.getPrepaidMovement10().getTipofac())
      ))
        .collect(Collectors.toList());


      List<AccountingData10> accountingMovements = new ArrayList<>();

      for (PrepaidAccountingMovement m : movements) {
        AccountingData10 accounting = buildAccounting10(m, AccountingStatusType.OK, AccountingStatusType.OK);
        accountingMovements.add(accounting);
      }

      if(!accountingMovements.isEmpty()) {
        accountingMovements = this.saveAccountingData(headers, accountingMovements);
        //Se agrega insert de Clearing
        this.saveClearingData(accountingMovements);

        log.info("Movimientos insertados en la tabla de contabilidad y liquidacion: " + accountingMovements.size());
        return accountingMovements;
      } else {
        log.info("No hay movimientos a insertar en la tabla de contabilidad y liquidacion");
        return Collections.emptyList();
      }

    }
    else {
      log.info("No hay movimientos a insertar en la tabla de contabilidad. -> [fecha]: " + date.toString());
      return Collections.emptyList();
    }
  }

  public AccountingData10 buildAccounting10(PrepaidAccountingMovement accountingMovement, AccountingStatusType status, AccountingStatusType accountingStatus) {
    AccountingTxType type = AccountingTxType.RETIRO_WEB;
    AccountingMovementType movementType = AccountingMovementType.RETIRO_WEB;
    TransactionOriginType trxOriginType = TransactionOriginType.WEB;

    PrepaidMovement10 movement = accountingMovement.getPrepaidMovement10();

    if (TipoFactura.CARGA_TRANSFERENCIA.equals(movement.getTipofac())) {
      type = AccountingTxType.CARGA_WEB;
      movementType = AccountingMovementType.CARGA_WEB;
    } else if (TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(movement.getTipofac())) {
      type = AccountingTxType.CARGA_POS;
      movementType = AccountingMovementType.CARGA_POS;
      trxOriginType = TransactionOriginType.POS;
    } else if (TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(movement.getTipofac())) {
      type = AccountingTxType.RETIRO_POS;
      movementType = AccountingMovementType.RETIRO_POS;
      trxOriginType = TransactionOriginType.POS;
    } else if (TipoFactura.SUSCRIPCION_INTERNACIONAL.equals(movement.getTipofac())) {
      type = AccountingTxType.COMPRA_SUSCRIPCION;
      movementType = AccountingMovementType.SUSCRIPCION;
      trxOriginType = TransactionOriginType.MASTERCARDINT;
    } else if (TipoFactura.COMPRA_INTERNACIONAL.equals(movement.getTipofac()) && movement.getClamondiv().equals(CodigoMoneda.CHILE_CLP)) {
      type = AccountingTxType.COMPRA_PESOS;
      movementType = AccountingMovementType.COMPRA_PESOS;
      trxOriginType = TransactionOriginType.MASTERCARDINT;
    } else if (TipoFactura.COMPRA_INTERNACIONAL.equals(movement.getTipofac()) && !movement.getClamondiv().equals(CodigoMoneda.CHILE_CLP)) {
      type = AccountingTxType.COMPRA_MONEDA;
      movementType = AccountingMovementType.COMPRA_MONEDA;
      trxOriginType = TransactionOriginType.MASTERCARDINT;
    }
    //TODO: Verificar todo lo que son devolucione y anulaciones.
    AccountingData10 accounting = new AccountingData10();
    accounting.setIdTransaction(movement.getId());
    accounting.setOrigin(AccountingOriginType.MOVEMENT);
    accounting.setType(type);
    accounting.setAccountingMovementType(movementType);
    accounting.setAmount(new NewAmountAndCurrency10(movement.getImpfac().setScale(0, BigDecimal.ROUND_HALF_UP)));

    //Se colocan en 0 ya que solo se procesan cargas y retiros
    NewAmountAndCurrency10 zero = new NewAmountAndCurrency10(BigDecimal.ZERO.setScale(0, BigDecimal.ROUND_HALF_UP));
    accounting.setAmountUsd(zero);
    accounting.setExchangeRateDif(BigDecimal.ZERO);
    accounting.setAmountMastercard(zero);

    //Se calcula la comision del movimiento
    BigDecimal fee = BigDecimal.ZERO;
    BigDecimal feeIva = BigDecimal.ZERO;
    switch (movement.getTipoMovimiento()) {
      case TOPUP:
        // Calcula las comisiones segun el tipo de carga (WEB o POS)
        if (TransactionOriginType.WEB.equals(trxOriginType)) {
          fee = getPercentage().getTOPUP_WEB_FEE_AMOUNT().setScale(0, BigDecimal.ROUND_HALF_UP);
          feeIva = getCalculationsHelper().calculateFeeIva(fee).setScale(0, BigDecimal.ROUND_HALF_UP);

          accounting.setFee(fee);
          accounting.setFeeIva(feeIva);
          accounting.setCollectorFee(BigDecimal.ZERO);
          accounting.setCollectorFeeIva(BigDecimal.ZERO);
        }
        else {
          // Comision es Fija $200
          fee = getPercentage().getTOPUP_POS_FEE_AMOUNT().setScale(0, BigDecimal.ROUND_HALF_UP);
          feeIva = getCalculationsHelper().calculateFeeIva(fee).setScale(0, BigDecimal.ROUND_HALF_UP);
          accounting.setFee(BigDecimal.ZERO);
          accounting.setFeeIva(BigDecimal.ZERO);
          accounting.setCollectorFee(fee);
          accounting.setCollectorFeeIva(feeIva);
        }
        break;
      case WITHDRAW:{
        // Calcula las comisiones segun el tipo de carga (WEB o POS)
        if (TransactionOriginType.WEB.equals(trxOriginType)) {
          fee = getPercentage().getWITHDRAW_WEB_FEE_AMOUNT().setScale(0, BigDecimal.ROUND_HALF_UP);
          feeIva = getCalculationsHelper().calculateFeeIva(fee).setScale(0, BigDecimal.ROUND_HALF_UP);
          accounting.setFee(fee);
          accounting.setFeeIva(feeIva);
          accounting.setCollectorFee(BigDecimal.ZERO);
          accounting.setCollectorFeeIva(BigDecimal.ZERO);
        }
        else {
          // Comision es Fija $200
          fee = getPercentage().getTOPUP_POS_FEE_AMOUNT().setScale(0, BigDecimal.ROUND_HALF_UP);
          feeIva = getCalculationsHelper().calculateFeeIva(fee).setScale(0, BigDecimal.ROUND_HALF_UP);
          accounting.setFee(BigDecimal.ZERO);
          accounting.setFeeIva(BigDecimal.ZERO);
          accounting.setCollectorFee(fee);
          accounting.setCollectorFeeIva(feeIva);
        }
        break;
      }

      default: {
        //TODO: Se debe verificar.
        accounting.setFee(BigDecimal.ZERO);
        accounting.setFeeIva(BigDecimal.ZERO);
        accounting.setCollectorFee(BigDecimal.ZERO);
        accounting.setCollectorFeeIva(BigDecimal.ZERO);
        break;
      }
    }
    accounting.setTransactionDate(movement.getFechaCreacion());
    accounting.setStatus(status);
    accounting.setAccountingStatus(accountingStatus);
    accounting.setFileId(0L);

    // Monto que afecta al saldo del usuario
    NewAmountAndCurrency10 amountToBalance = new NewAmountAndCurrency10();
    amountToBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);

    if(TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(movement.getTipofac()) || TipoFactura.RETIRO_TRANSFERENCIA.equals(movement.getTipofac())) {
      amountToBalance.setValue(movement.getImpfac().add(fee).add(feeIva));
    } else {
      amountToBalance.setValue(movement.getImpfac().subtract(fee).subtract(feeIva));
    }

    accounting.setAmountBalance(amountToBalance);


    accounting.setConciliationDate(accountingMovement.getReconciliationDate());

    return accounting;
  }

  public File createAccountingCSV(String filename, String fileId, List<AccountingData10> accountingData) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    // TODO: Agregar tasa de intercambio
    String[] header = new String[]{"ID_PREPAGO","ID_CONTABILIDAD", "ID_TRX", "ID_CUENTA_ORIGEN", "TIPO_TRX", "MOV_CONTABLE",
      "FECHA_TRX", "FECHA_CONCILIACION", "MONTO_TRX_PESOS", "MONTO_TRX_MCARD_PESOS", "MONTO_TRX_USD", "VALOR_USD",
      "DIF_TIPO_CAMBIO", "COMISION_PREPAGO_PESOS", "IVA_COMISION_PREPAGO_PESOS", "COMISION_RECAUDADOR_MC_PESOS",
      "IVA_COMISION_RECAUDADOR_MC_PESOS", "MONTO_AFECTO_A_SALDO_PESOS", "ID_CUENTA_DESTINO"};
    writer.writeNext(header);

    for (AccountingData10 mov : accountingData) {

      String transactionDate = getTimestampAtTimezone(mov.getTransactionDate(), null, null);

      ZonedDateTime conciliationDate = getTimestampAtTimezone(mov.getConciliationDate(), null);

      String reconciliationDate = "";

      /**
       * Se evalua la fecha de conciliacion:
       *  - Si la fecha de conciliacion es mayor a hoy, el movimiento no ha sido conciliado.
       *  - Si la fecha de conciliacion es menor a hoy, ya el movimiento fue conciliado.
       */
      if(conciliationDate.isBefore(ZonedDateTime.now())) {
        reconciliationDate = getTimestampAtTimezone(mov.getConciliationDate(), null, null);
      }

      String usdValue = "";
      if(mov.getAmountMastercard().getValue().doubleValue() > 0) {
        usdValue = (mov.getAmountMastercard().getValue().divide(mov.getAmountUsd().getValue(),2, RoundingMode.HALF_UP)).toString();
      }

      String[] data = new String[]{
        mov.getId().toString(), //ID_PREPAGO,
        fileId, //ID_LIQUIDACION,
        mov.getIdTransaction().toString(), //ID_TRX
        "", //ID_CUENTA_ORIGEN - Este campo es utilizado solo por MulticajaRed. No lo utiliza ni setea Prepago
        mov.getType().getValue(), //TIPO_TRX
        mov.getAccountingMovementType().getValue(), //MOV_CONTABLE
        transactionDate, //FECHA_TRX
        reconciliationDate, //FECHA_CONCILIACION
        mov.getAmount().getValue().toBigInteger().toString(), //MONTO_TRX_PESOS
        mov.getAmountMastercard().getValue().toBigInteger().toString(), //MONTO_TRX_MCARD_PESOS
        mov.getAmountUsd().getValue().toString(), //MONTO_TRX_USD
        usdValue, //VALOR_USD
        mov.getExchangeRateDif().toString(), //DIF_TIPO_CAMBIO
        mov.getFee().toBigInteger().toString(), //COMISION_PREPAGO_PESOS
        mov.getFeeIva().toBigInteger().toString(), //IVA_COMISION_PREPAGO_PESOS
        mov.getCollectorFee().toBigInteger().toString(), //COMISION_RECAUDADOR_MC_PESOS
        mov.getCollectorFeeIva().toBigInteger().toString(), //IVA_COMISION_RECAUDADOR_MC_PESOS
        mov.getAmountBalance().getValue().toBigInteger().toString(), //MONTO_AFECTO_A_SALDO_PESOS
        "" //ID_CUENTA_DESTINO - Este campo es utilizado solo por MulticajaRed. No lo utiliza ni setea Prepago
      };
      writer.writeNext(data);
    }
    writer.close();
    return file;
  }

  private File createAccountingReconciliationCSV(String filename, String fileId, List<AccountingData10> accountingData) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    // TODO: Agregar tasa de intercambio
    String[] header = new String[]{"ID_PREPAGO","ID_CONTABILIDAD", "ID_TRX", "ID_CUENTA_ORIGEN", "TIPO_TRX", "MOV_CONTABLE",
      "FECHA_TRX", "FECHA_CONCILIACION", "MONTO_TRX_PESOS", "MONTO_TRX_MCARD_PESOS", "MONTO_TRX_USD", "VALOR_USD",
      "DIF_TIPO_CAMBIO", "COMISION_PREPAGO_PESOS", "IVA_COMISION_PREPAGO_PESOS", "COMISION_RECAUDADOR_MC_PESOS",
      "IVA_COMISION_RECAUDADOR_MC_PESOS", "MONTO_AFECTO_A_SALDO_PESOS", "ID_CUENTA_DESTINO", "ESTADO_CONTABLE"};
    writer.writeNext(header);

    for (AccountingData10 mov : accountingData) {

      String transactionDate = getTimestampAtTimezone(mov.getTransactionDate(), null, null);

      ZonedDateTime conciliationDate = getTimestampAtTimezone(mov.getConciliationDate(), null);

      String reconciliationDate = "";

      /**
       * Se evalua la fecha de conciliacion:
       *  - Si la fecha de conciliacion es mayor a hoy, el movimiento no ha sido conciliado.
       *  - Si la fecha de conciliacion es menor a hoy, ya el movimiento fue conciliado.
       */
      if(conciliationDate.isBefore(ZonedDateTime.now())) {
        reconciliationDate = getTimestampAtTimezone(mov.getConciliationDate(), null, null);
      }

      String usdValue = "";
      if(mov.getAmountMastercard().getValue().doubleValue() > 0) {
        usdValue = (mov.getAmountMastercard().getValue().divide(mov.getAmountUsd().getValue(),2, RoundingMode.HALF_UP)).toString();
      }

      String[] data = new String[]{
        mov.getId().toString(), //ID_PREPAGO,
        fileId, //ID_LIQUIDACION,
        mov.getIdTransaction().toString(), //ID_TRX
        "", //ID_CUENTA_ORIGEN - Este campo es utilizado solo por MulticajaRed. No lo utiliza ni setea Prepago
        mov.getType().getValue(), //TIPO_TRX
        mov.getAccountingMovementType().getValue(), //MOV_CONTABLE
        transactionDate, //FECHA_TRX
        AccountingStatusType.NOT_OK.equals(mov.getAccountingStatus()) ? "" : reconciliationDate, //FECHA_CONCILIACION
        mov.getAmount().getValue().toBigInteger().toString(), //MONTO_TRX_PESOS
        mov.getAmountMastercard().getValue().toBigInteger().toString(), //MONTO_TRX_MCARD_PESOS
        mov.getAmountUsd().getValue().toString(), //MONTO_TRX_USD
        usdValue, //VALOR_USD
        mov.getExchangeRateDif().toString(), //DIF_TIPO_CAMBIO
        mov.getFee().toBigInteger().toString(), //COMISION_PREPAGO_PESOS
        mov.getFeeIva().toBigInteger().toString(), //IVA_COMISION_PREPAGO_PESOS
        mov.getCollectorFee().toBigInteger().toString(), //COMISION_RECAUDADOR_MC_PESOS
        mov.getCollectorFeeIva().toBigInteger().toString(), //IVA_COMISION_RECAUDADOR_MC_PESOS
        mov.getAmountBalance().getValue().toBigInteger().toString(), //MONTO_AFECTO_A_SALDO_PESOS
        "", //ID_CUENTA_DESTINO - Este campo es utilizado solo por MulticajaRed. No lo utiliza ni setea Prepago
        AccountingStatusType.NOT_OK.equals(mov.getAccountingStatus()) ? AccountingStatusType.NOT_CONFIRMED.getValue() : "" //ESTADO_CONTABLE
      };
      writer.writeNext(data);
    }
    writer.close();
    return file;
  }

  private static final String TIME_ZONE = "America/Santiago";
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  private String getTimestampAtTimezone(Timestamp ts, String timeZone, String pattern) {
    if(StringUtils.isAllBlank(pattern)) {
      pattern = DATE_PATTERN;
    }

    ZonedDateTime atTimezone = getTimestampAtTimezone(ts, timeZone);
    return atTimezone.format(DateTimeFormatter.ofPattern(pattern));
  }

  private ZonedDateTime getTimestampAtTimezone(Timestamp ts, String timeZone) {
    if(ts == null) {
      ts = Timestamp.from(Instant.now());
    }
    if(StringUtils.isAllBlank(timeZone)) {
      timeZone = TIME_ZONE;
    }
    LocalDateTime localDateTime = ts.toLocalDateTime();
    ZonedDateTime utc = localDateTime.atZone(ZoneOffset.UTC);

    if(timeZone.equalsIgnoreCase("utc")){
      return utc;
    }

    return utc.withZoneSameInstant(ZoneId.of(timeZone));
  }

  public void sendFile(String fileName, String emailAddress) throws Exception {

    String file = "accounting_files/" + fileName;

    FileInputStream attachmentFile = new FileInputStream(file);
    String fileToSend = Base64Utils.encodeToString(IOUtils.toByteArray(attachmentFile));

    attachmentFile.close();

    // Enviamos el archivo al mail de reportes diarios
    EmailBody emailBodyToSend = new EmailBody();

    emailBodyToSend.addAttached(fileToSend, MimeType.CSV.getValue(), fileName);
    emailBodyToSend.setTemplateData(null);
    emailBodyToSend.setTemplate(MailTemplates.TEMPLATE_MAIL_ACCOUNTING_FILE_OK);
    emailBodyToSend.setAddress(emailAddress);
    mailPrepaidEJBBean10.sendMailAsync(null, emailBodyToSend);

    Files.delete(Paths.get(file));
  }

  @Override
  public IpmFile saveIpmFileRecord(Map<String, Object> headers, IpmFile file) throws Exception {
    if(file == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "ipm file"));
    }
    if(StringUtils.isAllBlank(file.getFileName())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "file.name"));
    }
    if(file.getStatus() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "file.status"));
    }
    Object[] params = {
      file.getFileName() == null ? new NullParam(Types.VARCHAR) : new InParam(file.getFileName(), Types.VARCHAR),
      file.getFileId() == null ?  "" : new InParam(file.getFileId(), Types.VARCHAR),
      file.getMessageCount() == null ? 0 : new InParam( file.getMessageCount(), Types.NUMERIC),
      file.getStatus() == null ? new NullParam(Types.VARCHAR) : new InParam(file.getStatus().getValue(), Types.VARCHAR),
      new OutParam("_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_acc_create_ipm_file_v10", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_create_ipm_file_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    file.setId(getNumberUtils().toLong(resp.get("_id")));

    return file;
  }

  @Override
  public IpmFile updateIpmFileRecord(Map<String, Object> headers, IpmFile file) throws Exception {
    if(file == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "ipm file"));
    }
    if(file.getId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "ipm file.id"));
    }
    if(file.getStatus() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "file.status"));
    }

    Object[] params = {
      file.getId() == null ? new NullParam(Types.BIGINT) : new InParam(file.getId(), Types.BIGINT),
      file.getFileId() == null ?  "" : new InParam(file.getFileId(), Types.VARCHAR),
      file.getMessageCount() == null ? 0 : new InParam( file.getMessageCount(), Types.NUMERIC),
      file.getStatus() == null ? new NullParam(Types.VARCHAR) : new InParam(file.getStatus().getValue(), Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_acc_update_ipm_file_v10", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_update_ipm_file_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }

    return file;
  }

  public List<IpmFile> findIpmFile(Map<String, Object> headers, Long id, String fileName, String fileId, IpmFileStatus status) throws Exception {
    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      fileName != null ? fileName : new NullParam(Types.VARCHAR),
      fileId != null ? fileId : new NullParam(Types.VARCHAR),
      status != null ? status.getValue() : new NullParam(Types.VARCHAR)
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      IpmFile c = new IpmFile();
      c.setId(getNumberUtils().toLong(row.get("_id"), null));
      c.setFileId(String.valueOf(row.get("_file_id")));
      c.setFileName(String.valueOf(row.get("_file_name")));
      c.setStatus(IpmFileStatus.valueOfEnum(row.get("_status").toString().trim()));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_create_date"));
      timestamps.setUpdatedAt((Timestamp)row.get("_update_date"));
      c.setTimestamps(timestamps);
      return c;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_search_ipm_file_v10",  rm, params);
    List<IpmFile> res = (List<IpmFile>)resp.get("result");
    return res != null ? res : Collections.EMPTY_LIST;
  }

  @Override
  public void convertIpmFileToCsv(String ipmFileName) throws Exception {
    if(StringUtils.isAllBlank(ipmFileName)) {
      throw new Exception("Ipm file name is null or empty");
    }

    // Se valida que este el archivo yml con las propiedades a extraer del IPM
    if(!Files.exists(Paths.get("./mideu.yml"), LinkOption.NOFOLLOW_LINKS)) {
      throw new Exception("config file [./mideu.yml] file does not exists");
    }

    /**
     * Se procesa el archivo IPM con libreria python
     * https://github.com/adelosa/mciutil
     */

    Runtime rt = Runtime.getRuntime();
    Process pr = rt.exec("mideu extract " + ipmFileName);

    BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

    String line = null;

    while((line=input.readLine()) != null) {
      System.out.println(line);
    }

    int exitVal = pr.waitFor();
    if(exitVal != 0){
      throw new Exception("Python file conversion exit with code " + exitVal);
    }
    log.info("Python file conversion exit with code " + exitVal);
  }

  @Override
  public IpmFile processIpmFile(Map<String, Object> headers, File file, IpmFile ipmFile) throws Exception {
    if(file == null || !file.exists()){
      throw new Exception("Csv file is null or does not exists");
    }

    if(ipmFile == null) {
      throw new Exception("IpmFile object is null");
    }

    FileReader csvFile = new FileReader(file);

    Boolean isReadSuccess;

    try {
      ipmFile = MastercardIpmFileHelper.readCsvIpmData(csvFile, ipmFile);
      ipmFile.setStatus(IpmFileStatus.PROCESSING);
      csvFile.close();
      isReadSuccess = Boolean.TRUE;
    } catch(Exception e) {
      log.error(String.format("Error reading csv file from IPM file -> [%s]", file.getName()), e);
      ipmFile = new IpmFile();
      ipmFile.setStatus(IpmFileStatus.ERROR);
      csvFile.close();
      throw e;
    }

    if(ipmFile.getId() == null) {
      ipmFile = this.saveIpmFileRecord(null, ipmFile);
    } else {
      ipmFile.setStatus(IpmFileStatus.PROCESSING);
      ipmFile = this.updateIpmFileRecord(null, ipmFile);
    }


    if(!isReadSuccess) {
      throw new Exception(String.format("Error reading csv file from IPM file -> [%s]", file.getName()));
    }

    Boolean isSuspicious = Boolean.FALSE;
    try {
      MastercardIpmFileHelper.validateFile(ipmFile);
    } catch(Exception e) {
      if(ipmFile.getHeader() != null && ipmFile.getHeader().getFileId() != null) {
        ipmFile.setFileId(ipmFile.getHeader().getFileId());
      }
      ipmFile.setStatus(IpmFileStatus.SUSPICIOUS);
      ipmFile = this.updateIpmFileRecord(null, ipmFile);
      isSuspicious = Boolean.TRUE;
    }

    if(isSuspicious) {
      throw new Exception(String.format("IPM file seems suspicious -> [%s]", file.getName()));
    }

    ipmFile.setFileId(ipmFile.getHeader().getFileId());
    this.updateIpmFileRecord(null, ipmFile);

    return ipmFile;
  }

  @Override
  public void processIpmFileTransactions(Map<String, Object> headers, IpmFile ipmFile) throws Exception {
    if(ipmFile == null) {
      throw new Exception("IpmFile object null");
    }

    if(ipmFile.getTransactions().isEmpty()) {
      log.info(String.format("There are not transactions to process in file [%s]", ipmFile.getFileName()));
      return;
    }

    List<AccountingData10> transactions = new ArrayList<>();
    List<PrepaidMovement10> movement10s = new ArrayList<>();

    for (IpmMessage trx: ipmFile.getTransactions()) {
      System.out.println(trx);
      AccountingData10 acc = null;
      PrepaidMovement10 prepaidMovement10 = null;
      try {
        // Se cambian los * por X que es como esta en nuestra BD.
        prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementByNumAutAndPan(trx.getPan().replace("*","X"),String.valueOf(trx.getApprovalCode()),MovementOriginType.OPE);
      } catch (Exception e) {
        log.error("PrepaidMovement10 Not Found");
      }
      if(prepaidMovement10 != null){
        acc = this.searchAccountingByIdTrx(headers,prepaidMovement10.getId());
      }

      if(acc == null){
        acc = new AccountingData10();
        acc.setOrigin(AccountingOriginType.IPM);
        acc.setType(this.getTransactionType(trx));
        acc.setAccountingMovementType(this.getMovementType(trx));
        acc.setIdTransaction(Long.valueOf(trx.getApprovalCode()));
      }

      acc.setTransactionDate(Timestamp.from(trx.getTransactionLocalDate().toInstant()));
      acc.setAmount(new NewAmountAndCurrency10(BigDecimal.ZERO));

      // Monto en pesos
      acc.setAmountMastercard(new NewAmountAndCurrency10(
        IpmMessage.movePeriod(
          NumberUtils.getInstance().toLong(trx.getCardholderBillingAmount()),
          ipmFile.getCurrencyExponents().get(
            trx.getCardholderBillingCurrencyCode()
          )
        ).setScale(0, BigDecimal.ROUND_HALF_UP)
      ));

      //Monto en usd
      acc.setAmountUsd(new NewAmountAndCurrency10(
        IpmMessage.movePeriod(
          NumberUtils.getInstance().toLong(trx.getReconciliationAmount()),
          ipmFile.getCurrencyExponents().get(
            trx.getReconciliationCurrencyCode()
          )
        ).setScale(2, BigDecimal.ROUND_UNNECESSARY)
      ));

      BigDecimal fee = BigDecimal.ZERO;
      BigDecimal iva = BigDecimal.ZERO;
      BigDecimal exchangeRateDiff = BigDecimal.ZERO;

      switch (acc.getType()) {
        case COMPRA_SUSCRIPCION:
          // 1% del monto CLP (DE6)
          fee = this.getCalculationsHelper().calculatePercentageValue(acc.getAmount().getValue(), this.getCalculationsHelper().getCalculatorParameter10().getSUBSCRIPTION_PURCHASE_FEE_PERCENTAGE());

          // IVA de la comision
          iva = this.getCalculationsHelper().calculatePercentageValue(fee, BigDecimal.valueOf(this.getCalculationsHelper().getCalculatorParameter10().getIVA()));
          break;
        case COMPRA_PESOS:
          //  Monto fijo
          BigDecimal fixedAmount = this.getCalculationsHelper().getCalculatorParameter10().getCLP_PURCHASE_FEE_AMOUNT();

          // 1.5% del monto CLP (DE6)
          BigDecimal percentage = this.getCalculationsHelper().calculatePercentageValue(acc.getAmount().getValue(), this.getCalculationsHelper().getCalculatorParameter10().getCLP_PURCHASE_FEE_PERCENTAGE());

          fee = fixedAmount.add(percentage);

          // IVA monto fijo + IVA monto variable
          iva = (getCalculationsHelper().calculatePercentageValue(fixedAmount, BigDecimal.valueOf(this.getCalculationsHelper().getCalculatorParameter10().getIVA())))
            .add(getCalculationsHelper().calculatePercentageValue(percentage, BigDecimal.valueOf(this.getCalculationsHelper().getCalculatorParameter10().getIVA())));

          break;
        case COMPRA_MONEDA:
          //Monto fijo
          fee = this.getCalculationsHelper().getCalculatorParameter10().getOTHER_CURRENCY_PURCHASE_FEE_AMOUNT();

          // IVA monto fijo
          iva = this.getCalculationsHelper().calculatePercentageValue(fee, BigDecimal.valueOf(this.getCalculationsHelper().getCalculatorParameter10().getIVA()));

          //  1.5% del monto CLP (DE6)
          exchangeRateDiff = this.getCalculationsHelper().calculatePercentageValue(acc.getAmountMastercard().getValue(), this.getCalculationsHelper().getCalculatorParameter10().getOTHER_CURRENCY_PURCHASE_EXCHANGE_RATE_PERCENTAGE());
          break;
      }

      acc.setFee(fee.setScale(0, BigDecimal.ROUND_HALF_UP));
      acc.setFeeIva(iva.setScale(0, BigDecimal.ROUND_HALF_UP));
      acc.setExchangeRateDif(exchangeRateDiff);
      acc.setFileId(0L);
      // Monto que afecta al saldo del cliente
      acc.setAmountBalance(new NewAmountAndCurrency10(acc.getAmountMastercard().getValue().add(acc.getFee()).add(acc.getFeeIva()).add(acc.getExchangeRateDif()).setScale(0, BigDecimal.ROUND_UP)));
      acc.setCollectorFee(BigDecimal.ZERO);
      acc.setCollectorFeeIva(BigDecimal.ZERO);

      //TODO: Revisar Fecha. Usar fecha del archivo IPM?
      acc.setConciliationDate(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()));
      acc.setStatus(AccountingStatusType.PENDING);
      acc.setAccountingStatus(AccountingStatusType.PENDING);

      // Si el movimiento no existe en nuestra BD se agrega  como uno nuevo, si no  se actualiza.
      if(prepaidMovement10 == null) {
        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getPrepaidCardEJB10().getPrepaidCardByEncryptedPan(null, getEncryptUtil().encrypt(trx.getPan()));
        // Se agrega movimiento solo si existe la tarjeta.
        if(prepaidCard10 != null){
          PrepaidMovement10 mov = buildMovementAut(prepaidCard10.getIdUser(), prepaidCard10 ,trx,getTipoMovimientoFromAccTxType(acc.getType()),getTipoFacFromAccTxType(acc.getType()));
          movement10s.add(mov);
        }else{
          //TODO: Si la tarjeta no existe se debe investigar.
        }
        transactions.add(acc);
      }
      else {
        // Si El movimiento ya existe, se actualiza la data y los status.
        System.out.println("!! OPD Accounting");
        // TODO: deberia modificar es el accountingStatus
        acc.setStatus(AccountingStatusType.OK);
        this.updateAccountingDataFull(headers,acc); // Actualizar todos los valores de Accounting
        // Busca el movimiento de clearing  y luego le actualiza el status
        ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(headers,acc.getId());
        getPrepaidClearingEJBBean10().updateClearingData(headers,clearingData10.getId(),AccountingStatusType.PENDING);
        // Movimiento actualizado a procesado OK
        getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(headers,prepaidMovement10.getId(),PrepaidMovementStatus.PROCESS_OK);

        //TODO: verificar si estos movimientos que solo vinieron en el IPM se deben agregar a investigar
      }
    }
    // Se añaden los movimientos que no llegaron desde un Archivo de operaciones diarias.
    this.getPrepaidMovementEJBBean10().addPrepaidMovement(headers,movement10s);
    // Se guarda data en Accounting
    this.saveAccountingData(null, transactions);
    // Se guarda la data en Clearing.
    this.saveClearingData(transactions);
    ipmFile.setStatus(IpmFileStatus.PROCESSED);
    this.updateIpmFileRecord(null, ipmFile);

  }
  private PrepaidMovementType getTipoMovimientoFromAccTxType(AccountingTxType txType){
    PrepaidMovementType movementType = null;
    switch (txType) {
      case COMPRA_PESOS:
      case COMPRA_MONEDA:
        movementType = PrepaidMovementType.PURCHASE;
        break;
      case COMPRA_SUSCRIPCION:
        movementType = PrepaidMovementType.SUSCRIPTION;
        break;
    }
   return movementType;
  }
  private TipoFactura getTipoFacFromAccTxType(AccountingTxType txType){
    TipoFactura tipoFactura = null;
    switch (txType) {
      case COMPRA_PESOS:
      case COMPRA_MONEDA:
        tipoFactura = TipoFactura.COMPRA_INTERNACIONAL;
        break;
      case COMPRA_SUSCRIPCION:
        tipoFactura = TipoFactura.COMPRA_INTERNACIONAL;
        break;
    }
    return tipoFactura;
  }
  private PrepaidMovement10 buildMovementAut(Long userId, PrepaidCard10 prepaidCard, IpmMessage batchTrx,PrepaidMovementType prepaidMovementType, TipoFactura tipoFactura) {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    prepaidMovement.setIdMovimientoRef(0L);
    prepaidMovement.setIdPrepaidUser(userId);
    prepaidMovement.setIdTxExterno(batchTrx.getApprovalCode().toString());
    prepaidMovement.setTipoMovimiento(prepaidMovementType);
    prepaidMovement.setMonto(getNumberUtils().toBigDecimal(batchTrx.getCardholderBillingAmount()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);

    String centalta = prepaidCard.getProcessorUserId().substring(4, 8);
    String cuenta = prepaidCard.getProcessorUserId().substring(8, 20);

    prepaidMovement.setCodent("");//Desde tarjeta Contrato
    prepaidMovement.setCentalta(centalta);//Desde tarjeta Contrato
    prepaidMovement.setCuenta(cuenta);//Desde tarjeta Contrato
    prepaidMovement.setClamon(CodigoMoneda.fromValue(NumberUtils.getInstance().toInteger(batchTrx.getCardholderBillingCurrencyCode())));

    //
    prepaidMovement.setTipofac(tipoFactura);// Revisar
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFactura.getCorrector()));

    prepaidMovement.setFecfac(java.sql.Date.valueOf(batchTrx.getTransactionLocalDate().toLocalDate()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(batchTrx.getPan());
    prepaidMovement.setClamondiv(batchTrx.getTransactionCurrencyCode());

    prepaidMovement.setImpdiv(getNumberUtils().toBigDecimal(batchTrx.getTransactionAmount()));
    prepaidMovement.setImpfac(getNumberUtils().toBigDecimal(batchTrx.getCardholderBillingAmount()));
    prepaidMovement.setCmbapli(0);
    prepaidMovement.setNumaut(batchTrx.getApprovalCode().toString());
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodact(0);
    prepaidMovement.setImpliq(BigDecimal.ZERO);
    prepaidMovement.setClamonliq(0);
    prepaidMovement.setNompob("");
    prepaidMovement.setNumextcta(0);
    prepaidMovement.setNummovext(0);
    prepaidMovement.setClamone(0);
    prepaidMovement.setTipolin("");
    prepaidMovement.setLinref(0);
    prepaidMovement.setNumbencta(1);
    prepaidMovement.setNumplastico(0L);
    prepaidMovement.setCodent("");
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setCodcom(batchTrx.getCardAcceptorId());
    prepaidMovement.setNomcomred(batchTrx.getMerchantName());
    prepaidMovement.setOriginType(MovementOriginType.OPE);

    //Tecnocom No conciliado
    prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
    // Switch Conciliado ya que no pasa por switch
    prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);

    return prepaidMovement;
  }



  private void saveClearingData(List<AccountingData10> accounting10s){
    for(AccountingData10 data: accounting10s){
      try {
        ClearingData10 clearing10 = new ClearingData10();
        clearing10.setAccountingId(data.getId());
        clearing10.setUserAccountId(0L);
        clearing10.setStatus(AccountingStatusType.PENDING);
        getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
        log.info("Save Clearing data OK");
      } catch (Exception e) {
        e.printStackTrace();
        log.info("Error, Verificar movimiento: "+data.getId());
      }
    }
  }

  @Override
  public AccountingTxType getTransactionType(IpmMessage trx) throws Exception {
    if(trx == null){
      throw new Exception("Transaction is null");
    }
    if(trx.getTransactionCurrencyCode() == null){
      throw new Exception("Transaction currency code is null");
    }
    if(StringUtils.isAllEmpty(trx.getMerchantName())){
      throw new Exception("MerchantName is null or empty");
    }

    if(CodigoMoneda.CHILE_CLP.getValue().equals(trx.getTransactionCurrencyCode())
      && isSubscriptionMerchant(trx.getMerchantName())) {
      return AccountingTxType.COMPRA_SUSCRIPCION;
    } else if(CodigoMoneda.CHILE_CLP.getValue().equals(trx.getTransactionCurrencyCode())) {
      return AccountingTxType.COMPRA_PESOS;
    } else {
      return AccountingTxType.COMPRA_MONEDA;
    }
  }

  public AccountingMovementType getMovementType(IpmMessage trx) throws Exception {
    if(trx == null){
      throw new Exception("Transaction is null");
    }
    if(trx.getTransactionCurrencyCode() == null){
      throw new Exception("Transaction currency code is null");
    }
    if(StringUtils.isAllEmpty(trx.getMerchantName())){
      throw new Exception("MerchantName is null or empty");
    }

    if(CodigoMoneda.CHILE_CLP.getValue().equals(trx.getTransactionCurrencyCode())
      && isSubscriptionMerchant(trx.getMerchantName())) {
      return AccountingMovementType.SUSCRIPCION;
    } else if(CodigoMoneda.CHILE_CLP.getValue().equals(trx.getTransactionCurrencyCode())) {
      return AccountingMovementType.COMPRA_PESOS;
    } else {
      return AccountingMovementType.COMPRA_MONEDA;
    }
  }

  @Override
  public Boolean isSubscriptionMerchant(final String merchantName) throws Exception {
    if(StringUtils.isAllBlank(merchantName)) {
      throw new Exception("merchantName is null or empty");
    }

    //TODO: externalizar en parametro la lista de comercios?
    List<String> merchants = Arrays.asList("Netflix", "Spotify", "Uber", "Itunes");

    return merchants
      .stream()
      .anyMatch(m -> merchantName.toLowerCase().contains(m.toLowerCase()));
  }

  @Override
  public void updateAccountingData(Map<String, Object> header, Long id, Long fileId, AccountingStatusType status) throws Exception {
    if(fileId == null && status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "allNull"));
    }

    this.updateAccountingData(header, id, fileId, status, null, null);
  }

  @Override
  public void updateAccountingStatus(Map<String, Object> header, Long id, AccountingStatusType accountingStatus) throws Exception {
    if(accountingStatus == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingStatus"));
    }

    this.updateAccountingData(header, id, null, null, accountingStatus, null);
  }

  @Override
  public void updateStatus(Map<String, Object> header, Long id, AccountingStatusType status) throws Exception {
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    this.updateAccountingData(header, id, null, status, null, null);
  }

  @Override
  public void updateAccountingStatusAndConciliationDate(Map<String, Object> header, Long id, AccountingStatusType accountingStatus, String conciliationDate) throws Exception {
    if(accountingStatus == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingStatus"));
    }
    if(StringUtils.isAllBlank(conciliationDate)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "conciliationDate"));
    }

    this.updateAccountingData(header, id, null, null, accountingStatus, conciliationDate);
  }

  private void updateAccountingData(Map<String, Object> header, Long id, Long fileId, AccountingStatusType status, AccountingStatusType accountingStatus, String conciliationDate) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    Object[] params = {
      new InParam(id, Types.BIGINT),
      fileId == null ? new NullParam(Types.BIGINT) : new InParam(fileId, Types.BIGINT),
      status == null ? new NullParam(Types.VARCHAR) : new InParam(status.getValue(), Types.VARCHAR),
      conciliationDate == null ? new NullParam(Types.VARCHAR) : new InParam(conciliationDate, Types.VARCHAR),
      accountingStatus == null ? new NullParam(Types.VARCHAR) : new InParam(accountingStatus.getValue(), Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_update_accounting_data_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_update_accounting_data_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

  /**
   * Busca los movimientos de accounting segun estado y rango de fechas. Las fechas ya deben venir en UTC.
   * @param headers
   * @param from fecha desde en UTC
   * @param to fecha hasta en UTC
   * @param status
   * @return
   * @throws Exception
   */
  public List<AccountingData10> getAccountingDataForFile(Map<String, Object> headers, LocalDateTime from, LocalDateTime to, AccountingStatusType status, AccountingStatusType accountingStatus) throws Exception {

    if(from == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "from"));
    }

    if(to == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "to"));
    }

    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    String f = from.format(formatter);
    String t = to.format(formatter);

    Object[] params = {
      f == null ? new NullParam(Types.VARCHAR) : f,
      t == null ? new NullParam(Types.VARCHAR) : t,
      status == null ? new NullParam(Types.VARCHAR) : status.getValue(),
      accountingStatus == null ? new NullParam(Types.VARCHAR) : accountingStatus.getValue()
    };
    log.info(params);
    RowMapper rm = (Map<String, Object> row) -> {
      AccountingData10 data = new AccountingData10();

      data.setId(getNumberUtils().toLong(row.get("_id")));
      data.setIdTransaction(getNumberUtils().toLong(row.get("_id_tx")));
      data.setType(AccountingTxType.fromValue(String.valueOf(row.get("_type"))));
      data.setOrigin(AccountingOriginType.fromValue(String.valueOf(row.get("_origin"))));
      data.setAccountingMovementType(AccountingMovementType.fromValue(String.valueOf(row.get("_accounting_mov"))));
      data.setFileId(getNumberUtils().toLong(row.get("_file_id")));

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setValue(getNumberUtils().toBigDecimal(row.get("_amount")));
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      data.setAmount(amount);

      NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
      amountUsd.setValue(getNumberUtils().toBigDecimal(row.get("_amount_usd")));
      amountUsd.setCurrencyCode(CodigoMoneda.USA_USD);
      data.setAmountUsd(amountUsd);

      NewAmountAndCurrency10 amountMc = new NewAmountAndCurrency10();
      amountMc.setValue(getNumberUtils().toBigDecimal(row.get("_amount_mcar")));
      amountMc.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      data.setAmountMastercard(amountMc);

      data.setExchangeRateDif(getNumberUtils().toBigDecimal(row.get("_exchange_rate_dif")));
      data.setFee(getNumberUtils().toBigDecimal(row.get("_fee")));
      data.setFeeIva(getNumberUtils().toBigDecimal(row.get("_fee_iva")));
      data.setCollectorFee(getNumberUtils().toBigDecimal(row.get("_collector_fee")));
      data.setCollectorFeeIva(getNumberUtils().toBigDecimal(row.get("_collector_fee_iva")));

      NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
      amountBalance.setValue(getNumberUtils().toBigDecimal(row.get("_amount_balance")));
      amountBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      data.setAmountBalance(amountBalance);

      data.setStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_status"))));
      data.setTransactionDate((Timestamp) row.get("_transaction_date"));
      data.setConciliationDate((Timestamp) row.get("_conciliation_date"));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_create_date"));
      timestamps.setUpdatedAt((Timestamp)row.get("_update_date"));
      data.setTimestamps(timestamps);

      data.setAccountingStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_accounting_status"))));

      return data;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_search_accounting_data_for_file_v10", rm, params);
    log.info("Respuesta Busca Movimiento: "+ resp);

    List<AccountingData10> result = (List<AccountingData10>)resp.get("result");
    return result != null ? result : Collections.EMPTY_LIST;
  }

  /**
   * Busca los movimientos en accounting y genera un archivo csv que se envia por correo
   * @param headers
   * @param date
   * @return
   * @throws Exception
   */
  public AccountingFiles10 generateAccountingFile(Map<String, Object> headers, ZonedDateTime date) throws Exception {
    if(date == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    // primer dia del mes anterior
    ZonedDateTime firstDay = date.minusMonths(1)
      .with(TemporalAdjusters.firstDayOfMonth())
      .withHour(0).withMinute(0).withSecond(0).withNano(0);

    // ultimo dia del mes anterior
    ZonedDateTime lastDay = date
      .minusMonths(1)
      .with(TemporalAdjusters.lastDayOfMonth())
      .withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime firstDayUtc = ZonedDateTime.ofInstant(firstDay.toInstant(), ZoneOffset.UTC);
    ZonedDateTime lastDayUtc = ZonedDateTime.ofInstant(lastDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime ldtFrom = firstDayUtc.toLocalDateTime();
    LocalDateTime ldtTo = lastDayUtc.toLocalDateTime();

    List<AccountingData10> movements = this.getAccountingDataForFile(null, ldtFrom, ldtTo, AccountingStatusType.PENDING, AccountingStatusType.OK);

    if(movements.isEmpty()){
      return null;
    }

    String directoryName = "accounting_files";
    File directory = new File(directoryName);
    if (! directory.exists()){
      directory.mkdir();
    }

    String fileId = date.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
    String fileName = String.format("TRX_PREPAGO_%s.CSV", fileId);

    createAccountingCSV(directoryName + "/" + fileName, fileId, movements); // Crear archivo csv temporal

    AccountingFiles10 file = new AccountingFiles10();
    file.setStatus(AccountingStatusType.PENDING);
    file.setName(fileName);
    file.setFileId(fileId);
    file.setFileFormatType(AccountingFileFormatType.CSV);
    file.setFileType(AccountingFileType.ACCOUNTING);
    file.setUrl("");

    file = getPrepaidAccountingFileEJBBean10().insertAccountingFile(headers, file);

    movements = this.updateAccountingData(headers, movements, file.getId());

    return file;
  }

  public AccountingFiles10 generatePendingConciliationResultFile(Map<String, Object> headers, ZonedDateTime date) throws Exception {
    if(date == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    // primer dia de dos meses antes
    ZonedDateTime firstDay = date
      .minusMonths(2)
      .with(TemporalAdjusters.firstDayOfMonth())
      .withHour(0).withMinute(0).withSecond(0).withNano(0);

    // ultimo dia de dos meses antes
    ZonedDateTime lastDay = date
      .minusMonths(2)
      .with(TemporalAdjusters.lastDayOfMonth())
      .withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime firstDayUtc = ZonedDateTime.ofInstant(firstDay.toInstant(), ZoneOffset.UTC);
    ZonedDateTime lastDayUtc = ZonedDateTime.ofInstant(lastDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime ldtFrom = firstDayUtc.toLocalDateTime();
    LocalDateTime ldtTo = lastDayUtc.toLocalDateTime();

    List<AccountingData10> movements = this.getAccountingDataForFile(null, ldtFrom, ldtTo, AccountingStatusType.SENT_PENDING_CON, null);

    if(movements.isEmpty()){
      return null;
    }

    String directoryName = "accounting_files";
    File directory = new File(directoryName);
    if (! directory.exists()){
      directory.mkdir();
    }

    String fileId = date.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
    String month = date.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyyMM"));
    String fileName = String.format("%s_CUADRATURA_%s_PREPAGO.CSV", fileId, month);

    createAccountingReconciliationCSV(directoryName + "/" + fileName, fileId, movements); // Crear archivo csv temporal

    AccountingFiles10 file = new AccountingFiles10();
    file.setStatus(AccountingStatusType.PENDING);
    file.setName(fileName);
    file.setFileId(fileId);
    file.setFileFormatType(AccountingFileFormatType.CSV);
    file.setFileType(AccountingFileType.ACCOUNTING_RECONCILIATION);
    file.setUrl("");

    file = getPrepaidAccountingFileEJBBean10().insertAccountingFile(headers, file);

    for (AccountingData10 m : movements) {
      ZonedDateTime conciliationDate = getTimestampAtTimezone(m.getConciliationDate(), null);
      AccountingStatusType status = conciliationDate.isBefore(ZonedDateTime.now()) ? m.getAccountingStatus() : AccountingStatusType.NOT_CONFIRMED;

      this.updateAccountingData(null, m.getId(), file.getId(), AccountingStatusType.SENT, status, null);
    }

    return file;
  }

  private List<AccountingData10> updateAccountingData (Map<String, Object> header, List<AccountingData10> data, Long fileId) throws Exception {
    if(data == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "data"));
    }

    for (AccountingData10 m : data) {
      ZonedDateTime conciliationDate = getTimestampAtTimezone(m.getConciliationDate(), null);
      AccountingStatusType status = conciliationDate.isBefore(ZonedDateTime.now()) ? AccountingStatusType.SENT : AccountingStatusType.SENT_PENDING_CON;
      this.updateAccountingData(null, m.getId(), fileId, status, null, null);
    }
    return data;
  }

  @Override
  public void expireIpmMovements() throws SQLException {
    getDbUtils().execute(String.format("%s.create_sp_mc_acc_expire_old_ipm_movements_v10", getSchemaAccounting()));
  }

}
