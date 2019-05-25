package cl.multicaja.prepaid.kafka.events;

import cl.multicaja.prepaid.kafka.events.model.Transaction;

public class TransactionEvent extends BaseEvent {

  private String accountId;
  private String cardId;
  private Transaction transaction;

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getCardId() {
    return cardId;
  }

  public void setCardId(String cardId) {
    this.cardId = cardId;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
  }
}
