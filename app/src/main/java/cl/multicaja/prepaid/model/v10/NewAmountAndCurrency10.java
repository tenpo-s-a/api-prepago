package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;

/**
 * @author abarazarte
 */
public class NewAmountAndCurrency10 extends BaseModel {

  private CurrencyCodes currencyCode;
  private BigDecimal value;

  public NewAmountAndCurrency10() {
    super();
  }

  public CurrencyCodes getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(CurrencyCodes currencyCode) {
    this.currencyCode = currencyCode;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

}
