package cl.multicaja.prepaid.model.v10;

import java.math.BigDecimal;

public class CcrDetailRecord10 {

  private final String CURRENCY_CODE_CLP = "152";
  private final String CURRENCY_CODE_USD = "840";

  private String detailRecord;
  private final CcrLayout10 DETAIL = new CcrLayout10(0, 1, "AN");
  private final CcrLayout10 CURRENCY_CODE = new CcrLayout10(1, 3, "N");
  private final CcrLayout10 REFERENCE_CURRENCY_CODE = new CcrLayout10(4, 3, "N");
  private final CcrLayout10 CURRENCY_EXPONENT = new CcrLayout10(7, 1, "N");
  private final CcrLayout10 RATE_CLASS = new CcrLayout10(8, 1, "AN");
  private final CcrLayout10 RATE_FORMAT_INDICATOR = new CcrLayout10(9, 1, "AN");
  private final CcrLayout10 BUY_CURRENCY_CONVERTION = new CcrLayout10(10, 15, "N");
  private final CcrLayout10 MID_CURRENCY_CONVERTION = new CcrLayout10(25, 15, "N");
  private final CcrLayout10 SELL_CURRENCY_CONVERTION = new CcrLayout10(40, 15, "N");

  public void setDetailRecord(String detailRecord) {
    this.detailRecord = detailRecord;
  }

  public String getDetail() {
    return this.detailRecord.substring(DETAIL.getStart(), DETAIL.getEnd());
  }

  public String getCurrencyCode() {
    return this.detailRecord.substring(CURRENCY_CODE.getStart(), CURRENCY_CODE.getEnd());
  }

  public String getReferenceCurrencyCode() {
    return this.detailRecord.substring(REFERENCE_CURRENCY_CODE.getStart(), REFERENCE_CURRENCY_CODE.getEnd());
  }

  public String getCurrencyExponent() {
    return this.detailRecord.substring(CURRENCY_EXPONENT.getStart(), CURRENCY_EXPONENT.getEnd());
  }

  public String getRateClass() {
    return this.detailRecord.substring(RATE_CLASS.getStart(), RATE_CLASS.getEnd());
  }

  public String getRateFormatIndicator() {
    return this.detailRecord.substring(RATE_FORMAT_INDICATOR.getStart(), RATE_FORMAT_INDICATOR.getEnd());
  }

  public String getBuyCurrencyConversion() {
    return this.getScaledValue(this.detailRecord.substring(BUY_CURRENCY_CONVERTION.getStart(), BUY_CURRENCY_CONVERTION.getEnd())).toString();
  }

  public String getMidCurrencyConversion() {
    return this.getScaledValue(this.detailRecord.substring(MID_CURRENCY_CONVERTION.getStart(), MID_CURRENCY_CONVERTION.getEnd())).toString();
  }

  public String getSellCurrencyConversion() {
    return this.getScaledValue(this.detailRecord.substring(SELL_CURRENCY_CONVERTION.getStart(), SELL_CURRENCY_CONVERTION.getEnd())).toString();
  }

  public String getCurrencyClpPrefix() {
    return CcrLayout10.DETAIL_PREFIX.concat(CURRENCY_CODE_CLP).concat(CURRENCY_CODE_USD);
  }

  private BigDecimal getScaledValue(String currencyValue) {
    Integer decimal = 7;
    Long field = Long.parseLong(currencyValue);
    BigDecimal scaled = new BigDecimal(field).scaleByPowerOfTen(-decimal);
    return scaled;
  }

}
