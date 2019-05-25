package cl.multicaja.prepaid.kafka.events;

import cl.multicaja.prepaid.kafka.events.model.Account;

public class AccountEvent extends BaseEvent {

  private Account account;

  public AccountEvent() {
    super();
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }
}
