package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
import java.math.BigDecimal;

public class Amount{
  private Integer currencyCode;
  private BigDecimal amount;
  private String currencyDescription;

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

  public String getCurrencyDescription() {
    return currencyDescription;
  }

  public void setCurrencyDescription(String currencyDescription) {
    this.currencyDescription = currencyDescription;
  }
}
