package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class PrepaidCardBalance extends BaseModel {

  private AmountAndCurrency primaryBalance;
  private AmountAndCurrency secondaryBalance;

  public PrepaidCardBalance() {
    super();
  }

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
