package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;

/**
 * @author abarazarte
 */
public class NewAmountAndCurrency extends BaseModel {

  private Integer currencyCode;
  private BigDecimal value;

  public NewAmountAndCurrency() {
    super();
  }

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
