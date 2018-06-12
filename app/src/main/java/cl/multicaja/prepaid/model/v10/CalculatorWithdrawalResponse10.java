package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.util.Objects;

public class CalculatorWithdrawalResponse10 extends BaseModel {

  private BigDecimal fee;
  private NewAmountAndCurrency10 amountToDiscount;

  public CalculatorWithdrawalResponse10(){
    super();
  }

  public CalculatorWithdrawalResponse10(BigDecimal fee, NewAmountAndCurrency10 amountToDiscount) {
    this.fee = fee;
    this.amountToDiscount = amountToDiscount;
  }

  public BigDecimal getFee() {
    return fee;
  }

  public void setFee(BigDecimal fee) {
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
    if (!(o instanceof CalculatorWithdrawalResponse10)) return false;
    CalculatorWithdrawalResponse10 that = (CalculatorWithdrawalResponse10) o;
    return  Objects.equals(getFee(), that.getFee()) &&
      Objects.equals(getAmountToDiscount(), that.getAmountToDiscount());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFee(), getAmountToDiscount());
  }
}
