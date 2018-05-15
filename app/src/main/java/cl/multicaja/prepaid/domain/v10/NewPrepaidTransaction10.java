package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class NewPrepaidTransaction10 extends BaseModel {

  private AmountAndCurrency10 amount;
  private Integer newTransactionId;

  public NewPrepaidTransaction10() {
    super();
  }

  public AmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(AmountAndCurrency10 amount) {
    this.amount = amount;
  }

  public Integer getNewTransactionId() {
    return newTransactionId;
  }

  public void setNewTransactionId(Integer newTransactionId) {
    this.newTransactionId = newTransactionId;
  }

}
