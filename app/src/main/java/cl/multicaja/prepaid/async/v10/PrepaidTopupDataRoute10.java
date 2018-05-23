package cl.multicaja.prepaid.async.v10;

import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public class PrepaidTopupDataRoute10 implements Serializable {

  private PrepaidTopup10 prepaidTopup;
  private User user;
  private PrepaidUser10 prepaidUser10;
  private PrepaidCard10 prepaidCard10;

  public PrepaidTopupDataRoute10() {
    super();
  }

  public PrepaidTopupDataRoute10(PrepaidTopup10 prepaidTopup, User user) {
    this.prepaidTopup = prepaidTopup;
    this.user = user;
  }

  public PrepaidTopup10 getPrepaidTopup() {
    return prepaidTopup;
  }

  public void setPrepaidTopup(PrepaidTopup10 prepaidTopup) {
    this.prepaidTopup = prepaidTopup;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public PrepaidUser10 getPrepaidUser10() {
    return prepaidUser10;
  }

  public void setPrepaidUser10(PrepaidUser10 prepaidUser10) {
    this.prepaidUser10 = prepaidUser10;
  }

  public PrepaidCard10 getPrepaidCard10() {
    return prepaidCard10;
  }

  public void setPrepaidCard10(PrepaidCard10 prepaidCard10) {
    this.prepaidCard10 = prepaidCard10;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
