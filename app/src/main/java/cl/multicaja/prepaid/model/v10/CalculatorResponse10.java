package cl.multicaja.prepaid.model.v10;

import java.math.BigDecimal;

public class CalculatorResponse10 {

  private BigDecimal amount;
  private BigDecimal charges;
  private BigDecimal totalInusd;
  private BigDecimal total;
  public CalculatorResponse10(){
  }

  public CalculatorResponse10(BigDecimal amount, BigDecimal charges, BigDecimal totalInusd, BigDecimal total) {
    this.amount = amount;
    this.charges = charges;
    this.totalInusd = totalInusd;
    this.total = total;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getCharges() {
    return charges;
  }

  public void setCharges(BigDecimal charges) {
    this.charges = charges;
  }

  public BigDecimal getTotalInusd() {
    return totalInusd;
  }

  public void setTotalInusd(BigDecimal totalInusd) {
    this.totalInusd = totalInusd;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }
}
