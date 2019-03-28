package cl.multicaja.prepaid.model.v11;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;

/**
 * @author esteban
 */
public class NewPrepaidTransaction11 extends BaseModel {

  private NewAmountAndCurrency10 amount;
  private Integer newTransactionId;

  public NewPrepaidTransaction11() {
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
