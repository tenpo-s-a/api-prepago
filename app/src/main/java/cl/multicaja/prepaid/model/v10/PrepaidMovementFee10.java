package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;

import java.math.BigDecimal;

public class PrepaidMovementFee10 extends BaseModel {
  private Long id;
  private Long movementId; // Relacion a la tabla prp_movimiento
  private PrepaidMovementFeeType feeType;
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

  public PrepaidMovementFeeType getFeeType() {
    return feeType;
  }

  public void setFeeType(PrepaidMovementFeeType feeType) {
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
