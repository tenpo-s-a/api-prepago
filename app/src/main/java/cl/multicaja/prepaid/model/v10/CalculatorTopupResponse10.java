package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.util.Objects;

public class CalculatorTopupResponse10 extends BaseModel {

  private BigDecimal fee;
  private NewAmountAndCurrency10 pca;
  private NewAmountAndCurrency10 eed;
  private NewAmountAndCurrency10 amountToPay;

  public CalculatorTopupResponse10(){
    super();
  }

  public CalculatorTopupResponse10(BigDecimal fee, NewAmountAndCurrency10 pca, NewAmountAndCurrency10 eed, NewAmountAndCurrency10 amountToPay) {
    this.fee = fee;
    this.pca = pca;
    this.eed = eed;
    this.amountToPay = amountToPay;
  }

  public BigDecimal getFee() {
    return fee;
  }

  public void setFee(BigDecimal fee) {
    this.fee = fee;
  }

  public NewAmountAndCurrency10 getPca() {
    return pca;
  }

  public void setPca(NewAmountAndCurrency10 pca) {
    this.pca = pca;
  }

  public NewAmountAndCurrency10 getEed() {
    return eed;
  }

  public void setEed(NewAmountAndCurrency10 eed) {
    this.eed = eed;
  }

  public NewAmountAndCurrency10 getAmountToPay() {
    return amountToPay;
  }

  public void setAmountToPay(NewAmountAndCurrency10 amountToPay) {
    this.amountToPay = amountToPay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CalculatorTopupResponse10)) return false;
    CalculatorTopupResponse10 that = (CalculatorTopupResponse10) o;
    return Objects.equals(getFee(), that.getFee()) &&
      Objects.equals(getPca(), that.getPca()) &&
      Objects.equals(getEed(), that.getEed()) &&
      Objects.equals(getAmountToPay(), that.getAmountToPay());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFee(), getPca(), getEed(), getAmountToPay());
  }
}
