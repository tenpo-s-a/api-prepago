package cl.multicaja.prepaid.kafka.events;

import cl.multicaja.prepaid.kafka.events.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class UserEvent extends BaseEvent {

  //@JsonProperty("eventId")
  //private String eventId;
  private User user;

  public UserEvent() {
    super();
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

}
