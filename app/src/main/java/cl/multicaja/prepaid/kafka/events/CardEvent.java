package cl.multicaja.prepaid.kafka.events;

import cl.multicaja.prepaid.kafka.events.model.Card;

public class CardEvent extends BaseEvent {

  private String accountId;
  private Card card;

  public CardEvent() {
    super();
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public Card getCard() {
    return card;
  }

  public void setCard(Card card) {
    this.card = card;
  }
}
