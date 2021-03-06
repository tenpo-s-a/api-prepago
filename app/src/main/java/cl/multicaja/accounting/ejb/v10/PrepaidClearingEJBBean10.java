package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.ZONEID;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.ejb.*;
import java.io.*;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

  private ResearchMovementInformationFiles researchMovementInformationFiles;

  private static final String INSERT_CLEARING = "INSERT INTO prepaid_accounting.clearing(\n" +
    "             accounting_id, user_account_id, file_id, status, created, \n" +
    "            updated, bank_id, account_number, account_type, account_rut)\n" +
    "    VALUES ( ?, ?, ?, ?, ?, \n" +
    "            ?, ?, ?, ?, ?);\n";

  private static final String FIND_BY_ID = String.format("SELECT * FROM %s.clearing id = ?",getSchemaAccounting());

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

  protected String toJson(Object obj) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(obj);
  }

  public ClearingData10 insertClearing(ClearingData10 clearing10)throws Exception {
    if(clearing10 == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "clearing10"));
    }
    //Error Id Accounting Null
    if(clearing10.getAccountingId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingId()"));
    }

    KeyHolder keyHolder = new GeneratedKeyHolder();
    getDbUtils().getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(INSERT_CLEARING, new String[] {"id"});
      ps.setLong(1, clearing10.getAccountingId());
      ps.setLong(2, 0);
      ps.setLong(3,clearing10.getFileId());
      ps.setString(4, clearing10.getStatus().getValue());
      ps.setTimestamp(5, Timestamp.from(Instant.now()));
      ps.setTimestamp(6, Timestamp.from(Instant.now()));
      if(clearing10.getUserBankAccount() == null ){
        ps.setLong(7,0L);
        ps.setString(8,"");
        ps.setString(9,"");
        ps.setLong(10,0L);
      } else {
        ps.setLong(7,clearing10.getUserBankAccount().getBankId());
        ps.setLong(8,clearing10.getUserBankAccount().getAccountNumber());
        ps.setString(9,clearing10.getUserBankAccount().getAccountType());
        ps.setString(10,clearing10.getUserBankAccount().getRut());
      }
      ps.setLong(5, 0L);
      ps.setString(6, AccountStatus.ACTIVE.toString());
      ps.setTimestamp(7, Timestamp.from(Instant.now()));
      ps.setTimestamp(8, Timestamp.from(Instant.now()));
      ps.setTimestamp(9, Timestamp.from(Instant.now()));
      ps.setTimestamp(10, Timestamp.from(Instant.now()));
      return ps;
    }, keyHolder);
    try{
      return  this.findById((long) keyHolder.getKey());
    }catch (Exception e){
      return null;
    }
  }

  public ClearingData10 findById(Long clearingId)throws Exception {
    if(clearingId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "clearingId"));
    }
    log.info(String.format("[findById] Buscando Clearing por -> clearingId [%d]", clearingId));
    try{
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_BY_ID, this.getClearingRowMapper(), clearingId);
    }catch (EmptyResultDataAccessException e){
      log.error(String.format("[findById] Buscando Clearing por -> clearingId [%d] no existe", clearingId));
      return null;
    }
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
      clearing10.getUserBankAccount() == null ? new NullParam(Types.BIGINT) : new InParam(clearing10.getUserBankAccount().getId(), Types.BIGINT),
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
  public ClearingData10 updateClearingData(Map<String, Object> header, Long id, AccountingStatusType status) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    return updateClearingData(header, id, null, status);
  }

  @Override
  public ClearingData10 updateClearingData(Map<String, Object> header, Long id, Long fileId, AccountingStatusType status) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "allNull"));
    }

    Object[] params = {
      new InParam(id, Types.BIGINT),
      fileId == null ? new NullParam(Types.BIGINT) : new InParam(fileId, Types.BIGINT),
      status == null ? new NullParam(Types.VARCHAR) : new InParam(status.getValue(), Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_update_clearing_data_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_update_clearing_data_v10 resp: " + resp);
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

  @Override
  public List<ClearingData10> searchClearingData(Map<String, Object> header, Long id, AccountingStatusType status, Long accountingId) throws Exception {

    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      status != null ? status.getValue() : new NullParam(Types.VARCHAR),
      accountingId != null ? accountingId : new NullParam(Types.BIGINT),
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = getClearingDataRowMapper();

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_search_clearing_data_v10",  rm, params);
    List<ClearingData10> res = (List<ClearingData10>)resp.get("result");
    log.info(res);
    return res != null ? res : Collections.emptyList();
  }

  @Override
  public ClearingData10 searchClearingDataById(Map<String, Object> header, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    List<ClearingData10> clearing10s = this.searchClearingData(null,id,null, null);
    return clearing10s != null && !clearing10s.isEmpty() ? clearing10s.get(0) : null;
  }

  @Override
  public ClearingData10 searchClearingDataByAccountingId(Map<String, Object> header, Long accountingId) throws Exception {
    if(accountingId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingId"));
    }
    List<ClearingData10> clearing10s = searchClearingData(null,null,null, accountingId);
    return clearing10s != null && !clearing10s.isEmpty() ? clearing10s.get(0) : null;
  }


  public List<ClearingData10> searchClearignDataByFileId(Map<String, Object> headers, String fileId) throws Exception{
    if(fileId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileId"));
    }
    return searchFullClearingData(headers,null, fileId,null);
  }

  private List<ClearingData10> searchFullClearingData(Map<String, Object> headers, LocalDateTime to, String fileId, AccountingStatusType status) throws Exception{

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
      amount.setCurrencyCode(CodigoMoneda.CLP);
      data.setAmount(amount);

      NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
      amountUsd.setValue(getNumberUtils().toBigDecimal(row.get("_amount_usd")));
      amountUsd.setCurrencyCode(CodigoMoneda.USD);
      data.setAmountUsd(amountUsd);

      NewAmountAndCurrency10 amountMc = new NewAmountAndCurrency10();
      amountMc.setValue(getNumberUtils().toBigDecimal(row.get("_amount_mcar")));
      amountMc.setCurrencyCode(CodigoMoneda.CLP);
      data.setAmountMastercard(amountMc);

      data.setExchangeRateDif(getNumberUtils().toBigDecimal(row.get("_exchange_rate_dif")));
      data.setFee(getNumberUtils().toBigDecimal(row.get("_fee")));
      data.setFeeIva(getNumberUtils().toBigDecimal(row.get("_fee_iva")));
      data.setCollectorFee(getNumberUtils().toBigDecimal(row.get("_collector_fee")));
      data.setCollectorFeeIva(getNumberUtils().toBigDecimal(row.get("_collector_fee_iva")));

      NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
      amountBalance.setValue(getNumberUtils().toBigDecimal(row.get("_amount_balance")));
      amountBalance.setCurrencyCode(CodigoMoneda.CLP);
      data.setAmountBalance(amountBalance);

      data.setStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_status"))));
      data.setFileId(getNumberUtils().toLong(row.get("file_id")));
      data.setTransactionDate((Timestamp) row.get("_transaction_date"));
      data.setConciliationDate((Timestamp) row.get("_conciliation_date"));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(((Timestamp)row.get("_created")).toLocalDateTime());
      timestamps.setUpdatedAt(((Timestamp)row.get("_updated")).toLocalDateTime());
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


  //FIXME: Corregir esto despues. ACTION: Esto es una preparación de ambiente para el test de resolución de respuesta del banco, debería ir en los tests
  /**
   * Busca los movimientos en clearing y genera un archivo csv que se envia por correo
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
        //TODO: Verificar como se cargaran los datos de las cuentas de transferencia
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

    createClearingCSV(directoryName + "/" + fileName, fileId, movements); // Crear archivo csv temporal

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

  //FIXME: Corregir esto despues. ACTION: Esto es una preparación de ambiente para el test de resolución de respuesta del banco, debería ir en los tests
  public File createClearingCSV(String filename, String fileId, List<ClearingData10> lstClearingMovement10s) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    // FIXME: Agregar tasa de intercambio ACTION: Revisar en el proyecto prepaid batch worker
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

        "", //ID_CUENTA_DESTINO - Este campo es utilizado solo por MulticajaRed. No lo utiliza ni setea Prepago
        mov.getUserBankAccount().getRut() != null ? mov.getUserBankAccount().getRut() : "", //RUT
        "BANCO", //BANCO
        mov.getUserBankAccount().getAccountNumber() != null ? String.valueOf(mov.getUserBankAccount().getAccountNumber()) : "", //NRO_CUENTA
        mov.getUserBankAccount().getAccountType() != null ? mov.getUserBankAccount().getAccountType(): "", //TIPO_CUENTA
        mov.getStatus().getValue() //ESTADO_LIQUIDACION
      };
      writer.writeNext(data);
    }
    writer.close();
    return file;
  }

  public void processClearingResponse(InputStream inputStream, String fileName) throws Exception {
    log.info("processClearingResponse IN");
    String fileId = fileName.replace("TRX_PREPAGO_","").replace(".CSV","");
    List<ClearingData10> clearingData10s = processClearingResponseDataFile(inputStream);
    log.info(String.format("Registro procesados: %d", clearingData10s.size()));
    processClearingBankResponse(clearingData10s, fileName, fileId);
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
          clearingData.setId(getNumberUtils().toLong(record[0]));
          clearingData.setIdTransaction(getNumberUtils().toLong(record[2]));
          clearingData.setType(AccountingTxType.fromValue(String.valueOf(record[4])));
          clearingData.setAmount(new NewAmountAndCurrency10(getNumberUtils().toBigDecimal(record[8])));
          clearingData.setAmountMastercard(new NewAmountAndCurrency10(getNumberUtils().toBigDecimal(record[9])));
          clearingData.setAmountBalance(new NewAmountAndCurrency10(getNumberUtils().toBigDecimal(record[17])));
          clearingData.setStatus(AccountingStatusType.fromValue(String.valueOf(record[23])));

          Timestamps timestamps = new Timestamps();
          LocalDateTime localDateTime = getDateUtils().dateStringToLocalDateTime(record[6],DATE_PATTERN);
          ZonedDateTime ldtZoned = localDateTime.atZone(ZoneId.of(ZONEID.AMERICA_SANTIAGO.getValue()));
          ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
          timestamps.setCreatedAt(utcZoned.toLocalDateTime());

          clearingData.setTimestamps(timestamps);

          String stringRut = String.valueOf(record[19]);

          UserAccount userAccount = new UserAccount();
          userAccount.setRut(stringRut);
          userAccount.setBankName(String.valueOf(record[20]));
          userAccount.setAccountNumber(Long.valueOf(String.valueOf(record[21])));
          userAccount.setAccountType(String.valueOf(record[22]));

          clearingData.setUserBankAccount(userAccount);
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

  // F1 de clearing
  private void processClearingBankResponse(List<ClearingData10> clearingDataInFile, String fileName, String fileId) throws Exception {
    //TODO (poca prioridad): Es posible que el archivo de respuesta tenga transacciones de mas de 1 dia
    final List<ClearingData10>  clearingDataInTable = searchClearignDataByFileId(null, fileId);

    //Verifica lo que debe venir en el archivo.
    for(ClearingData10 data : clearingDataInTable) {
      // Buscar si está conciliado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(data.getIdTransaction());
      // Buscar el movimiento en si
      PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(data.getIdTransaction());
      // Buscar su par en el archivo
      ClearingData10 result = clearingDataInFile.stream().filter(x ->data.getId().equals(x.getId())).findAny().orElse(null);

      if(AccountingTxType.RETIRO_WEB.equals(data.getType())) {
        if(AccountingStatusType.PENDING.equals(data.getStatus()) && reconciliedMovement10 == null) { // Aun no ha sido procesado?
          if(result != null) { //Existe tambien en el archivo?
            PrepaidUser10 prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidMovement10.getIdPrepaidUser());

            //TODO: Verificar datos de cuenta bancaria, ya que los datos ahora estan en la tabla de Clearing
            //Que coincidan los datos del archivo con los de clearing
            //Coinciden todos sus valores
            if(data.getAmount().getValue().compareTo(result.getAmount().getValue()) == 0 &&
              data.getAmountBalance().getValue().compareTo(result.getAmountBalance().getValue()) == 0 &&
              data.getAmountMastercard().getValue().compareTo(result.getAmountMastercard().getValue()) == 0) {
              // Si existe en el archivo y concuerda se actualiza al estado que dice el banco.
              updateClearingData(null, data.getId(),null, result.getStatus());
            } else { // Si  viene en el archivo, pero los montos no concuerdan, marcar.
              updateClearingData(null, data.getId(),null, AccountingStatusType.INVALID_INFORMATION);
            }
          } else { // No viene en el archivo, marcar
            updateClearingData(null, data.getId(),null, AccountingStatusType.NOT_IN_FILE);
          }
        } else {
          // Este movimiento ya fue procesado anteriormente, dado que:
          // O su estado clearing es distinto de PENDING
          // O ya esta conciliado

          List<ResearchMovementInformationFiles> researchMovementInformationFilesList1 = new ArrayList<>();
          ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
          researchMovementInformationFiles.setIdArchivo((Long.valueOf(fileId)));
          researchMovementInformationFiles.setIdEnArchivo(data.getResearchId());
          researchMovementInformationFiles.setNombreArchivo(fileName);
          researchMovementInformationFiles.setTipoArchivo(AccountingFileType.CLEARING.toString());
          researchMovementInformationFilesList1.add(researchMovementInformationFiles);
          createClearingResearch(
            researchMovementInformationFilesList1,
            ReconciliationOriginType.CLEARING,
            data.getTimestamps().getCreatedAt(),
            ResearchMovementResponsibleStatusType.OTI_PREPAID,
            ResearchMovementDescriptionType.MOVEMENT_WAS_PROCESSED,
            data.getIdTransaction(),
            PrepaidMovementType.PURCHASE,
            ResearchMovementSentStatusType.SENT_RESEARCH_PENDING);

          // Los movimientos con clearing resuelto y no conciliados deben conciliarse (para que no pasen a clearingResolution)
          if(!AccountingStatusType.PENDING.equals(data.getStatus()) && reconciliedMovement10 == null) {
            getPrepaidMovementEJBBean10().createMovementConciliate(null, data.getIdTransaction(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
          }
        }
      }
    }
    //Verifica que no venga algo extra en el archivo.
    for(ClearingData10 data : clearingDataInFile) {
      if(AccountingTxType.RETIRO_WEB.equals(data.getType())) {
        // Busca todos los retiros web que tienen que venir en el archivo
        ClearingData10 result = clearingDataInTable.stream().filter(x ->data.getId().equals(x.getId())).findAny().orElse(null);
        //Viene en el archivo y no existe en nuestra tabla
        if(result == null) {
          //Agregar a Investigar

          List<ResearchMovementInformationFiles> researchMovementInformationFilesList2 = new ArrayList<>();
          ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
          researchMovementInformationFiles.setIdArchivo((Long.valueOf(fileId)));
          researchMovementInformationFiles.setIdEnArchivo(data.getResearchId());
          researchMovementInformationFiles.setNombreArchivo(fileName);
          researchMovementInformationFiles.setTipoArchivo(AccountingFileType.CLEARING.toString());
          researchMovementInformationFilesList2.add(researchMovementInformationFiles);
          createClearingResearch(
            researchMovementInformationFilesList2,
            ReconciliationOriginType.CLEARING,
            data.getTimestamps().getCreatedAt(),
            ResearchMovementResponsibleStatusType.RECONCIALITION_MULTICAJA_OTI_PREPAGO,
            ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB,
            data.getIdTransaction(),
            PrepaidMovementType.PURCHASE,
            ResearchMovementSentStatusType.SENT_RESEARCH_PENDING);

        }
      } else {
        // Todo: Por ahora todo los movimientos que no sean RETIRO_WEB son aceptados
        // En un futuro este archivo vendran mas tipos de movimientos que deben ser chequeados tambien si existen
      }
    }
  }

  public void createClearingResearch(
    List<ResearchMovementInformationFiles> researchMovementInformationFilesList,
    ReconciliationOriginType reconciliationOriginType,
    LocalDateTime dateOfTransaction,
    ResearchMovementResponsibleStatusType researchMovementResponsibleStatusType,
    ResearchMovementDescriptionType researchMovementDescriptionType,
    Long movRef,
    PrepaidMovementType prepaidMovementType,
    ResearchMovementSentStatusType researchMovementSentStatusType
  ) throws Exception{


    String jsonSent = this.toJson(researchMovementInformationFilesList);
    getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      jsonSent,
      reconciliationOriginType.name(),
      dateOfTransaction,
      researchMovementResponsibleStatusType.getValue(),
      researchMovementDescriptionType.getValue(),
      movRef,
      prepaidMovementType.name(),
      researchMovementSentStatusType.getValue());
  }


  @Override
  public List<ClearingData10> getWebWithdrawForReconciliation(Map<String, Object> headers) throws Exception {

    RowMapper rm = getClearingDataRowMapper();
    Map<String, Object> resp = getDbUtils().execute(String.format("%s.mc_acc_busca_retiros_web_conciliar_v10", getSchemaAccounting()), rm);

    List list = (List)resp.get("result");

    return list != null ? list : Collections.EMPTY_LIST;
  }

  private org.springframework.jdbc.core.RowMapper<ClearingData10> getClearingRowMapper() {
    return (ResultSet rs, int rowNum) -> {
      ClearingData10 c = new ClearingData10();
      c.setId(rs.getLong("id"));
      c.setAccountingId(rs.getLong("accounting_id"));
      c.setFileId(rs.getLong("file_id"));
      c.setStatus(AccountingStatusType.fromValue(rs.getString("status")));

      UserAccount ua = new UserAccount();
      ua.setBankId(rs.getLong("bank_id"));
      ua.setAccountNumber(rs.getLong("account_number"));
      ua.setAccountType(rs.getString("account_type"));
      ua.setRut(rs.getString("account_rut"));
      c.setUserBankAccount(ua);

      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(rs.getTimestamp("created").toLocalDateTime());
      timestamps.setUpdatedAt(rs.getTimestamp("_updated").toLocalDateTime());
      c.setTimestamps(timestamps);

       return c;
    };
  }

  private RowMapper getClearingDataRowMapper() {
    return (Map<String, Object> row) -> {
      ClearingData10 clearing10 = new ClearingData10();
      clearing10.setId(getNumberUtils().toLong(row.get("_id")));
      clearing10.setIdTransaction(getNumberUtils().toLong(row.get("_id_tx")));
      clearing10.setAccountingId(getNumberUtils().toLong(row.get("_accounting_id"))); //IdAccounting
      clearing10.setUserAccountId(getNumberUtils().toLong(row.get("_user_account_id")));
      clearing10.setStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_status"))));
      clearing10.setFileId(getNumberUtils().toLong(row.get("_file_id")));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(((Timestamp)row.get("_created")).toLocalDateTime());
      timestamps.setUpdatedAt(((Timestamp)row.get("_updated")).toLocalDateTime());
      clearing10.setTimestamps(timestamps);
      return clearing10;
    };
  }

  public ClearingData10 buildClearing(Long accountingId,UserAccount userAccount){
    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setAccountingId(accountingId);
    clearingData10.setUserBankAccount(userAccount);
    clearingData10.setStatus(AccountingStatusType.INITIAL);
    return clearingData10;
  }
}
