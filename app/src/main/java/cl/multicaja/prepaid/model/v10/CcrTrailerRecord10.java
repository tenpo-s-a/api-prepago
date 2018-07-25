package cl.multicaja.prepaid.model.v10;

public class CcrTrailerRecord10 {
  private CurrencyConversion10 DATE = new CurrencyConversion10(1, 6, "N");
  private String trailerRecord;

  public void setTrailerRecord(String trailerRecord) {
    this.trailerRecord = trailerRecord;
  }

  public String getDate() {
    return this.trailerRecord.substring(DATE.getStart(), DATE.getEnd());
  }
}
