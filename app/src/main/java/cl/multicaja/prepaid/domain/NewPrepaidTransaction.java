package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class NewPrepaidTransaction {

  private AmountAndCurrency amount;
  private Integer newTransactionId;

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
