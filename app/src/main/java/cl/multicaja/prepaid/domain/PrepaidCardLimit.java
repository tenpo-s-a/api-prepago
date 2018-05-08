package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class PrepaidCardLimit extends BaseModel {

  private String id;
  private String name;
  private AmountAndCurrency amount;

  public PrepaidCardLimit() {
    super();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AmountAndCurrency getAmount() {
    return amount;
  }

  public void setAmount(AmountAndCurrency amount) {
    this.amount = amount;
  }

}
