package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class SimulationNew10 extends BaseModel {

  private NewAmountAndCurrency10 amount;

  private TransactionOriginType method;

  public SimulationNew10(){
    super();
  }

  public SimulationNew10(NewAmountAndCurrency10 amount, TransactionOriginType method) {
    this.amount = amount;
    this.method = method;
  }

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
    this.amount = amount;
  }

  public TransactionOriginType getPaymentMethod() {
    return method;
  }

  public void setPaymentMethod(TransactionOriginType paymentMethod) {
    this.method = paymentMethod;
  }

  @JsonIgnore
  public boolean isTransactionWeb() {
    return TransactionOriginType.WEB.equals(this.getPaymentMethod());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimulationNew10 that = (SimulationNew10) o;
    return Objects.equals(amount, that.amount) &&
      method == that.method;
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, method);
  }
}
