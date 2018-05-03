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

  public String getProcessorTransactionId() {
    return processor_transaction_id;
  }

  public void setProcessorTransactionId(String processor_transaction_id) {
    this.processor_transaction_id = processor_transaction_id;
  }
}
