package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class BalanceDTO {

  private Amount primary_balance;
  private Amount secondary_balance;

  public Amount getPrimaryBalance() {
    return primary_balance;
  }

  public void setPrimaryBalance(Amount primary_balance) {
    this.primary_balance = primary_balance;
  }

  public Amount getSecondaryBalance() {
    return secondary_balance;
  }

  public void setSecondaryBalance(Amount secondary_balance) {
    this.secondary_balance = secondary_balance;
  }
}
