package cl.multicaja.prepaid.model.v10;

import java.math.BigDecimal;

public class PrepaidMovementFee10 {
  private Long id;
  private Long movementId; // Relacion a la tabla prp_movimiento
  private String feeType;
  private BigDecimal amount;
  private BigDecimal iva;
  private Timestamps timestamps;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getMovementId() {
    return movementId;
  }

  public void setMovementId(Long movementId) {
    this.movementId = movementId;
  }

  public String getFeeType() {
    return feeType;
  }

  public void setFeeType(String feeType) {
    this.feeType = feeType;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getIva() {
    return iva;
  }

  public void setIva(BigDecimal iva) {
    this.iva = iva;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
