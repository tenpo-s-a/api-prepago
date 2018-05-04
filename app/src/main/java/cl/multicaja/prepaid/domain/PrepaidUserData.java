package cl.multicaja.prepaid.domain;

import java.util.List;

/**
 * @author abarazarte
 */
public class PrepaidUserData {

  private PrepaidCard card;
  private List<PrepaidCardLimit> limits;

  public PrepaidCard getCard() {
    return card;
  }

  public void setCard(PrepaidCard card) {
    this.card = card;
  }

  public List<PrepaidCardLimit> getLimits() {
    return limits;
  }

  public void setLimits(List<PrepaidCardLimit> limits) {
    this.limits = limits;
  }
}

class PrepaidCardLimit {
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
