package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.AccountingOriginType;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.MailPrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10;
import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Base64Utils;

import javax.ejb.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
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

  public List<Accounting10> searchAccountingData(Map<String, Object> header, LocalDateTime dateToSearch) throws Exception {

    if(dateToSearch == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "dateToSearch"));
    }

    Object[] params = {
      dateToSearch
    };

    RowMapper rm = (Map<String, Object> row) -> {
      Accounting10 account = new Accounting10();

      account.setIdTransaction(numberUtils.toLong(row.get("id_tx")));

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setValue(numberUtils.toBigDecimal(row.get("amount")));
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);

      account.setAmount(amount);

      NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
      amount.setValue(numberUtils.toBigDecimal(row.get("ammount_usd")));
      amount.setCurrencyCode(CodigoMoneda.USA_USN);

      account.setAmountUsd(amountUsd);

      account.setExchangeRateDif(numberUtils.toBigDecimal(row.get("exchange_rate_dif")));
      account.setFee(numberUtils.toBigDecimal(row.get("fee")));
      account.setFeeIva(numberUtils.toBigDecimal(row.get("fee_iva")));

      account.setType(AccountingTxType.fromValue(String.valueOf(row.get("type"))));
      account.setOrigin(AccountingOriginType.fromValue(String.valueOf(row.get("origin"))));
      account.setTransactionDate((Timestamp) row.get("transaction_date"));

      return account;
    };
    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".XXX", rm, params);
    log.info("Respuesta Busca Movimiento: "+resp);
    return (List)resp.get("result");

  }

  @Override
  public List<Accounting10> searchAccountingData(Map<String, Object> header, Date dateToSearch) throws Exception {
    return null;
  }

  public List<Accounting10> saveAccountingData (Map<String, Object> header, List<Accounting10> accounting10s) throws Exception {
    if(accounting10s == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accounting10s"));
    }
    List<Accounting10> accounting10sFinal = new ArrayList<>();
    for(Accounting10 account : accounting10s) {

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
      Object[] params = {
        new InParam(account.getIdTransaction(), Types.BIGINT),
        new InParam(account.getType(), Types.VARCHAR),
        new InParam(account.getOrigin(), Types.VARCHAR),
        account.getAmount() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getAmount().getValue(), Types.NUMERIC),
        account.getAmount()== null ?  new NullParam(Types.NUMERIC) : new InParam(account.getAmount().getCurrencyCode().getValue(), Types.NUMERIC),
        account.getAmountUsd() == null ? new NullParam(Types.NUMERIC) : new InParam( account.getAmountUsd().getValue(), Types.NUMERIC),
        account.getExchangeRateDif() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getExchangeRateDif(), Types.NUMERIC),
        account.getFee() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getFee(), Types.NUMERIC),
        account.getFeeIva() == null ? new NullParam(Types.NUMERIC) : new InParam(account.getFeeIva(),Types.NUMERIC),
        new InParam(account.getTransactionDateInFormat(),Types.VARCHAR),
        new OutParam("_id", Types.BIGINT),
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_prp_insert_accounting_data_v10",params);

      if (!"0".equals(resp.get("_error_code"))) {
        log.error("mc_prp_insert_accounting_data_v10 resp: " + resp);
        throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
      }
      account.setId(numberUtils.toLong(resp.get("_id")));
      log.info("Accounting Insertado Id: "+numberUtils.toLong(resp.get("_id")));
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
      ReconciliationStatusType.RECONCILED.getValue()
    };

    RowMapper rm = (Map<String, Object> row) -> {
      try{
        PrepaidMovement10 p = new PrepaidMovement10();
        p.setId(numberUtils.toLong(row.get("_id")));
        p.setIdMovimientoRef(numberUtils.toLong(row.get("_id_movimiento_ref")));
        p.setIdPrepaidUser(numberUtils.toLong(row.get("_id_usuario")));
        p.setIdTxExterno(String.valueOf(row.get("_id_tx_externo")));
        p.setTipoMovimiento(PrepaidMovementType.valueOfEnum(String.valueOf(row.get("_tipo_movimiento"))));
        p.setMonto(numberUtils.toBigDecimal(row.get("_monto")));
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
        p.setClamon(CodigoMoneda.fromValue(numberUtils.toInteger(row.get("_clamon"))));
        p.setIndnorcor(IndicadorNormalCorrector.fromValue(numberUtils.toInteger(row.get("_indnorcor"))));
        p.setTipofac(TipoFactura.valueOfEnumByCodeAndCorrector(numberUtils.toInteger(row.get("_tipofac")), p.getIndnorcor().getValue()));
        p.setFecfac((java.sql.Date)row.get("_fecfac"));
        p.setNumreffac(String.valueOf(row.get("_numreffac")));
        p.setPan(String.valueOf(row.get("_pan")));
        p.setClamondiv(numberUtils.toInteger(row.get("_clamondiv")));
        p.setImpdiv(numberUtils.toLong(row.get("_impdiv")));
        p.setImpfac(numberUtils.toBigDecimal(row.get("_impfac")));
        p.setCmbapli(numberUtils.toInteger(row.get("_cmbapli")));
        p.setNumaut(String.valueOf(row.get("_numaut")));
        p.setIndproaje(IndicadorPropiaAjena.fromValue(String.valueOf(row.get("_indproaje"))));
        p.setCodcom(String.valueOf(row.get("_codcom")));
        p.setCodact(numberUtils.toInteger(row.get("_codact")));
        p.setImpliq(numberUtils.toLong(row.get("_impliq")));
        p.setClamonliq(numberUtils.toInteger(row.get("_clamonliq")));
        p.setCodpais(CodigoPais.fromValue(numberUtils.toInteger(row.get("_codpais"))));
        p.setNompob(String.valueOf(row.get("_nompob")));
        p.setNumextcta(numberUtils.toInteger(row.get("_numextcta")));
        p.setNummovext(numberUtils.toInteger(row.get("_nummovext")));
        p.setClamone(numberUtils.toInteger(row.get("_clamone")));
        p.setTipolin(String.valueOf(row.get("_tipolin")));
        p.setLinref(numberUtils.toInteger(row.get("_linref")));
        p.setNumbencta(numberUtils.toInteger(row.get("_numbencta")));
        p.setNumplastico(numberUtils.toLong(row.get("_numplastico")));
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
  public List<Accounting10> processMovementForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception {
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

      List<Accounting10> accountingMovements = new ArrayList<>();

      for (PrepaidMovement10 m : movements) {

        AccountingTxType type = AccountingTxType.RETIRO_WEB;;

        if(TipoFactura.CARGA_TRANSFERENCIA.equals(m.getTipofac())) {
          type = AccountingTxType.CARGA_WEB;
        } else if(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(m.getTipofac())) {
          type = AccountingTxType.CARGA_POS;
        } else if(TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(m.getTipofac())) {
          type = AccountingTxType.RETIRO_POS;
        }


        Accounting10 accounting = new Accounting10();
        accounting.setIdTransaction(m.getId());
        accounting.setOrigin(AccountingOriginType.MOVEMENT);
        accounting.setType(type);
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
        BigDecimal iva = getCalculationsHelper().calculateIva(fee);
        accounting.setFeeIva(iva);
        accounting.setTransactionDate(m.getFechaCreacion());
        accountingMovements.add(accounting);
      }

      if(!accountingMovements.isEmpty()) {
        accountingMovements = this.saveAccountingData(headers, accountingMovements);
        log.info("Movimientos insertados en la tabla de contabilidad: " + accountingMovements.size());
        return accountingMovements;
      } else {
        log.info("No hay movimientos a insertar en la tabla de contabilidad");
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

    List<Accounting10> movements = this.searchAccountingData(null, date);
    String fileName = "src/main/resources/accounting_file.csv";
    createAccountingCSV(fileName, movements); // Crear archivo csv temporal
    sendFile(fileName, getConfigUtils().getProperty("accounting.email.dailyreport")); // envia archivo al email de reportes
    new File(fileName).delete(); // borra el archivo creado
  }

  public void createAccountingCSV(String filename, List<Accounting10> lstAccountingMovement10s) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    String[] header = new String[]{"ID", "FECHA", "TIPO", "MONTO_IPM", "MONTO_USD", "DIF_TIPO_CAMBIO", "COMISION", "IVA"};
    writer.writeNext(header);

    for (Accounting10 mov : lstAccountingMovement10s) {
      System.out.println("Mov: " + mov);
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
}
