package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.util.Objects;

public class CalculatorTopupResponse10 extends BaseModel {

  private BigDecimal comission;
  private BigDecimal pca;
  private BigDecimal eed;
  private NewAmountAndCurrency10 amountToPay;

  public CalculatorTopupResponse10(){
    super();
  }

  public CalculatorTopupResponse10(BigDecimal comission, BigDecimal pca, BigDecimal eed, NewAmountAndCurrency10 amountToPay) {
    this.comission = comission;
    this.pca = pca;
    this.eed = eed;
    this.amountToPay = amountToPay;
  }

  public BigDecimal getComission() {
    return comission;
  }

  public void setComission(BigDecimal comission) {
    this.comission = comission;
  }

  public BigDecimal getPca() {
    return pca;
  }

  public void setPca(BigDecimal pca) {
    this.pca = pca;
  }

  public BigDecimal getEed() {
    return eed;
  }

  public void setEed(BigDecimal eed) {
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
    return Objects.equals(getComission(), that.getComission()) &&
      Objects.equals(getPca(), that.getPca()) &&
      Objects.equals(getEed(), that.getEed()) &&
      Objects.equals(getAmountToPay(), that.getAmountToPay());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getComission(), getPca(), getEed(), getAmountToPay());
  }
}
