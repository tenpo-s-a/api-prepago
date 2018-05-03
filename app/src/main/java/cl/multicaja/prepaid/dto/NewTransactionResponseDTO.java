package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class NewTransactionResponseDTO {

  private BalanceDTO balance;
  private String processor_transaction_id;

  public BalanceDTO getBalance() {
    return balance;
  }

  public void setBalance(BalanceDTO balance) {
    this.balance = balance;
  }

  public String getProcessor_transaction_id() {
    return processor_transaction_id;
  }

  public void setProcessor_transaction_id(String processor_transaction_id) {
    this.processor_transaction_id = processor_transaction_id;
  }
}
