package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.mastercard.model.AccountingFile;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.ReconciliationMcRed10;
import cl.multicaja.prepaid.model.v10.ReconciliationOriginType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidClearingEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidClearingEJB10 {

  private static Log log = LogFactory.getLog(PrepaidClearingEJBBean10.class);

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private PrepaidAccountingFileEJBBean10 prepaidAccountingFileEJBBean10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

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

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  @Override
  public ClearingData10 insertClearingData(Map<String, Object> header, ClearingData10 clearing10) throws Exception {

    if(clearing10 == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "clearing10"));
    }
    //Error Id Accounting Null
    if(clearing10.getAccountingId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingId()"));
    }

    log.info(clearing10.toString());

    Object[] params = {
      clearing10.getAccountingId() == null ? new NullParam(Types.BIGINT):new InParam(clearing10.getAccountingId(), Types.BIGINT),
      clearing10.getUserBankAccount().getId() == null ? new NullParam(Types.BIGINT) : new InParam(clearing10.getUserBankAccount().getId(), Types.BIGINT),
      clearing10.getFileId() == null ? new NullParam(Types.BIGINT) : new InParam(clearing10.getFileId(), Types.BIGINT),
      clearing10.getStatus() == null ? new NullParam(Types.VARCHAR) : new InParam(clearing10.getStatus().getValue(), Types.VARCHAR),
      new OutParam("_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_acc_create_clearing_data_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_create_clearing_data_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }

    return searchClearingDataById(header,getNumberUtils().toLong(resp.get("_id")));
  }

  @Override
  public ClearingData10 updateClearingData(Map<String, Object> header, Long id, Long fileId, AccountingStatusType status) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    if(fileId == null && status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "allNull"));
    }

    Object[] params = {
      new InParam(id, Types.BIGINT),
      fileId == null ? new NullParam(Types.BIGINT) : new InParam(fileId, Types.BIGINT),
      status == null ? new NullParam(Types.VARCHAR) : new InParam(status, Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_update_clearing_data_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_create_accounting_file_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    return searchClearingDataById(header,getNumberUtils().toLong(id));
  }


  private List<ClearingData10> updateClearingData (Map<String, Object> header, List<ClearingData10> data, Long fileId) throws Exception {
    if(data == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "data"));
    }

    for (ClearingData10 m : data) {
      m.setStatus(AccountingStatusType.SENT);
      m.setFileId(fileId);
      m = this.updateClearingData(header, m.getId(), m.getFileId(), m.getStatus());
    }
    return data;
  }

  //TODO: este metodo no tiene test usando el parametro "status"
  @Override
  public List<ClearingData10> searchClearingData(Map<String, Object> header, Long id, AccountingStatusType status) throws Exception {

    if(id == null && status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value","allNull"));
    }
    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      status != null ? status.getValue() : new NullParam(Types.VARCHAR),
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      ClearingData10 clearing10 = new ClearingData10();

      clearing10.setId(getNumberUtils().toLong(row.get("_id")));
      clearing10.setAccountingId(getNumberUtils().toLong(row.get("_accounting_id"))); //IdAccounting
      clearing10.setUserAccountId(getNumberUtils().toLong(row.get("_user_account_id")));
      clearing10.setStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_status"))));
      clearing10.setFileId(getNumberUtils().toLong(row.get("_file_id")));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_created"));
      timestamps.setUpdatedAt((Timestamp)row.get("_updated"));
      clearing10.setTimestamps(timestamps);
      return clearing10;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_search_clearing_data_v10",  rm, params);
    List<ClearingData10> res = (List<ClearingData10>)resp.get("result");
    log.info(res);
    return res != null ? res : Collections.EMPTY_LIST;
  }

  @Override
  public ClearingData10 searchClearingDataById(Map<String, Object> header, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    List<ClearingData10> clearing10s = searchClearingData(null,id,null);
    return clearing10s != null && !clearing10s.isEmpty() ? clearing10s.get(0) : null;
  }


  public List<ClearingData10> searchClearignDataByFileId(Map<String, Object> headers, String fileId) throws Exception{
    if(fileId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileId"));
    }
    return searchFullClearingData(headers,null,fileId,null);
  }

  private List<ClearingData10> searchFullClearingData(Map<String, Object> headers, LocalDateTime to,String fileId, AccountingStatusType status) throws Exception{

    String format = "yyyy-MM-dd HH:mm:ss";
    String t = null;
    if(to != null){
      t = to.format(DateTimeFormatter.ofPattern(format));
    }

    Object[] params = {
      t == null ? new NullParam(Types.VARCHAR) : t,
      status == null ? new NullParam(Types.VARCHAR) : status.getValue(),
      fileId == null ? new NullParam(Types.VARCHAR) : fileId
    };
    log.info(params);
    RowMapper rm = (Map<String, Object> row) -> {
      ClearingData10 data = new ClearingData10();

      data.setId(getNumberUtils().toLong(row.get("_id")));
      data.setIdTransaction(getNumberUtils().toLong(row.get("_id_tx")));
      data.setType(AccountingTxType.fromValue(String.valueOf(row.get("_type"))));
      data.setAccountingMovementType(AccountingMovementType.fromValue(String.valueOf(row.get("_accounting_mov"))));
      data.setOrigin(AccountingOriginType.fromValue(String.valueOf(row.get("_origin"))));

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
      data.setFileId(getNumberUtils().toLong(row.get("file_id")));
      data.setTransactionDate((Timestamp) row.get("_transaction_date"));
      data.setConciliationDate((Timestamp) row.get("_conciliation_date"));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_created"));
      timestamps.setUpdatedAt((Timestamp)row.get("_updated"));
      data.setTimestamps(timestamps);

      //data.setAccountingId(getNumberUtils().toLong(row.get("_accounting_id"))); //IdAccounting
      data.setUserAccountId(getNumberUtils().toLong(row.get("_user_account_id")));

      return data;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_search_clearing_data_for_file_v10", rm, params);
    log.info("Respuesta Busca Movimiento: "+ resp);

    List<ClearingData10> result = (List<ClearingData10>)resp.get("result");
    return result != null ? result : Collections.EMPTY_LIST;
  }

  public List<ClearingData10> searchClearingDataToFile(Map<String, Object> headers, LocalDateTime to) throws Exception {
    if(to == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "to"));
    }
    return searchFullClearingData(headers,to,null,AccountingStatusType.PENDING);
  }

  /**
   * Busca los movimientos en accounting y genera un archivo csv que se envia por correo
   * @param headers
   * @param date
   * @return
   * @throws Exception
   */
  public AccountingFiles10 generateClearingFile(Map<String, Object> headers, ZonedDateTime date) throws Exception {
    if(date == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    ZonedDateTime endDay = date.withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime toUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime to = toUtc.toLocalDateTime();

    List<ClearingData10> movements = this.searchClearingDataToFile(null, to);

    for (ClearingData10 mov : movements) {
      //Busca la cuenta bancaria del movimiento
      if(mov.getUserBankAccount().getId() > 0) {
        //Obtener el Id del usuario
        Long prepaidUserId = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getIdTransaction()).getIdPrepaidUser();
        Long userIdMc = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUserId).getUserIdMc();
        UserAccount userAccount = getUserClient().getUserBankAccountById(null, userIdMc, mov.getUserBankAccount().getId());
        if(userAccount == null) {
          throw new ValidationException(CUENTA_NO_ASOCIADA_A_USUARIO);
        }
        mov.getUserBankAccount().setAccountNumber(userAccount.getAccountNumber());
        mov.getUserBankAccount().setAccountType(userAccount.getAccountType());
        mov.getUserBankAccount().setBankName(userAccount.getBankName());
        Rut r = new Rut();
        r.setValue(userAccount.getRut().getValue());
        r.setDv(userAccount.getRut().getDv());
        mov.getUserBankAccount().setRut(r);
      }
    }

    if(movements.isEmpty()){
      return null;
    }

    String directoryName = "clearing_files";
    File directory = new File(directoryName);
    if (! directory.exists()){
      directory.mkdir();
    }

    String fileId = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String fileName = String.format("TRX_PREPAGO_%s.CSV", fileId);

    createAccountingCSV(directoryName + "/" + fileName, fileId, movements); // Crear archivo csv temporal

    AccountingFiles10 file = new AccountingFiles10();
    file.setStatus(AccountingStatusType.PENDING);
    file.setName(fileName);
    file.setFileId(fileId);
    file.setFileFormatType(AccountingFileFormatType.CSV);
    file.setFileType(AccountingFileType.CLEARING);
    file.setUrl("");

    file = getPrepaidAccountingFileEJBBean10().insertAccountingFile(headers, file);


    movements = this.updateClearingData(headers, movements, file.getId());

    return file;
  }

  private final String TIME_ZONE = "America/Santiago";
  private final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  private String getTimestampAtTimezone(Timestamp ts, String timeZone, String pattern) {
    if(ts == null) {
      ts = Timestamp.from(Instant.now());
    }
    if(StringUtils.isAllBlank(timeZone)) {
      timeZone = TIME_ZONE;
    }
    if(StringUtils.isAllBlank(pattern)) {
      pattern = DATE_PATTERN;
    }
    LocalDateTime localDateTime = ts.toLocalDateTime();
    ZonedDateTime utc = localDateTime.atZone(ZoneOffset.UTC);
    ZonedDateTime atTimezone = utc.withZoneSameInstant(ZoneId.of(timeZone));
    return atTimezone.format(DateTimeFormatter.ofPattern(pattern));
  }


  private void createAccountingCSV(String filename, String fileId, List<ClearingData10> lstClearingMovement10s) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    String[] header = new String[]{"ID_PREPAGO","ID_LIQUIDACION", "ID_TRX", "ID_CUENTA_ORIGEN", "TIPO_TRX", "MOV_CONTABLE",
      "FECHA_TRX", "FECHA_CONCILIACION", "MONTO_TRX_PESOS", "MONTO_TRX_MCARD_PESOS", "MONTO_TRX_USD", "VALOR_USD",
      "DIF_TIPO_CAMBIO", "COMISION_PREPAGO_PESOS", "IVA_COMISION_PREPAGO_PESOS", "COMISION_RECAUDADOR_MC_PESOS",
      "IVA_COMISION_RECAUDADOR_MC_PESOS", "MONTO_AFECTO_A_SALDO_PESOS", "ID_CUENTA_DESTINO", "RUT", "BANCO",
      "NRO_CUENTA", "TIPO_CUENTA", "ESTADO_LIQUIDACION"};
    writer.writeNext(header);

    for (ClearingData10 mov : lstClearingMovement10s) {

      Long accountId = mov.getUserBankAccount().getId();

      String transactionDate = getTimestampAtTimezone(mov.getTransactionDate(), null, null);
      String reconciliationDate = getTimestampAtTimezone(mov.getConciliationDate(), null, null);

      String[] data = new String[]{
        mov.getId().toString(), //ID,
        fileId, //ID_LIQUIDACION,
        mov.getIdTransaction().toString(), //ID_TRX
        "0", //ID_CUENTA_ORIGEN TODO: este código es dado por Multicaja red.
        mov.getType().getValue(), //TIPO_TRX
        mov.getAccountingMovementType().getValue(), //MOV_CONTABLE
        transactionDate, //FECHA_TRX
        reconciliationDate, //FECHA_CONCILIACION
        mov.getAmountBalance().getValue().toString(), //MONTO_TRX_PESOS
        mov.getAmountMastercard().getValue().toString(), //MONTO_TRX_MCARD_PESOS
        mov.getAmountUsd().getValue().toString(), //MONTO_TRX_USD
        "", //VALOR_USD TODO: de donde sacar este valor?
        mov.getExchangeRateDif().toString(), //DIF_TIPO_CAMBIO
        mov.getFee().toString(), //COMISION_PREPAGO_PESOS
        mov.getFeeIva().toString(), //IVA_COMISION_PREPAGO_PESOS
        mov.getCollectorFee().toString(), //COMISION_RECAUDADOR_MC_PESOS
        mov.getCollectorFeeIva().toString(), //IVA_COMISION_RECAUDADOR_MC_PESOS
        mov.getAmount().getValue().toString(), //MONTO_AFECTO_A_SALDO_PESOS
        "", //ID_CUENTA_DESTINO - Este campo es utilizado solo por MulticajaRed. No lo utiliza ni setea Prepago
        accountId > 0 ? String.format("%s-%s", mov.getUserBankAccount().getRut().getValue(), mov.getUserBankAccount().getRut().getDv()) : "", //RUT
        accountId > 0 ? mov.getUserBankAccount().getBankName() : "", //BANCO
        accountId > 0 ? getNumberUtils().toLong(mov.getUserBankAccount().getAccountNumber()).toString() : "", //NRO_CUENTA
        accountId > 0 ? mov.getUserBankAccount().getAccountType() : "", //TIPO_CUENTA
        mov.getStatus().getValue() //ESTADO_LIQUIDACION
      };
      writer.writeNext(data);
    }
    writer.close();
  }
  public void processClearingResponse(InputStream inputStream, String fileName) throws Exception {
    log.info("processClearingResponse IN");
    String fileId = fileName.replace("TRX_PREPAGO_","").replace(".CSV","");
    List<ClearingData10> clearingData10s = processClearingResponseDataFile(inputStream);
    log.info(String.format("Registro procesados: %d",clearingData10s.size()));
    processClearingBankResponse(clearingData10s,fileName,fileId);
    log.info("processClearingResponse OUT");
  }

  private List<ClearingData10> processClearingResponseDataFile(InputStream inputStream) throws IOException, ValidationException {

    List<ClearingData10> clearingData10s;
      log.info("IN");
      try {
        Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        CSVReader csvReader = new CSVReader(reader,',');
        csvReader.readNext();
        String[] record;
        clearingData10s = new ArrayList<>();

        while ((record = csvReader.readNext()) != null) {
          log.debug(Arrays.toString(record));
          ClearingData10 clearingData = new ClearingData10();
          clearingData.setId(numberUtil.toLong(record[0]));
          clearingData.setType(AccountingTxType.fromValue(String.valueOf(record[4])));
          clearingData.setAmountBalance(new NewAmountAndCurrency10(numberUtil.toBigDecimal(record[8])));
          clearingData.setAmountMastercard(new NewAmountAndCurrency10(numberUtil.toBigDecimal(record[9])));
          clearingData.setAmount(new NewAmountAndCurrency10(numberUtil.toBigDecimal(record[17])));
          clearingData.setStatus(AccountingStatusType.fromValue(String.valueOf(record[23])));
          clearingData10s.add(clearingData);
        }
        inputStream.close();
      }catch (Exception e){
        inputStream.close();
        log.error("Exception: "+e);
        e.printStackTrace();
        System.out.println("Exception: "+e);
        throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
      }
      log.info("OUT");
      return clearingData10s;

  }
  private void processClearingBankResponse(List<ClearingData10> clearingDataInFile,String fileName,String fileId) throws Exception {
    final List<ClearingData10>  clearingDataInTable = searchClearignDataByFileId(null,fileId);
    //Verifica lo que debe venir en el archivo.
    for (ClearingData10 data : clearingDataInTable) {
      if (AccountingTxType.RETIRO_WEB.equals(data.getType())) {

        // Busca todos los retiros web que tienen que venir en el archivo
        ClearingData10 result = clearingDataInFile.stream().filter(x ->data.getId().equals(x.getId())).findAny().orElse(null);
        //Existe
        if(result != null) {
          //Coinciden
          if(data.getAmount().equals(result.getAmount()) &&
            data.getAmountBalance().equals(result.getAmount()) &&
            data.getAmountMastercard().equals(result.getAmount())
          ){
            // Si existe en el archivo y concuerda se actualiza al estado que dice el banco.
            ClearingData10 dataUpdated = updateClearingData(null,data.getId(),null,result.getStatus());
          }
          else{//Si  viene en el archivo, pero los montos no concuerdan, investigar.
            ClearingData10 dataUpdated = updateClearingData(null,data.getId(),null,AccountingStatusType.RESEARCH);
            this.createClearingResearch(fileName,data.getId());
          }
        }
        else{ // No viene en el archivo
          ClearingData10 dataUpdated = updateClearingData(null,data.getId(),null,AccountingStatusType.RESEARCH);
          this.createClearingResearch(fileName,data.getId());
        }
      }
    }
    //Verifica que no venga algo extra en el archivo.
    for (ClearingData10 data : clearingDataInFile) {
      if (AccountingTxType.RETIRO_WEB.equals(data.getType())) {
        // Busca todos los retiros web que tienen que venir en el archivo
        ClearingData10 result = clearingDataInTable.stream().filter(x ->data.getId().equals(x.getId())).findAny().orElse(null);
        //Viene en el archivo y no existe en nuestra tabla
        if(result == null) {
          //Agregar a Investigar
          this.createClearingResearch(fileName,data.getId());
        }
      }
    }
  }
  // Agrega movimiento a investigar
  private void createClearingResearch(String fileName,Long clearingId) throws Exception {
    String idToResearch = String.format("ClearingId=%d",clearingId);
    getPrepaidMovementEJBBean10().createMovementResearch(null,idToResearch, ReconciliationOriginType.CLEARING,fileName);
  }
}
