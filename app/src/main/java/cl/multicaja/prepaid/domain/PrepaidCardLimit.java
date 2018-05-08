package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class PrepaidCardLimit {

  private String id;
  private String name;
  private AmountAndCurrency amount;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AmountAndCurrency getAmount() {
    return amount;
  }

  public void setAmount(AmountAndCurrency amount) {
    this.amount = amount;
  }

}
