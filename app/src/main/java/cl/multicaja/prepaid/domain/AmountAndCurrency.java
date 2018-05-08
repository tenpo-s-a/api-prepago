package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class AmountAndCurrency extends NewAmountAndCurrency {

  private String currencyDescription;

  public AmountAndCurrency() {
    super();
  }

  public String getCurrencyDescription() {
    return currencyDescription;
  }

  public void setCurrencyDescription(String currencyDescription) {
    this.currencyDescription = currencyDescription;
  }

}
