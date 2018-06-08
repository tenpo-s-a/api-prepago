package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class CalculatorRequest10 extends BaseModel {

  private NewAmountAndCurrency10 amount;
  private Integer rut;
  private TransactionOriginType paymentMethod;

  public CalculatorRequest10(){
    super();
  }

  public CalculatorRequest10(NewAmountAndCurrency10 amount, Integer rut, TransactionOriginType paymentMethod) {
    this.amount = amount;
    this.rut = rut;
    this.paymentMethod = paymentMethod;
  }

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
    this.amount = amount;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public TransactionOriginType getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(TransactionOriginType paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  @JsonIgnore
  public boolean isTransactionWeb() {
    return TransactionOriginType.WEB.equals(this.getPaymentMethod());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CalculatorRequest10 that = (CalculatorRequest10) o;
    return Objects.equals(amount, that.amount) &&
      Objects.equals(rut, that.rut) &&
      paymentMethod == that.paymentMethod;
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, rut, paymentMethod);
  }
}
