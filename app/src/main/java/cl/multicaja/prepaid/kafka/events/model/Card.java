package cl.multicaja.prepaid.kafka.events.model;

public class Card extends BaseModel {

  private String pan;

  public Card() {
    super();
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }
}
