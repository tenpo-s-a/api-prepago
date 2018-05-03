package cl.multicaja.prepaid.dto;

import java.math.BigDecimal;

/**
 * @author abarazarte
 */
public class NewAmount {

  private Integer currency_code;
  private BigDecimal amount;

  public Integer getCurrency_code() {
    return currency_code;
  }

  public void setCurrency_code(Integer currency_code) {
    this.currency_code = currency_code;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

}
