package cl.multicaja.prepaid.model.v10;

import java.math.BigDecimal;

public class CalculatorResponse10 {


  private BigDecimal comision;
  private BigDecimal pca;
  private BigDecimal eed;
  private BigDecimal aPagar;

  public CalculatorResponse10(){
  }

  public CalculatorResponse10(BigDecimal comision, BigDecimal pca, BigDecimal eed, BigDecimal aPagar) {
    this.comision = comision;
    this.pca = pca;
    this.eed = eed;
    this.aPagar = aPagar;
  }

  public BigDecimal getComision() {
    return comision;
  }

  public void setComision(BigDecimal comision) {
    this.comision = comision;
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

  public BigDecimal getaPagar() {
    return aPagar;
  }

  public void setaPagar(BigDecimal aPagar) {
    this.aPagar = aPagar;
  }
}
