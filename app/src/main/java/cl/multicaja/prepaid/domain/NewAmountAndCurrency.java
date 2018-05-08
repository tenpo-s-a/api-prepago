package cl.multicaja.prepaid.domain;

import java.math.BigDecimal;

/**
 * @author abarazarte
 */
public class NewAmountAndCurrency {

  private Integer currencyCode;
  private BigDecimal value;

  public Integer getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(Integer currencyCode) {
    this.currencyCode = currencyCode;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

}
