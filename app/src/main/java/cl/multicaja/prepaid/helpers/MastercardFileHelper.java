package cl.multicaja.prepaid.helpers;

import cl.multicaja.prepaid.model.v10.*;
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
  private final CcrFile10 ccrFile10;

  private MastercardFileHelper(){
    ccrFile10 = new CcrFile10();
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

  public CcrFile10 getValidCurrencyDetailRecordClp(final InputStream inputStream) throws Exception {
    Integer records = -1;
    Map<String, String> recordsMap = new HashMap<String, String>();
    Scanner scanner = new Scanner(inputStream);

    final String headerRecord = scanner.hasNextLine() ? scanner.nextLine() : null;

    //Se valida header record
    validateHeader(headerRecord);

    while (scanner.hasNextLine()) {
      final String line = scanner.nextLine();
      if(line.startsWith(ccrFile10.getCcrDetailRecord10().getCurrencyClpPrefix())) {
        recordsMap.put(CcrLayout10.DETAIL_PREFIX, line);
      } else if (line.startsWith(CcrLayout10.TRAILER_PREFIX)){
        recordsMap.put(CcrLayout10.TRAILER_PREFIX, line);
      }
      records++;
    }

    //Se valida trailer record
    validateTrailer(recordsMap.get(CcrLayout10.TRAILER_PREFIX), records);
    //Se valida CLP detail record
    validateClpDetail(recordsMap.get(CcrLayout10.DETAIL_PREFIX));
    return ccrFile10;
  }

  private void validateHeader(String headerRecord) throws Exception {
    if(headerRecord == null) {
      throw new Exception("Header record not found");
    }

    ccrFile10.getCcrHeaderRecord10().setHeaderRecord(headerRecord);
    //Validar Prefijo
    if(!CcrLayout10.HEADER_PREFIX.equals(ccrFile10.getCcrHeaderRecord10().getHeader())) {
      throw new Exception(String.format("Unexpected HEADER prefix -> actual [%s], expected [%s]", ccrFile10.getCcrHeaderRecord10().getHeader(), CcrLayout10.HEADER_PREFIX));
    }
    //Validar Fecha
    if(!isHeaderDateTimeFormat(ccrFile10.getCcrHeaderRecord10().getDate(), ccrFile10.getCcrHeaderRecord10().DATE_FORMAT)) {
      throw new Exception(String.format("Invalid DATE FORMAT [%s]", ccrFile10.getCcrHeaderRecord10().getDate()));
    }
    //Validar Hora
    if(!isHeaderDateTimeFormat(ccrFile10.getCcrHeaderRecord10().getTime(), ccrFile10.getCcrHeaderRecord10().TIME_FORMAT)) {
      throw new Exception(String.format("Invalid TIME FORMAT [%s]", ccrFile10.getCcrHeaderRecord10().getTime()));
    }

    ccrFile10.getCcrHeaderRecord10().setFileCreationDatetime(ccrFile10.getCcrHeaderRecord10().getDate().concat(ccrFile10.getCcrHeaderRecord10().getTime()));

    //Validar version
    if(Integer.parseInt(ccrFile10.getCcrHeaderRecord10().getVersion()) < ccrFile10.getCcrHeaderRecord10().MIN_VERSION) {
      throw new Exception(String.format("Invalid FILE VERSION [%s]", ccrFile10.getCcrHeaderRecord10().getVersion()));
    }
  }

  private void validateTrailer(String trailerRecord, Integer records) throws Exception {
    if(trailerRecord == null) {
      throw new Exception("Trailer record not found");
    }
    ccrFile10.getCcrTrailerRecord10().setTrailerRecord(trailerRecord);

    //Validar que el total de registros detail sea igual al los registros informados en trailer record
    if(Integer.parseInt(ccrFile10.getCcrTrailerRecord10().getDate()) != records){
      throw new Exception("Invalid content");
    }
  }

  private void validateClpDetail(String detailRecord) throws Exception {
    if(detailRecord == null) {
      throw new Exception("Clp detail record not found");
    }
    ccrFile10.getCcrDetailRecord10().setDetailRecord(detailRecord);
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
