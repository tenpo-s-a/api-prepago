package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.MastercardFileHelper;
import cl.multicaja.prepaid.model.v10.CcrFile10;
import cl.multicaja.prepaid.model.v10.CurrencyUsd;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;
import static cl.multicaja.core.model.Errors.FILE_ALREADY_PROCESSED;


public class PendingCurrencyModification10 extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(PendingCurrencyModification10.class);
  public PendingCurrencyModification10(BaseRoute10 route) {
    super(route);
  }

  public Processor processCurrencyConvertionFile() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info("Proccess file name : " + fileName);
        try{
          CcrFile10 ccrFile10 = MastercardFileHelper.getInstance().getValidCurrencyDetailRecordClp(inputStream);
          if(ccrFile10 != null) {
            log.info(String.format("BUY [%s], MID [%s], SELL [%s]", ccrFile10.getCcrDetailRecord10().getBuyCurrencyConversion(), ccrFile10.getCcrDetailRecord10().getMidCurrencyConversion(), ccrFile10.getCcrDetailRecord10().getSellCurrencyConversion()));
            CurrencyUsd currencyUsd = getRoute().getPrepaidCardEJBBean10().getCurrencyUsd();
            if(currencyUsd != null) {
              if(currencyUsd.getFileName().equals(fileName)) {
                throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), "File already processed");
              }
            }
            currencyUsd = getCurrencyUsd(fileName, ccrFile10);
            getRoute().getPrepaidCardEJBBean10().updateUsdValue(currencyUsd);
          }
        } catch (Exception ex){
          String msg = String.format("Error processing file [%s]", fileName);
          log.error(msg, ex);
          throw new ValidationException(FILE_ALREADY_PROCESSED.getValue(), msg);
        }
      }
    };
  }

  private CurrencyUsd getCurrencyUsd(String fileName, CcrFile10 ccrFile10) {
    CurrencyUsd currencyUsd = new CurrencyUsd();
    currencyUsd.setFileName(fileName);
    currencyUsd.setCurrencyExponent(Integer.parseInt(ccrFile10.getCcrDetailRecord10().getCurrencyExponent()));
    currencyUsd.setExpirationUsdDate(getExpirationUsdDate());
    currencyUsd.setBuyCurrencyConvertion(Double.parseDouble(ccrFile10.getCcrDetailRecord10().getBuyCurrencyConversion()));
    currencyUsd.setSellCurrencyConvertion(Double.parseDouble(ccrFile10.getCcrDetailRecord10().getSellCurrencyConversion()));
    currencyUsd.setMidCurrencyConvertion(Double.parseDouble(ccrFile10.getCcrDetailRecord10().getMidCurrencyConversion()));
    return currencyUsd;
  }

  private Timestamp getExpirationUsdDate(){
    //TODO: Debe ser reemplazado por el tiemp de expiracion definido por Mastercard
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(Calendar.DATE, 1);
    return new Timestamp(c.getTime().getTime());
  }

}
