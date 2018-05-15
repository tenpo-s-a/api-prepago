package cl.multicaja.prepaid.model.v10;

/**
 * @author abarazarte
 */
public class AmountAndCurrency10 extends NewAmountAndCurrency10 {

  private String currencyDescription;

  public AmountAndCurrency10() {
    super();
  }

  public String getCurrencyDescription() {
    return currencyDescription;
  }

  public void setCurrencyDescription(String currencyDescription) {
    this.currencyDescription = currencyDescription;
  }

}
