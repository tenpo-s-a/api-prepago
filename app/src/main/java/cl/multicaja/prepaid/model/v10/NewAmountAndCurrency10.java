package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.tecnocom.constants.CodigoMoneda;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author abarazarte
 */
public class NewAmountAndCurrency10 extends BaseModel {

  private CodigoMoneda currencyCode;
  private BigDecimal value;

  public NewAmountAndCurrency10() {
    super();
  }

  /**
   * Crea una intancia con currencyCode por defecto en CodigoMoneda.CHILE_CLP
   *
   * @param value
   */
  public NewAmountAndCurrency10(BigDecimal value) {
    this.value = value;
    this.currencyCode = CodigoMoneda.CHILE_CLP;
  }

  public NewAmountAndCurrency10(BigDecimal value, CodigoMoneda currencyCode) {
    this.value = value;
    this.currencyCode = currencyCode;
  }

  public CodigoMoneda getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(CodigoMoneda currencyCode) {
    this.currencyCode = currencyCode;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NewAmountAndCurrency10)) return false;
    NewAmountAndCurrency10 that = (NewAmountAndCurrency10) o;
    return getCurrencyCode() == that.getCurrencyCode() &&
      Objects.equals(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCurrencyCode(), getValue());
  }
}
