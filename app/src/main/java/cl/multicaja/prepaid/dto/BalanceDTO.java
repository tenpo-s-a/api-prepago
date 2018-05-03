package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class BalanceDTO {

  private Amount primaryBalance;
  private Amount secondaryBalance;

  public Amount getPrimaryBalance() {
    return primaryBalance;
  }

  public void setPrimaryBalance(Amount primaryBalance) {
    this.primaryBalance = primaryBalance;
  }

  public Amount getSecondaryBalance() {
    return secondaryBalance;
  }

  public void setSecondaryBalance(Amount secondaryBalance) {
    this.secondaryBalance = secondaryBalance;
  }
}
