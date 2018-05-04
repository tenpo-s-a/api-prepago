package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class PrepaidCardBalance {

  private AmountAndCurrency primaryBalance;
  private AmountAndCurrency secondaryBalance;

  public AmountAndCurrency getPrimaryBalance() {
    return primaryBalance;
  }

  public void setPrimaryBalance(AmountAndCurrency primaryBalance) {
    this.primaryBalance = primaryBalance;
  }

  public AmountAndCurrency getSecondaryBalance() {
    return secondaryBalance;
  }

  public void setSecondaryBalance(AmountAndCurrency secondaryBalance) {
    this.secondaryBalance = secondaryBalance;
  }
}
