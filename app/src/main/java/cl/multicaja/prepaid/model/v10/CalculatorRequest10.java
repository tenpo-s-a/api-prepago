package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.io.Serializable;
import java.util.Objects;

public class CalculatorRequest10 extends BaseModel implements Serializable {

  private NewAmountAndCurrency10 amount;
  //private Long userId;
  private Integer userRut;
  private TopupType paymentMethod;

  public CalculatorRequest10(){
  }
  public CalculatorRequest10(NewAmountAndCurrency10 amount,/* Long userId,*/ Integer userRut, TopupType paymentMethod) {
    this.amount = amount;
    //this.userId = userId;
    this.userRut = userRut;
    this.paymentMethod = paymentMethod;
  }

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
    this.amount = amount;
  }

  /*public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }*/

  public Integer getUserRut() {
    return userRut;
  }

  public void setUserRut(Integer userRut) {
    this.userRut = userRut;
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
    CalculatorRequest10 that = (CalculatorRequest10) o;
    return Objects.equals(amount, that.amount) &&
     // Objects.equals(userId, that.userId) &&
      Objects.equals(userRut, that.userRut) &&
      paymentMethod == that.paymentMethod;
  }

  @Override
  public int hashCode() {

    return Objects.hash(amount,/* userId,*/ userRut, paymentMethod);
  }
}
