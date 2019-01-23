package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.helpers.mastercard.MastercardIpmFileHelper;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.accounting.helpers.mastercard.model.IpmMessage;
import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.MailPrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10;
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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cl.multicaja.core.model.Errors.ERROR_DE_COMUNICACION_CON_BBDD;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

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

  public List<AccountingData10> searchAccountingData(Map<String, Object> header, LocalDateTime dateToSearch) throws Exception {

    if(dateToSearch == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "dateToSearch"));
    }

    Object[] params = {
      dateToSearch
    };

    RowMapper rm = (Map<String, Object> row) -> {
      AccountingData10 account = new AccountingData10();

      account.setIdTransaction(getNumberUtils().toLong(row.get("_id_tx")));
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setValue(getNumberUtils().toBigDecimal(row.get("_amount")));
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);

      account.setAmount(amount);

      NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
      amount.setValue(getNumberUtils().toBigDecimal(row.get("_amount_usd")));
      amount.setCurrencyCode(CodigoMoneda.USA_USD);

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
      account.setFileId(getNumberUtils().toLong(row.get("file_id")));
      return account;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_search_accounting_data_v10", rm, params);
    log.info("Respuesta Busca Movimiento: "+resp);
    return (List)resp.get("result");
  }

  @Override
  public List<AccountingData10> searchAccountingData(Map<String, Object> header, Date dateToSearch) throws Exception {
    return null;
  }

  public List<AccountingData10> saveAccountingData (Map<String, Object> header, List<AccountingData10> accounting10s) throws Exception {
    if(accounting10s == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accounting10s"));
    }
    List<AccountingData10> accounting10sFinal = new ArrayList<>();
    for(AccountingData10 account : accounting10s) {

      if(account.getIdTransaction() == null){
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getIdTransaction"));
      }
      if(account.getType() == null){
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getType"));
      }
      if(account.getOrigin() == null){
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getOrigin"));
      }
      if(account.getTransactionDate() == null){
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getTransactionDate"));
      }
      if(account.getStatus() == null){
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getStatus"));
      }

      Object[] params = {
        new InParam(account.getIdTransaction(), Types.BIGINT),
        new InParam(account.getType().getValue(), Types.VARCHAR),
        new InParam(account.getAccountingMovementType().getValue(), Types.VARCHAR),
        new InParam(account.getOrigin().getValue(), Types.VARCHAR),
        account.getAmount() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getAmount().getValue(), Types.NUMERIC),
        account.getAmount().getCurrencyCode() == null ?  new NullParam(Types.NUMERIC) : new InParam(account.getAmount().getCurrencyCode().getValue(), Types.NUMERIC),
        account.getAmountUsd().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( account.getAmountUsd().getValue(), Types.NUMERIC),
        account.getAmountMastercard().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( account.getAmountMastercard().getValue(), Types.NUMERIC),
        account.getExchangeRateDif() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getExchangeRateDif(), Types.NUMERIC),
        account.getFee() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getFee(), Types.NUMERIC),
        account.getFeeIva() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getFeeIva(),Types.NUMERIC),
        account.getCollectorFee() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getCollectorFee(), Types.NUMERIC),
        account.getCollectorFeeIva() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getCollectorFeeIva(),Types.NUMERIC),
        account.getAmountBalance().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( account.getAmountBalance().getValue(),Types.NUMERIC),
        new InParam(account.getTransactionDateInFormat(),Types.VARCHAR),
        new InParam(account.getConciliationDateInFormat(),Types.VARCHAR),
        new InParam(account.getStatus().getValue(), Types.VARCHAR),
        new InParam(account.getFileId(), Types.BIGINT),
        new OutParam("_id", Types.BIGINT),
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_prp_insert_accounting_data_v10",params);

      if (!"0".equals(resp.get("_error_code"))) {
        log.error("mc_prp_insert_accounting_data_v10 resp: " + resp);
        throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
      }
      account.setId(getNumberUtils().toLong(resp.get("_id")));
      log.info("Accounting Insertado Id: "+getNumberUtils().toLong(resp.get("_id")));
      accounting10sFinal.add(account);
    }
    return accounting10sFinal;
  }

  /**
   * Busca los movimientos conciliados para agregarlos en la tabla de contabilidad.
   *
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  public List<PrepaidMovement10> getReconciledPrepaidMovementsForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception {
    if(date == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    String ts = date.minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Object[] params = {
      ts,
      ReconciliationStatusType.RECONCILED.getValue(),
      BusinessStatusType.OK.getValue(),
      AccountingOriginType.MOVEMENT.getValue()
    };

    RowMapper rm = (Map<String, Object> row) -> {
      try{
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
        p.setImpdiv(getNumberUtils().toLong(row.get("_impdiv")));
        p.setImpfac(getNumberUtils().toBigDecimal(row.get("_impfac")));
        p.setCmbapli(getNumberUtils().toInteger(row.get("_cmbapli")));
        p.setNumaut(String.valueOf(row.get("_numaut")));
        p.setIndproaje(IndicadorPropiaAjena.fromValue(String.valueOf(row.get("_indproaje"))));
        p.setCodcom(String.valueOf(row.get("_codcom")));
        p.setCodact(getNumberUtils().toInteger(row.get("_codact")));
        p.setImpliq(getNumberUtils().toLong(row.get("_impliq")));
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
        log.info("RowMapper getPrepaidMovements");
        log.info(p);

        return p;
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
    List<PrepaidMovement10> movements = this.getReconciledPrepaidMovementsForAccounting(headers, date);

    if(movements != null) {

      log.info("Se filtran los movimientos por tipo de factura -> Cargas y Retiros");
      movements = movements.stream().filter(movement -> (TipoFactura.CARGA_TRANSFERENCIA.equals(movement.getTipofac()) ||
        TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(movement.getTipofac()) ||
        TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(movement.getTipofac()) ||
        TipoFactura.RETIRO_TRANSFERENCIA.equals(movement.getTipofac())
      ))
        .collect(Collectors.toList());


      List<AccountingData10> accountingMovements = new ArrayList<>();

      for (PrepaidMovement10 m : movements) {

        AccountingTxType type = AccountingTxType.RETIRO_WEB;;
        AccountingMovementType movementType = AccountingMovementType.RETIRO_WEB;

        if(TipoFactura.CARGA_TRANSFERENCIA.equals(m.getTipofac())) {
          type = AccountingTxType.CARGA_WEB;
          movementType = AccountingMovementType.CARGA_WEB;
        } else if(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(m.getTipofac())) {
          type = AccountingTxType.CARGA_POS;
          movementType =AccountingMovementType.CARGA_POS;
        } else if(TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(m.getTipofac())) {
          type = AccountingTxType.RETIRO_POS;
          movementType =AccountingMovementType.RETIRO_POS;
        }

        AccountingData10 accounting = new AccountingData10();
        accounting.setIdTransaction(m.getId());
        accounting.setOrigin(AccountingOriginType.MOVEMENT);
        accounting.setType(type);
        accounting.setAccountingMovementType(movementType);
        accounting.setAmount(new NewAmountAndCurrency10(m.getImpfac()));

        //Se colocan en 0 ya que solo se procesan cargas y retiros
        accounting.setAmountUsd(new NewAmountAndCurrency10(BigDecimal.ZERO));
        accounting.setExchangeRateDif(BigDecimal.ZERO);

        //Se calcula la comision del movimiento
        BigDecimal fee = BigDecimal.ZERO;
        switch (m.getTipoMovimiento()) {
          case TOPUP:
            // Calcula las comisiones segun el tipo de carga (WEB o POS)
            if (TransactionOriginType.WEB.equals(m.getOriginType())) {
              fee = getPercentage().getTOPUP_WEB_FEE_AMOUNT();
            } else {
              // MAX(100; 0,5% * prepaid_topup_new_amount_value) + IVA
              fee = getCalculationsHelper().calculateFee(m.getImpfac(), getPercentage().getTOPUP_POS_FEE_PERCENTAGE());
            }
            break;
          case WITHDRAW:
            // Calcula las comisiones segun el tipo de carga (WEB o POS)
            if (TransactionOriginType.WEB.equals(m.getOriginType())) {
              fee = getPercentage().getWITHDRAW_WEB_FEE_AMOUNT();
            } else {
              // MAX ( 100; 0,5%*prepaid_topup_new_amount_value ) + IVA
              fee = getCalculationsHelper().calculateFee(m.getImpfac(), getPercentage().getWITHDRAW_POS_FEE_PERCENTAGE());
            }
            break;
        }
        accounting.setFee(fee);

        // Se calcula el Iva correspondiente a la comision
        BigDecimal iva = getCalculationsHelper().calculateIvaFromTotal(fee);
        accounting.setFeeIva(iva);
        accounting.setTransactionDate(m.getFechaCreacion());

        accounting.setStatus(AccountingStatusType.OK);
        //TODO: Llenar con los valores que corresponda.
        accounting.setFileId(0L);
        accounting.setAmountBalance(new NewAmountAndCurrency10(new BigDecimal(0)));
        accounting.setAmountMastercard(new NewAmountAndCurrency10(new BigDecimal(0)));
        accounting.setCollectorFee(new BigDecimal(0));
        accounting.setCollectorFeeIva(new BigDecimal(0));
        accounting.setConciliationDate(new Timestamp(System.currentTimeMillis()));

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

  /**
   * Busca los movimientos en accounting y genera un archivo csv que se envia por correo
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  @Override
  public void generateAccountingFile(Map<String, Object> headers, LocalDateTime date) throws Exception {
    if(date == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    List<AccountingData10> movements = this.searchAccountingData(null, date);
    String fileName = "src/main/resources/accounting_file.csv";
    createAccountingCSV(fileName, movements); // Crear archivo csv temporal
    sendFile(fileName, getConfigUtils().getProperty("accounting.email.dailyreport")); // envia archivo al email de reportes
    new File(fileName).delete(); // borra el archivo creado
  }

  public void createAccountingCSV(String filename, List<AccountingData10> lstAccountingMovement10s) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    String[] header = new String[]{"ID", "FECHA", "TIPO", "MONTO_IPM", "MONTO_USD", "DIF_TIPO_CAMBIO", "COMISION", "IVA"};
    writer.writeNext(header);

    for (AccountingData10 mov : lstAccountingMovement10s) {
      String[] data = new String[]{ mov.getId().toString(),
                                    mov.getTransactionDateInFormat(),
                                    mov.getType().getValue(),
                                    mov.getAmount().getValue().toString(),
                                    mov.getAmountUsd().getValue().toString(),
                                    mov.getExchangeRateDif().toString(),
                                    mov.getFee().toString(),
                                    mov.getFeeIva().toString() };
      writer.writeNext(data);
    }
    writer.close();
  }

  public void sendFile(String fileName, String emailAddress) throws Exception {
    FileInputStream attachmentFile = new FileInputStream(fileName);
    String fileToSend = Base64Utils.encodeToString(IOUtils.toByteArray(attachmentFile));

    // Enviamos el archivo al mail de reportes diarios
    EmailBody emailBodyToSend = new EmailBody();
    String fileNameToSend = String.format("reporte_contable_%s.csv", LocalDateTime.now().atZone(ZoneId.of("America/Santiago")).toLocalDate().toString());
    emailBodyToSend.addAttached(fileToSend, MimeType.CSV.getValue(), fileNameToSend);
    emailBodyToSend.setTemplateData(null);
    emailBodyToSend.setTemplate(MailTemplates.TEMPLATE_MAIL_ACCOUNTING_FILE_OK);
    emailBodyToSend.setAddress(emailAddress);
    mailPrepaidEJBBean10.sendMailAsync(null, emailBodyToSend);
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
    for (IpmMessage trx: ipmFile.getTransactions()) {

      AccountingData10 acc = new AccountingData10();
      acc.setOrigin(AccountingOriginType.IPM);
      acc.setType(this.getTransactionType(trx));
      acc.setAccountingMovementType(this.getMovementType(trx));
      acc.setIdTransaction(Long.valueOf(trx.getApprovalCode()));
      acc.setTransactionDate(Timestamp.from(trx.getTransactionLocalDate().toInstant()));

      // Monto en pesos
      acc.setAmount(new NewAmountAndCurrency10(
        IpmMessage.movePeriod(
          NumberUtils.getInstance().toLong(trx.getCardholderBillingAmount()),
          ipmFile.getCurrencyExponents().get(
            trx.getCardholderBillingCurrencyCode()
          )
        )
      ));

      //Monto en usd
      acc.setAmountUsd(new NewAmountAndCurrency10(
        IpmMessage.movePeriod(
          NumberUtils.getInstance().toLong(trx.getReconciliationAmount()),
          ipmFile.getCurrencyExponents().get(
            trx.getReconciliationCurrencyCode()
          )
        )
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
          exchangeRateDiff = this.getCalculationsHelper().calculatePercentageValue(acc.getAmount().getValue(), this.getCalculationsHelper().getCalculatorParameter10().getOTHER_CURRENCY_PURCHASE_EXCHANGE_RATE_PERCENTAGE());
          break;
      }

      acc.setFee(fee);
      acc.setFeeIva(iva);
      acc.setExchangeRateDif(exchangeRateDiff);
      //TODO: Llenar con los valores que corresponda.
      acc.setFileId(0L);
      acc.setAmountBalance(new NewAmountAndCurrency10(new BigDecimal(0)));
      acc.setAmountMastercard(new NewAmountAndCurrency10(new BigDecimal(0)));
      acc.setCollectorFee(new BigDecimal(0));
      acc.setCollectorFeeIva(new BigDecimal(0));
      acc.setConciliationDate(new Timestamp(System.currentTimeMillis()));
      acc.setStatus(AccountingStatusType.OK);

      transactions.add(acc);
    }

    this.saveAccountingData(null, transactions);
    // Se guarda la data en Clearing.
    this.saveClearingData(transactions);

    ipmFile.setStatus(IpmFileStatus.PROCESSED);
    this.updateIpmFileRecord(null, ipmFile);
  }

  private void saveClearingData(List<AccountingData10> accounting10s){
    for(AccountingData10 data: accounting10s){
      try {
        ClearingData10 clearing10 = new ClearingData10();
        clearing10.setAccountingId(data.getId());
        clearing10.setUserAccountId(0L);
        clearing10.setStatus(AccountingStatusType.OK);
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

}
