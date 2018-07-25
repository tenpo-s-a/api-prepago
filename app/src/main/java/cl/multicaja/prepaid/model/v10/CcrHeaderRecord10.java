package cl.multicaja.prepaid.model.v10;

public class CcrHeaderRecord10 {

  public static final String DATE_FORMAT = "yyyyMMdd";
  public static final String TIME_FORMAT = "HHmmss";
  public static final Integer MIN_VERSION = 1;

  private String headerRecord;
  private final CurrencyConversion10 HEADER = new CurrencyConversion10(0, 1, "AN");
  private final CurrencyConversion10 DATE = new CurrencyConversion10(1, 8, "N");
  private final CurrencyConversion10 TIME = new CurrencyConversion10(9, 6, "AN");
  private final CurrencyConversion10 VERSION = new CurrencyConversion10(15, 1, "AN");

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


}
