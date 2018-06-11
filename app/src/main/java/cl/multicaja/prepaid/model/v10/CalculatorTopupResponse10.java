package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.util.Objects;

public class CalculatorTopupResponse10 extends BaseModel {

  private BigDecimal fee;
  private Double pca;
  private Double eed;
  private NewAmountAndCurrency10 amountToPay;

  public CalculatorTopupResponse10(){
    super();
  }

  public CalculatorTopupResponse10(BigDecimal fee, Double pca, Double eed, NewAmountAndCurrency10 amountToPay) {
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

  public Double getPca() {
    return pca;
  }

  public void setPca(Double pca) {
    this.pca = pca;
  }

  public Double getEed() {
    return eed;
  }

  public void setEed(Double eed) {
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
