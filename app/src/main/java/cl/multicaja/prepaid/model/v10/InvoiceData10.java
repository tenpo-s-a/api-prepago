package cl.multicaja.prepaid.model.v10;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceData10 {

  // Datos Base
  private Long movementId;
  private PrepaidMovementType type;

  // Datos Header
  private LocalDateTime fechaTrx;
  private Integer rut;
  private String dv;
  private BigDecimal amount;
  private BigDecimal amountPaid;
  private String clientName;
  // Datos Detail

  private BigDecimal amountOne;
  private BigDecimal amountTwo;
  private String comment;

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public Long getMovementId() {
    return movementId;
  }

  public void setMovementId(Long movementId) {
    this.movementId = movementId;
  }

  public PrepaidMovementType getType() {
    return type;
  }

  public void setType(PrepaidMovementType type) {
    this.type = type;
  }

  public LocalDateTime getFechaTrx() {
    return fechaTrx;
  }

  public void setFechaTrx(LocalDateTime fechaTrx) {
    this.fechaTrx = fechaTrx;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public String getDv() {
    return dv;
  }

  public void setDv(String dv) {
    this.dv = dv;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getAmountPaid() {
    return amountPaid;
  }

  public void setAmountPaid(BigDecimal amountPaid) {
    this.amountPaid = amountPaid;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public BigDecimal getAmountOne() {
    return amountOne;
  }

  public void setAmountOne(BigDecimal amountOne) {
    this.amountOne = amountOne;
  }

  public BigDecimal getAmountTwo() {
    return amountTwo;
  }

  public void setAmountTwo(BigDecimal amountTwo) {
    this.amountTwo = amountTwo;
  }
}
