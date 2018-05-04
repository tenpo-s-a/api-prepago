package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class PrepaidCard {

  private Integer id;
  private String processorUserId;
  private String pan;
  private String expiration;
  private String nameOnCard;
  private String status;
  private Timestamps timestamps;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getProcessorUserId() {
    return processorUserId;
  }

  public void setProcessorUserId(String processorUserId) {
    this.processorUserId = processorUserId;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getExpiration() {
    return expiration;
  }

  public void setExpiration(String expiration) {
    this.expiration = expiration;
  }

  public String getNameOnCard() {
    return nameOnCard;
  }

  public void setNameOnCard(String nameOnCard) {
    this.nameOnCard = nameOnCard;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
