package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class PrepaidCardLimit10 extends BaseModel {

  private String id;
  private String name;
  private AmountAndCurrency10 amount;

  public PrepaidCardLimit10() {
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

  public AmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(AmountAndCurrency10 amount) {
    this.amount = amount;
  }

}
