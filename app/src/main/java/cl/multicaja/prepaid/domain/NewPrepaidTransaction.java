package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class NewPrepaidTransaction {

  private NewAmountAndCurrency amount;
  private Integer newTransactionId;

  public NewAmountAndCurrency getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency amount) {
    this.amount = amount;
  }

  public Integer getNewTransactionId() {
    return newTransactionId;
  }

  public void setNewTransactionId(Integer newTransactionId) {
    this.newTransactionId = newTransactionId;
  }
}
