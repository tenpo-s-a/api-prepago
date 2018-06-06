package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.util.Objects;

public class CalculatorTopupResponse10 extends BaseModel {

  private Double comission;
  private Double pca;
  private Double eed;
  private Double toPay;

  public CalculatorTopupResponse10(){
    super();
  }

  public CalculatorTopupResponse10(Double comission, Double pca, Double eed, Double toPay) {
    this.comission = comission;
    this.pca = pca;
    this.eed = eed;
    this.toPay = toPay;
  }

  public Double getComission() {
    return comission;
  }

  public void setComission(Double comission) {
    this.comission = comission;
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

  public Double getToPay() {
    return toPay;
  }

  public void setToPay(Double toPay) {
    this.toPay = toPay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CalculatorTopupResponse10)) return false;
    CalculatorTopupResponse10 that = (CalculatorTopupResponse10) o;
    return Objects.equals(getComission(), that.getComission()) &&
      Objects.equals(getPca(), that.getPca()) &&
      Objects.equals(getEed(), that.getEed()) &&
      Objects.equals(getToPay(), that.getToPay());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getComission(), getPca(), getEed(), getToPay());
  }
}
