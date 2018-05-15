package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class PrepaidCardBalance extends BaseModel {

  private AmountAndCurrency10 primaryBalance;
  private AmountAndCurrency10 secondaryBalance;

  public PrepaidCardBalance() {
    super();
  }

  public AmountAndCurrency10 getPrimaryBalance() {
    return primaryBalance;
  }

  public void setPrimaryBalance(AmountAndCurrency10 primaryBalance) {
    this.primaryBalance = primaryBalance;
  }

  public AmountAndCurrency10 getSecondaryBalance() {
    return secondaryBalance;
  }

  public void setSecondaryBalance(AmountAndCurrency10 secondaryBalance) {
    this.secondaryBalance = secondaryBalance;
  }

}
