package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class NewPrepaidTransactionDTO {

  private NewAmount amount;
  private Integer newTransactionId;

  public NewAmount getAmount() {
    return amount;
  }

  public void setAmount(NewAmount amount) {
    this.amount = amount;
  }

  public Integer getNewTransactionId() {
    return newTransactionId;
  }

  public void setNewTransactionId(Integer newTransactionId) {
    this.newTransactionId = newTransactionId;
  }
}
