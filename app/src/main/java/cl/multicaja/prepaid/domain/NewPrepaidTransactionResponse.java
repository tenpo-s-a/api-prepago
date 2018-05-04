package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class NewPrepaidTransactionResponse {

  private PrepaidCardBalance balance;
  private String processor_transaction_id;

  public PrepaidCardBalance getBalance() {
    return balance;
  }

  public void setBalance(PrepaidCardBalance balance) {
    this.balance = balance;
  }

  public String getProcessorTransactionId() {
    return processor_transaction_id;
  }

  public void setProcessorTransactionId(String processor_transaction_id) {
    this.processor_transaction_id = processor_transaction_id;
  }
}
