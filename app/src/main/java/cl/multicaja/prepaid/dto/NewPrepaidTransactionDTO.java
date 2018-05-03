package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class NewPrepaidTransactionDTO {

  private NewAmount amount;
  private Integer new_transaction_id;

  public NewAmount getAmount() {
    return amount;
  }

  public void setAmount(NewAmount amount) {
    this.amount = amount;
  }

  public Integer getNewTransactionId() {
    return new_transaction_id;
  }

  public void setNewTransactionId(Integer new_transaction_id) {
    this.new_transaction_id = new_transaction_id;
  }
}
