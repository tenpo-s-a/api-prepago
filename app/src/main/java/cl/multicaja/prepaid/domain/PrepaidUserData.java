package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;

import java.util.List;

/**
 * @author abarazarte
 */
public class PrepaidUserData extends BaseModel {

  private PrepaidCard card;
  private List<PrepaidCardLimit> limits;

  public PrepaidUserData() {
    super();
  }

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
