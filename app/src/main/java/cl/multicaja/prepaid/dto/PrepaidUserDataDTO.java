package cl.multicaja.prepaid.dto;

import java.util.List;

/**
 * @author abarazarte
 */
public class PrepaidUserDataDTO {

  private PrepaidCardDTO card;
  private List<PrepaidCardLimit> limits;

  public PrepaidCardDTO getCard() {
    return card;
  }

  public void setCard(PrepaidCardDTO card) {
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
  private Amount amount;

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

  public Amount getAmount() {
    return amount;
  }

  public void setAmount(Amount amount) {
    this.amount = amount;
  }
}
