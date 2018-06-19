package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.util.Objects;

public class SimulationWithdrawal10 extends BaseModel {

  private NewAmountAndCurrency10 fee;
  private NewAmountAndCurrency10 amountToDiscount;

  public SimulationWithdrawal10(){
    super();
  }

  public SimulationWithdrawal10(NewAmountAndCurrency10 fee, NewAmountAndCurrency10 amountToDiscount) {
    this.fee = fee;
    this.amountToDiscount = amountToDiscount;
  }

  public NewAmountAndCurrency10 getFee() {
    return fee;
  }

  public void setFee(NewAmountAndCurrency10 fee) {
    this.fee = fee;
  }

  public NewAmountAndCurrency10 getAmountToDiscount() {
    return amountToDiscount;
  }

  public void setAmountToDiscount(NewAmountAndCurrency10 amountToDiscount) {
    this.amountToDiscount = amountToDiscount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimulationWithdrawal10)) return false;
    SimulationWithdrawal10 that = (SimulationWithdrawal10) o;
    return  Objects.equals(getFee(), that.getFee()) &&
      Objects.equals(getAmountToDiscount(), that.getAmountToDiscount());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFee(), getAmountToDiscount());
  }
}
