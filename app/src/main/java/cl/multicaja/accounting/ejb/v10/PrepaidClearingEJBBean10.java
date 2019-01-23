package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.ReconciliationMcRed10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.io.*;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidClearingEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidClearingEJB10 {

  private static Log log = LogFactory.getLog(PrepaidClearingEJBBean10.class);

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
      id == null ? new NullParam(Types.BIGINT) : new InParam(id, Types.BIGINT),
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

  @Override
  public List<ClearingData10> searchClearingData(Map<String, Object> header, Long id, AccountingStatusType status) throws Exception {

    if(id == null && status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value","allNull"));
    }
    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      status != null ? status : new NullParam(Types.VARCHAR),
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      ClearingData10 clearing10 = new ClearingData10();

      clearing10.setId(getNumberUtils().toLong(row.get("_id")));
      clearing10.setAccountingId(getNumberUtils().toLong(row.get("_accounting_id"))); //IdAccounting
      clearing10.setUserAccountId(getNumberUtils().toLong(row.get("_user_account_id")));
      clearing10.setStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_status"))));
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

  public List<ClearingData10> searchClearingDataToFile(Map<String, Object> headers, LocalDateTime from, LocalDateTime to) throws Exception {
    if(from == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "from"));
    }
    if(to == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "to"));
    }

    String format = "yyyy-MM-dd HH:mm:ss";

    String f = from.format(DateTimeFormatter.ofPattern(format));
    String t = to.format(DateTimeFormatter.ofPattern(format));

    log.info(String.format("From: %s", f));
    log.info(String.format("To: %s", t));

    Object[] params = {
      f,
      t,
      AccountingStatusType.PENDING.getValue()
    };

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

  /**
   * Busca los movimientos en accounting y genera un archivo csv que se envia por correo
   * @param headers
   * @param date
   * @return
   * @throws Exception
   */
  public void generateClearingFile(Map<String, Object> headers, ZonedDateTime date) throws Exception {
    if(date == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "date"));
    }

    ZonedDateTime midnight = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    ZonedDateTime endDay = date.withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime fromUtc = ZonedDateTime.ofInstant(midnight.toInstant(), ZoneOffset.UTC);
    ZonedDateTime toUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime from = fromUtc.toLocalDateTime();
    LocalDateTime to = toUtc.toLocalDateTime();

    List<ClearingData10> movements = this.searchClearingDataToFile(null, from, to);

    String fileName = String.format("TRX_PREPAGO_%s.CSV", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    createAccountingCSV(fileName, movements); // Crear archivo csv temporal
    //sendFile(fileName, getConfigUtils().getProperty("accounting.email.dailyreport")); // envia archivo al email de reportes
    //new File(fileName).delete(); // borra el archivo creado
  }


  public void createAccountingCSV(String filename, List<ClearingData10> lstClearingMovement10s) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    String[] header = new String[]{"ID_LIQUIDACION", "ID_TRX", "ID_CUENTA_ORIGEN", "TIPO_TRX", "MOV_CONTABLE",
      "FECHA_TRX", "FECHA_CONCILIACION", "MONTO_TRX_PESOS", "MONTO_TRX_MCARD_PESOS", "MONTO_TRX_USD", "VALOR_USD",
      "DIF_TIPO_CAMBIO", "COMISION_PREPAGO_PESOS", "IVA_COMISION_PREPAGO_PESOS", "COMISION_RECAUDADOR_MC_PESOS",
      "IVA_COMISION_RECAUDADOR_MC_PESOS", "MONTO_AFECTO_A_SALDO_PESOS", "ID_CUENTA_DESTINO", "RUT", "BANCO",
      "NRO_CUENTA", "TIPO_CUENTA", "ESTADO_LIQUIDACION"};
    writer.writeNext(header);

    for (ClearingData10 mov : lstClearingMovement10s) {

      Long bankAccountId = mov.getUserBankAccount().getId();

      if(bankAccountId > 0) {
        //TODO: buscar la informacion de la cuenta bancaria en api-users
      }

      String[] data = new String[]{
        mov.getId().toString(), //ID_LIQUIDACION,
        mov.getIdTransaction().toString(), //ID_TRX
        "0", //ID_CUENTA_ORIGEN TODO: este código es dado por Multicaja red.
        mov.getType().getValue(), //TIPO_TRX
        "", //MOV_CONTABLE TODO: definir los tipos de movimientos contables.
        mov.getTransactionDateInFormat(), //FECHA_TRX
        mov.getConciliationDateInFormat(), //FECHA_CONCILIACION
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
        "", //RUT
        "", //BANCO
        "", //NRO_CUENTA
        "", //TIPO_CUENTA
        mov.getStatus().getValue() //ESTADO_LIQUIDACION
      };
      writer.writeNext(data);
    }
    writer.close();
  }
  public void processClearingResponse(InputStream inputStream, String fileName) throws Exception {
    log.info("processClearingResponse IN");
    List<ClearingData10> clearingData10s = processClearingResponseDataFile(inputStream,fileName);
    log.info(String.format("Registro procesados: %d",clearingData10s.size()));
    updateClearingBankResponse(clearingData10s);
    log.info("processClearingResponse OUT");
  }

  public List<ClearingData10> processClearingResponseDataFile(InputStream inputStream, String fileName) throws IOException, ValidationException {

    List<ClearingData10> clearingData10s;
      log.info("IN");
      try {
        Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        CSVReader csvReader = new CSVReader(reader,';');
        csvReader.readNext();
        String[] record;
        clearingData10s = new ArrayList<>();

        while ((record = csvReader.readNext()) != null) {
          log.debug(Arrays.toString(record));
          ClearingData10 clearingData = new ClearingData10();
          clearingData.setAccountingId(numberUtil.toLong(record[0]));
          clearingData.setStatus(AccountingStatusType.fromValue(String.valueOf(record[0])));
          clearingData.setUserAccountId(getNumberUtils().toLong(record[0]));
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
  public void updateClearingBankResponse( List<ClearingData10> clearingData10s) throws Exception {
    for (ClearingData10 data : clearingData10s){
      ClearingData10 dataUpdated = updateClearingData(null,data.getId(),null,data.getStatus());
      log.info("Updated ClearingData: "+dataUpdated);
    }
  }
}
