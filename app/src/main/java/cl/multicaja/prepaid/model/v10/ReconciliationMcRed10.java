package cl.multicaja.prepaid.model.v10;

import java.math.BigDecimal;

public class ReconciliationMcRed10 {


  private String mcCode;

  private String dateTrx;

  private BigDecimal amount;

  private Long clientId;

  private Long externalId;

  public String getMcCode() {
    return mcCode;
  }

  public void setMcCode(String mcCode) {
    this.mcCode = mcCode;
  }

  public String getDateTrx() {
    return dateTrx;
  }

  public void setDateTrx(String dateTrx) {
    this.dateTrx = dateTrx;
  }

  public Long getClientId() {
    return clientId;
  }

  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public Long getExternalId() {
    return externalId;
  }

  public void setExternalId(Long externalId) {
    this.externalId = externalId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }
}
