package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.mastercard.MastercardFileHelper;
import cl.multicaja.prepaid.model.v10.CcrFile10;
import cl.multicaja.prepaid.model.v10.CurrencyUsd;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.InputStream;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class MastercardCurrencyUpdateEJBBean10 extends PrepaidBaseEJBBean10 implements MastercardCurrencyUpdateEJB10 {

  private static Log log = LogFactory.getLog(MastercardCurrencyUpdateEJBBean10.class);

  public MastercardCurrencyUpdateEJBBean10() {
    super();
  }

  @Override
  public CurrencyUsd getCurrencyUsd() throws Exception {
    String sp = getSchema() + ".mc_prp_buscar_valor_usd_v10";
    RowMapper rm = (Map<String, Object> row) -> {
      CurrencyUsd c = new CurrencyUsd();
      c.setId(getNumberUtils().toLong(row.get("_id"), null));
      c.setFileName(String.valueOf(row.get("_nombre_archivo")));
      c.setCreationDate((Timestamp)row.get("_fecha_creacion"));
      c.setEndDate((Timestamp)row.get("_fecha_termino"));
      c.setExpirationUsdDate((Timestamp)row.get("_fecha_expiracion_usd"));
      c.setBuyCurrencyConvertion(getNumberUtils().toDouble(row.get("_precio_compra"), null));
      c.setMidCurrencyConvertion(getNumberUtils().toDouble(row.get("_precio_medio"), null));
      c.setSellCurrencyConvertion(getNumberUtils().toDouble(row.get("_precio_venta"), null));
      c.setCurrencyExponent(getNumberUtils().toInteger(row.get("_exponente"), null));
      c.setDayCurrencyConvertion(getNumberUtils().toDouble(row.get("_precio_dia"), null));
      return c;
    };
    Map<String, Object> resp = getDbUtils().execute(sp, rm);
    return resp.get("result") != null ? (CurrencyUsd) ((List) resp.get("result")).get(0) : null;
  }

  @Override
  public void updateUsdValue(CurrencyUsd currencyUsd) throws Exception {
    Object[] params = {
      new InParam(currencyUsd.getFileName(), Types.VARCHAR),
      new InParam(currencyUsd.getExpirationUsdDate(), Types.TIMESTAMP),
      new InParam(currencyUsd.getBuyCurrencyConvertion(), Types.NUMERIC),
      new InParam(currencyUsd.getSellCurrencyConvertion(), Types.NUMERIC),
      new InParam(currencyUsd.getMidCurrencyConvertion(), Types.NUMERIC),
      new InParam(currencyUsd.getCurrencyExponent(), Types.NUMERIC),
      new InParam(currencyUsd.getSellCurrencyConvertion(),Types.NUMERIC),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_actualiza_valor_usd_v11", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_prp_actualiza_valor_usd_v11 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  public void processMastercardUsdFile(InputStream inputStream, String fileName) throws Exception {
    try{
      CcrFile10 ccrFile10 = MastercardFileHelper.getInstance().getValidCurrencyDetailRecordClp(inputStream);
      inputStream.close();
      if(ccrFile10 != null) {
        log.info(String.format("BUY [%s], MID [%s], SELL [%s]", ccrFile10.getCcrDetailRecord10().getBuyCurrencyConversion(), ccrFile10.getCcrDetailRecord10().getMidCurrencyConversion(), ccrFile10.getCcrDetailRecord10().getSellCurrencyConversion()));
        CurrencyUsd currencyUsd = this.getCurrencyUsd();
        if(currencyUsd != null) {
          if(currencyUsd.getFileName().equals(fileName)) {
            throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), "File already processed");
          }
        }
        currencyUsd = getCurrencyUsd(fileName, ccrFile10);
        this.updateUsdValue(currencyUsd);
      }
    } catch (Exception ex){
      inputStream.close();
      String msg = String.format("Error processing file [%s]", fileName);
      log.error(msg, ex);
      throw new ValidationException(FILE_ALREADY_PROCESSED.getValue(), msg);
    }
  }

  private CurrencyUsd getCurrencyUsd(String fileName, CcrFile10 ccrFile10) {
    CurrencyUsd currencyUsd = new CurrencyUsd();
    currencyUsd.setFileName(fileName);
    currencyUsd.setCurrencyExponent(Integer.parseInt(ccrFile10.getCcrDetailRecord10().getCurrencyExponent()));
    currencyUsd.setExpirationUsdDate(getExpirationUsdDate());
    currencyUsd.setBuyCurrencyConvertion(Double.parseDouble(ccrFile10.getCcrDetailRecord10().getBuyCurrencyConversion()));
    currencyUsd.setSellCurrencyConvertion(Double.parseDouble(ccrFile10.getCcrDetailRecord10().getSellCurrencyConversion()));
    currencyUsd.setMidCurrencyConvertion(Double.parseDouble(ccrFile10.getCcrDetailRecord10().getMidCurrencyConversion()));
    currencyUsd.setDayCurrencyConvertion(CalculationsHelper.dayCurrencyVariation * currencyUsd.getSellCurrencyConvertion());
    return currencyUsd;
  }

  private Timestamp getExpirationUsdDate(){
    //FIXME: Debe ser reemplazado por el tiempo de expiracion definido por Mastercard
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(Calendar.DATE, 1);
    return new Timestamp(c.getTime().getTime());
  }
}
