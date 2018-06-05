package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Calculator10 extends BaseModel implements Serializable {

  private BigDecimal amount;
  private Long userId;
  private Integer userRun;
  private TopupType paymentMethod;

  public Calculator10(BigDecimal amount, Long userId, Integer userRun, TopupType paymentMethod) {
    this.amount = amount;
    this.userId = userId;
    this.userRun = userRun;
    this.paymentMethod = paymentMethod;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Integer getUserRun() {
    return userRun;
  }

  public void setUserRun(Integer userRun) {
    this.userRun = userRun;
  }

  public TopupType getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(TopupType paymentMethod) {
    this.paymentMethod = paymentMethod;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Calculator10 that = (Calculator10) o;
    return Objects.equals(amount, that.amount) &&
      Objects.equals(userId, that.userId) &&
      Objects.equals(userRun, that.userRun) &&
      paymentMethod == that.paymentMethod;
  }

  @Override
  public int hashCode() {

    return Objects.hash(amount, userId, userRun, paymentMethod);
  }
}
