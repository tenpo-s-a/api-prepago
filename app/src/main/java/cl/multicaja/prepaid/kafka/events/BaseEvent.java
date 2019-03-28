package cl.multicaja.prepaid.kafka.events;

public class BaseEvent {
  private String userId;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
