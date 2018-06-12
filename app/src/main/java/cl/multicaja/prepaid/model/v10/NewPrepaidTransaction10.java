package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class NewPrepaidTransaction10 extends BaseModel {

  private NewAmountAndCurrency10 amount;
  private Integer newTransactionId;

  public NewPrepaidTransaction10() {
    super();
  }

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
    this.amount = amount;
  }

  public Integer getNewTransactionId() {
    return newTransactionId;
  }

  public void setNewTransactionId(Integer newTransactionId) {
    this.newTransactionId = newTransactionId;
  }

}
