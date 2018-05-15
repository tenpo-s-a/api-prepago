package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

import java.util.List;

/**
 * @author abarazarte
 */
public class PrepaidUserData10 extends BaseModel {

  private PrepaidCard10 card;
  private List<PrepaidCardLimit10> limits;

  public PrepaidUserData10() {
    super();
  }

  public PrepaidCard10 getCard() {
    return card;
  }

  public void setCard(PrepaidCard10 card) {
    this.card = card;
  }

  public List<PrepaidCardLimit10> getLimits() {
    return limits;
  }

  public void setLimits(List<PrepaidCardLimit10> limits) {
    this.limits = limits;
  }

}
