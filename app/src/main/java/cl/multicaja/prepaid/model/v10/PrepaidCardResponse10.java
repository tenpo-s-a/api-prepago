package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.kafka.events.model.BaseModel;

/**
 * Se utiliza para responder
 */
public class PrepaidCardResponse10 extends BaseModel {
  String pan;
  String nameOnCard;

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getNameOnCard() {
    return nameOnCard;
  }

  public void setNameOnCard(String nameOnCard) {
    this.nameOnCard = nameOnCard;
  }
}
