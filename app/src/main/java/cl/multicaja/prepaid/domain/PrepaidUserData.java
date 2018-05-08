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
