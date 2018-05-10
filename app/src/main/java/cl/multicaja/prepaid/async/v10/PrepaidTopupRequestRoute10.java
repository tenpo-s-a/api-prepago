package cl.multicaja.prepaid.async.v10;

import cl.multicaja.prepaid.domain.PrepaidTopup;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public class PrepaidTopupRequestRoute10 implements Serializable {

  private PrepaidTopup prepaidTopup;
  private User user;

  public PrepaidTopupRequestRoute10() {
    super();
  }

  public PrepaidTopupRequestRoute10(PrepaidTopup prepaidTopup, User user) {
    this.prepaidTopup = prepaidTopup;
    this.user = user;
  }

  public PrepaidTopup getPrepaidTopup() {
    return prepaidTopup;
  }

  public void setPrepaidTopup(PrepaidTopup prepaidTopup) {
    this.prepaidTopup = prepaidTopup;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
