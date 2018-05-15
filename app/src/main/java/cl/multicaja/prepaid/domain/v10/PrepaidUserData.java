package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

import java.util.List;

/**
 * @author abarazarte
 */
public class PrepaidUserData extends BaseModel {

  private PrepaidCard10 card;
  private List<PrepaidCardLimit> limits;

  public PrepaidUserData() {
    super();
  }

  public PrepaidCard10 getCard() {
    return card;
  }

  public void setCard(PrepaidCard10 card) {
    this.card = card;
  }

  public List<PrepaidCardLimit> getLimits() {
    return limits;
  }

  public void setLimits(List<PrepaidCardLimit> limits) {
    this.limits = limits;
  }

}
