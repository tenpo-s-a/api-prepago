package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.tecnocom.constants.CodigoMoneda;

import java.math.BigDecimal;

/**
 * @author abarazarte
 */
public class NewAmountAndCurrency10 extends BaseModel {

  private CodigoMoneda currencyCode;
  private BigDecimal value;

  public NewAmountAndCurrency10() {
    super();
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

}
