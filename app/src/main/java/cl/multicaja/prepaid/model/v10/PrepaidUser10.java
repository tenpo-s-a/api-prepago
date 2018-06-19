package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.users.model.v10.Timestamps;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * @author abarazarte
 */
public class PrepaidUser10 extends BaseModel {

  private Long id;
  private Long userId;
  private Integer rut;
  private PrepaidUserStatus status;
  private Timestamps timestamps;
  private PrepaidUserLevel userLevel;

  @JsonIgnore
  private PrepaidBalanceInfo10 balance;

  @JsonIgnore
  private Long balanceExpiration;

  public PrepaidUser10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public PrepaidUserStatus getStatus() {
    return status;
  }

  public void setStatus(PrepaidUserStatus status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  public PrepaidBalanceInfo10 getBalance() {
    return balance;
  }

  public void setBalance(PrepaidBalanceInfo10 balance) {
    this.balance = balance;
  }

  public Long getBalanceExpiration() {
    return balanceExpiration;
  }

  public void setBalanceExpiration(Long balanceExpiration) {
    this.balanceExpiration = balanceExpiration;
  }

  public PrepaidUserLevel getUserLevel() {
    return userLevel;
  }

  public void setUserLevel(PrepaidUserLevel userLevel) {
    this.userLevel = userLevel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidUser10)) return false;
    PrepaidUser10 that = (PrepaidUser10) o;
    return Objects.equals(getId(), that.getId()) &&
      Objects.equals(getUserId(), that.getUserId()) &&
      Objects.equals(getRut(), that.getRut()) &&
      getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getUserId(), getRut(), getStatus(), getTimestamps());
  }
}
