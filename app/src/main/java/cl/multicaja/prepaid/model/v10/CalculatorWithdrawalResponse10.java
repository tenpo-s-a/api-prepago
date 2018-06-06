package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.util.Objects;

public class CalculatorWithdrawalResponse10 extends BaseModel {

  private NewAmountAndCurrency10 amount;
  private Double comission;
  private NewAmountAndCurrency10 amountToDiscount;

  public CalculatorWithdrawalResponse10(){
    super();
  }

  public CalculatorWithdrawalResponse10(NewAmountAndCurrency10 amount, Double comission, NewAmountAndCurrency10 amountToDiscount) {
    this.amount = amount;
    this.comission = comission;
    this.amountToDiscount = amountToDiscount;
  }

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
    this.amount = amount;
  }

  public Double getComission() {
    return comission;
  }

  public void setComission(Double comission) {
    this.comission = comission;
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
    return Objects.equals(getAmount(), that.getAmount()) &&
      Objects.equals(getComission(), that.getComission()) &&
      Objects.equals(getAmountToDiscount(), that.getAmountToDiscount());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAmount(), getComission(), getAmountToDiscount());
  }
}
