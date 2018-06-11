package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class PrepaidBalance10 extends BaseModel {

  private NewAmountAndCurrency10 primary;
  private NewAmountAndCurrency10 secondary;
  private boolean updated;

  public PrepaidBalance10() {
    super();
  }

  public PrepaidBalance10(NewAmountAndCurrency10 primary, NewAmountAndCurrency10 secondary, boolean updated) {
    this.primary = primary;
    this.secondary = secondary;
    this.updated = updated;
  }

  public NewAmountAndCurrency10 getPrimary() {
    return primary;
  }

  public void setPrimary(NewAmountAndCurrency10 primary) {
    this.primary = primary;
  }

  public NewAmountAndCurrency10 getSecondary() {
    return secondary;
  }

  public void setSecondary(NewAmountAndCurrency10 secondary) {
    this.secondary = secondary;
  }

  public boolean isUpdated() {
    return updated;
  }

  public void setUpdated(boolean updated) {
    this.updated = updated;
  }
}
