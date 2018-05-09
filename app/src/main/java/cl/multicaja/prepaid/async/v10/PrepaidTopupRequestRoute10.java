package cl.multicaja.prepaid.async.v10;

import cl.multicaja.prepaid.domain.NewPrepaidTopup;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public class PrepaidTopupRequestRoute10 implements Serializable {

  private NewPrepaidTopup newPrepaidTopup;
  private User user;

  public PrepaidTopupRequestRoute10() {
    super();
  }

  public PrepaidTopupRequestRoute10(NewPrepaidTopup newPrepaidTopup, User user) {
    this.newPrepaidTopup = newPrepaidTopup;
    this.user = user;
  }

  public NewPrepaidTopup getNewPrepaidTopup() {
    return newPrepaidTopup;
  }

  public void setNewPrepaidTopup(NewPrepaidTopup newPrepaidTopup) {
    this.newPrepaidTopup = newPrepaidTopup;
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
