package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
import java.math.BigDecimal;

public class Amount{
  private Integer currency_code;
  private BigDecimal amount;
  private String currency_description;

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

  public String getCurrencyDescription() {
    return currency_description;
  }

  public void setCurrencyDescription(String currency_description) {
    this.currency_description = currency_description;
  }
}
