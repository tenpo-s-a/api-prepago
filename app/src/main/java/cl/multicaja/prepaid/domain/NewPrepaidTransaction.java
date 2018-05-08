package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class NewPrepaidTransaction extends BaseModel {

  private AmountAndCurrency amount;
  private Integer newTransactionId;

  public NewPrepaidTransaction() {
    super();
  }

  public AmountAndCurrency getAmount() {
    return amount;
  }

  public void setAmount(AmountAndCurrency amount) {
    this.amount = amount;
  }

  public Integer getNewTransactionId() {
    return newTransactionId;
  }

  public void setNewTransactionId(Integer newTransactionId) {
    this.newTransactionId = newTransactionId;
  }

}
