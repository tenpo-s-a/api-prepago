package cl.multicaja.prepaid.async.v10.model;

import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public class PrepaidReverseData10 implements Serializable {

  private PrepaidTopup10 prepaidTopup10;
  private PrepaidWithdraw10 prepaidWithdraw10;
  @Deprecated
  private User user;

  private PrepaidUser10 prepaidUser10;
  private PrepaidCard10 prepaidCard10;

  private PrepaidMovement10 prepaidMovementReverse;

  private Errors numError;
  private String msjError;

  public PrepaidReverseData10() {
    super();
  }

  public PrepaidReverseData10(PrepaidTopup10 prepaidTopup, PrepaidCard10 prepaidCard10,User user,PrepaidUser10 prepaidUser10, PrepaidMovement10 prepaidMovementReverse) {
    this.prepaidTopup10 = prepaidTopup;
    this.user = user;
    this.prepaidUser10 = prepaidUser10;
    this.prepaidCard10 = prepaidCard10;
    this.prepaidMovementReverse = prepaidMovementReverse;
  }

  public PrepaidReverseData10(PrepaidWithdraw10 prepaidWithdraw, PrepaidUser10 user, PrepaidMovement10 prepaidMovementReverse) {
    this.prepaidWithdraw10 = prepaidWithdraw;
    this.prepaidUser10 = user;
    this.prepaidMovementReverse = prepaidMovementReverse;
  }


  public PrepaidTopup10 getPrepaidTopup10() {
    return prepaidTopup10;
  }

  public void setPrepaidTopup10(PrepaidTopup10 prepaidTopup10) {
    this.prepaidTopup10 = prepaidTopup10;
  }

  public PrepaidWithdraw10 getPrepaidWithdraw10() {
    return prepaidWithdraw10;
  }

  public void setPrepaidWithdraw10(PrepaidWithdraw10 prepaidWithdraw10) {
    this.prepaidWithdraw10 = prepaidWithdraw10;
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

  public PrepaidMovement10 getPrepaidMovementReverse() {
    return prepaidMovementReverse;
  }

  public void setPrepaidMovementReverse(PrepaidMovement10 prepaidMovementReverse) {
    this.prepaidMovementReverse = prepaidMovementReverse;
  }

  public Errors getNumError() {
    return numError;
  }

  public void setNumError(Errors numError) {
    this.numError = numError;
  }

  public String getMsjError() {
    return msjError;
  }

  public void setMsjError(String msjError) {
    this.msjError = msjError;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
