package cl.multicaja.prepaid.model.v10;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CcrHeaderRecord10 {

  public static final String DATE_FORMAT = "yyyyMMdd";
  public static final String TIME_FORMAT = "HHmmss";
  public static final Integer MIN_VERSION = 1;

  private String headerRecord;

  private Timestamp fileCreationDatetime;
  private final CcrLayout10 HEADER = new CcrLayout10(0, 1, "AN");
  private final CcrLayout10 DATE = new CcrLayout10(1, 8, "N");
  private final CcrLayout10 TIME = new CcrLayout10(9, 6, "AN");
  private final CcrLayout10 VERSION = new CcrLayout10(15, 1, "AN");

  public void setHeaderRecord(String headerRecord) {
    this.headerRecord = headerRecord;
  }

  public String getHeader() {
    return this.headerRecord.substring(this.HEADER.getStart(), this.HEADER.getEnd());
  }

  public String getDate() {
    return this.headerRecord.substring(this.DATE.getStart(), this.DATE.getEnd());
  }

  public String getTime() {
    return this.headerRecord.substring(this.TIME.getStart(), this.TIME.getEnd());
  }

  public String getVersion() {
    return this.headerRecord.substring(this.VERSION.getStart(), this.VERSION.getEnd());
  }

  public Timestamp getFileCreationDatetime() {
    return fileCreationDatetime;
  }

  public void setFileCreationDatetime(Timestamp fileCreationDatetime) {
    this.fileCreationDatetime = fileCreationDatetime;
  }

  public void setFileCreationDatetime(String strDate) throws Exception {
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT.concat(TIME_FORMAT));
    Date parsedDate = dateFormat.parse(strDate);
    this.fileCreationDatetime = new Timestamp(parsedDate.getTime());
  }

}
