package cl.multicaja.prepaid.kafka.events.model;

public class BaseModel {

  private static final long serialVersionUID = 5919942623922780536L;

  private String id;
  private String status;
  private Timestamps timestamps;

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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
