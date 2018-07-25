package cl.multicaja.prepaid.helpers;

import cl.multicaja.prepaid.model.v10.CcrDetailRecord10;
import cl.multicaja.prepaid.model.v10.CcrHeaderRecord10;
import cl.multicaja.prepaid.model.v10.CcrTrailerRecord10;
import cl.multicaja.prepaid.model.v10.CurrencyConversion10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MastercardFileHelper {

  private static Log log = LogFactory.getLog(MastercardFileHelper.class);
  private static MastercardFileHelper INSTANCE = new MastercardFileHelper();
  private final CcrHeaderRecord10 ccrHeaderRecord10;
  private final CcrDetailRecord10 ccrDetailRecord10;
  private final CcrTrailerRecord10 ccrTrailerRecord10;

  private MastercardFileHelper(){
    ccrHeaderRecord10 = new CcrHeaderRecord10();
    ccrDetailRecord10 = new CcrDetailRecord10();
    ccrTrailerRecord10 = new CcrTrailerRecord10();
  }

  private static void createInstance() {
    if (INSTANCE == null) {
      synchronized(MastercardFileHelper.class) {
        if (INSTANCE == null) {
          INSTANCE = new MastercardFileHelper();
        }
      }
    }
  }

  public static MastercardFileHelper getInstance() {
    if (INSTANCE == null) {
      createInstance();
    }
    return INSTANCE;
  }

  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  public CcrDetailRecord10 getValidCurrencyDetailRecordClp(final InputStream inputStream) throws Exception {
    Integer records = -1;
    Map<String, String> recordsMap = new HashMap<String, String>();
    Scanner scanner = new Scanner(inputStream);

    final String headerRecord = scanner.hasNextLine() ? scanner.nextLine() : null;

    //Se valida header record
    validateHeader(headerRecord);

    while (scanner.hasNextLine()) {
      final String line = scanner.nextLine();
      if(line.startsWith(ccrDetailRecord10.getCurrencyClpPrefix())) {
        recordsMap.put(CurrencyConversion10.DETAIL_PREFIX, line);
      } else if (line.startsWith(CurrencyConversion10.TRAILER_PREFIX)){
        recordsMap.put(CurrencyConversion10.TRAILER_PREFIX, line);
      }
      records++;
    }

    //Se valida trailer record
    validateTrailer(recordsMap.get(CurrencyConversion10.TRAILER_PREFIX), records);
    //Se valida CLP detail record
    validateClpDetail(recordsMap.get(CurrencyConversion10.DETAIL_PREFIX));
    return ccrDetailRecord10;
  }

  private void validateHeader(String headerRecord) throws Exception {
    if(headerRecord == null) {
      throw new Exception("Header record not found");
    }

    ccrHeaderRecord10.setHeaderRecord(headerRecord);
    //Validar Prefijo
    if(!CurrencyConversion10.HEADER_PREFIX.equals(ccrHeaderRecord10.getHeader())) {
      throw new Exception(String.format("Unexpected HEADER prefix -> actual [%s], expected [%s]", ccrHeaderRecord10.getHeader(), CurrencyConversion10.HEADER_PREFIX));
    }
    //Validar Fecha
    if(!isHeaderDateTimeFormat(ccrHeaderRecord10.getDate(), ccrHeaderRecord10.DATE_FORMAT)) {
      throw new Exception(String.format("Invalid DATE FORMAT [%s]", ccrHeaderRecord10.getDate()));
    }
    //Validar Hora
    if(!isHeaderDateTimeFormat(ccrHeaderRecord10.getTime(), ccrHeaderRecord10.TIME_FORMAT)) {
      throw new Exception(String.format("Invalid TIME FORMAT [%s]", ccrHeaderRecord10.getTime()));
    }
    //Validar version
    if(Integer.parseInt(ccrHeaderRecord10.getVersion()) < ccrHeaderRecord10.MIN_VERSION) {
      throw new Exception(String.format("Invalid FILE VERSION [%s]", ccrHeaderRecord10.getVersion()));
    }
  }

  private void validateTrailer(String trailerRecord, Integer records) throws Exception {
    if(trailerRecord == null) {
      throw new Exception("Trailer record not found");
    }
    ccrTrailerRecord10.setTrailerRecord(trailerRecord);

    //Validar que el total de registros detail sea igual al los registros informados en trailer record
    if(Integer.parseInt(ccrTrailerRecord10.getDate()) != records){
      throw new Exception("Invalid content");
    }
  }

  private void validateClpDetail(String detailRecord) throws Exception {
    if(detailRecord == null) {
      throw new Exception("Clp detail record not found");
    }
    ccrDetailRecord10.setDetailRecord(detailRecord);
  }

  private Boolean isHeaderDateTimeFormat(String dateToValidate, String format){
    if(dateToValidate == null){
      return false;
    }

    SimpleDateFormat sdf = new SimpleDateFormat(format);
    sdf.setLenient(false);
    try {
      sdf.parse(dateToValidate);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }

}
