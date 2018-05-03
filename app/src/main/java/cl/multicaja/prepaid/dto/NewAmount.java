package cl.multicaja.prepaid.dto;

import java.math.BigDecimal;

/**
 * @author abarazarte
 */
public class NewAmount {

  private Integer currencyCode;
  private BigDecimal amount;

  public Integer getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(Integer currencyCode) {
    this.currencyCode = currencyCode;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

}
