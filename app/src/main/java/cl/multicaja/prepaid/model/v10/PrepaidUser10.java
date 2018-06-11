package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.users.model.v10.Timestamps;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author abarazarte
 */
public class PrepaidUser10 extends BaseModel {

  private Long id;
  private Long idUserMc;
  private Integer rut;
  private PrepaidUserStatus status;
  private BigDecimal balance;
  private Long balanceExpiration;
  private Timestamps timestamps;

  public PrepaidUser10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdUserMc() {
    return idUserMc;
  }

  public void setIdUserMc(Long idUserMc) {
    this.idUserMc = idUserMc;
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

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public Long getBalanceExpiration() {
    return balanceExpiration;
  }

  public void setBalanceExpiration(Long balanceExpiration) {
    this.balanceExpiration = balanceExpiration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidUser10)) return false;
    PrepaidUser10 that = (PrepaidUser10) o;
    return Objects.equals(getId(), that.getId()) &&
      Objects.equals(getIdUserMc(), that.getIdUserMc()) &&
      Objects.equals(getRut(), that.getRut()) &&
      getStatus() == that.getStatus() &&
      Objects.equals(getBalance(), that.getBalance()) &&
      Objects.equals(getBalanceExpiration(), that.getBalanceExpiration()) &&
      Objects.equals(getTimestamps(), that.getTimestamps());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getIdUserMc(), getRut(), getStatus(), getBalance(), getBalanceExpiration(), getTimestamps());
  }
}
