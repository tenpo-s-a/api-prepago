package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.AccountingOriginType;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
public class PrepaidAccountingEJBBean10 extends PrepaidBaseEJBBean10 {

  private static Log log = LogFactory.getLog(PrepaidMovementEJBBean10.class);

  public List<Accounting10> searchAccountingData(Map<String, Object> header, Date dateToSearch) throws Exception {

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

  public List<Accounting10> saveAccountingData (Map<String, Object> header,List<Accounting10> accounting10s) throws Exception {
    List<Accounting10> accounting10sFinal;
    if(accounting10s == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accounting10s"));
    }
    accounting10sFinal = new ArrayList<>();
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
        account.getAmountUsd().getValue() == null ? new NullParam(Types.NUMERIC) : new InParam( account.getAmountUsd().getValue(), Types.NUMERIC),
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

}
