package cl.multicaja.prepaid.model.v10;

import java.math.BigDecimal;

public class Percentage10 {

  private BigDecimal TOPUP_POS_FEE_PERCENTAGE;
  private BigDecimal TOPUP_WEB_FEE_PERCENTAGE;
  private BigDecimal TOPUP_WEB_FEE_AMOUNT;
  private BigDecimal WITHDRAW_POS_FEE_PERCENTAGE;
  private BigDecimal WITHDRAW_WEB_FEE_PERCENTAGE;
  private BigDecimal WITHDRAW_WEB_FEE_AMOUNT;
  private BigDecimal CALCULATOR_TOPUP_WEB_FEE_AMOUNT;
  private BigDecimal CALCULATOR_TOPUP_POS_FEE_PERCENTAGE;
  private BigDecimal CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT;
  private BigDecimal CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE;
  private BigDecimal OPENING_FEE;
  private double IVA;
  private int MAX_AMOUNT_BY_USER;

  public BigDecimal getTOPUP_POS_FEE_PERCENTAGE() {
    return TOPUP_POS_FEE_PERCENTAGE;
  }

  public void setTOPUP_POS_FEE_PERCENTAGE(BigDecimal TOPUP_POS_FEE_PERCENTAGE) {
    this.TOPUP_POS_FEE_PERCENTAGE = TOPUP_POS_FEE_PERCENTAGE;
  }

  public BigDecimal getTOPUP_WEB_FEE_PERCENTAGE() {
    return TOPUP_WEB_FEE_PERCENTAGE;
  }

  public void setTOPUP_WEB_FEE_PERCENTAGE(BigDecimal TOPUP_WEB_FEE_PERCENTAGE) {
    this.TOPUP_WEB_FEE_PERCENTAGE = TOPUP_WEB_FEE_PERCENTAGE;
  }

  public BigDecimal getTOPUP_WEB_FEE_AMOUNT() {
    return TOPUP_WEB_FEE_AMOUNT;
  }

  public void setTOPUP_WEB_FEE_AMOUNT(BigDecimal TOPUP_WEB_FEE_AMOUNT) {
    this.TOPUP_WEB_FEE_AMOUNT = TOPUP_WEB_FEE_AMOUNT;
  }

  public BigDecimal getWITHDRAW_POS_FEE_PERCENTAGE() {
    return WITHDRAW_POS_FEE_PERCENTAGE;
  }

  public void setWITHDRAW_POS_FEE_PERCENTAGE(BigDecimal WITHDRAW_POS_FEE_PERCENTAGE) {
    this.WITHDRAW_POS_FEE_PERCENTAGE = WITHDRAW_POS_FEE_PERCENTAGE;
  }

  public BigDecimal getWITHDRAW_WEB_FEE_PERCENTAGE() {
    return WITHDRAW_WEB_FEE_PERCENTAGE;
  }

  public void setWITHDRAW_WEB_FEE_PERCENTAGE(BigDecimal WITHDRAW_WEB_FEE_PERCENTAGE) {
    this.WITHDRAW_WEB_FEE_PERCENTAGE = WITHDRAW_WEB_FEE_PERCENTAGE;
  }

  public BigDecimal getWITHDRAW_WEB_FEE_AMOUNT() {
    return WITHDRAW_WEB_FEE_AMOUNT;
  }

  public void setWITHDRAW_WEB_FEE_AMOUNT(BigDecimal WITHDRAW_WEB_FEE_AMOUNT) {
    this.WITHDRAW_WEB_FEE_AMOUNT = WITHDRAW_WEB_FEE_AMOUNT;
  }

  public BigDecimal getCALCULATOR_TOPUP_WEB_FEE_AMOUNT() {
    return CALCULATOR_TOPUP_WEB_FEE_AMOUNT;
  }

  public void setCALCULATOR_TOPUP_WEB_FEE_AMOUNT(BigDecimal CALCULATOR_TOPUP_WEB_FEE_AMOUNT) {
    this.CALCULATOR_TOPUP_WEB_FEE_AMOUNT = CALCULATOR_TOPUP_WEB_FEE_AMOUNT;
  }

  public BigDecimal getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE() {
    return CALCULATOR_TOPUP_POS_FEE_PERCENTAGE;
  }

  public void setCALCULATOR_TOPUP_POS_FEE_PERCENTAGE(BigDecimal CALCULATOR_TOPUP_POS_FEE_PERCENTAGE) {
    this.CALCULATOR_TOPUP_POS_FEE_PERCENTAGE = CALCULATOR_TOPUP_POS_FEE_PERCENTAGE;
  }

  public BigDecimal getCALCULATOR_WITHDRAW_WEB_FEE_AMOUNT() {
    return CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT;
  }

  public void setCALCULATOR_WITHDRAW_WEB_FEE_AMOUNT(BigDecimal CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT) {
    this.CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT = CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT;
  }

  public BigDecimal getCALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE() {
    return CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE;
  }

  public void setCALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE(BigDecimal CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE) {
    this.CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE = CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE;
  }

  public BigDecimal getOPENING_FEE() {
    return OPENING_FEE;
  }

  public void setOPENING_FEE(BigDecimal OPENING_FEE) {
    this.OPENING_FEE = OPENING_FEE;
  }

  public double getIVA() {
    return IVA;
  }

  public void setIVA(double IVA) {
    this.IVA = IVA;
  }

  public int getMAX_AMOUNT_BY_USER() {
    return MAX_AMOUNT_BY_USER;
  }

  public void setMAX_AMOUNT_BY_USER(int MAX_AMOUNT_BY_USER) {
    this.MAX_AMOUNT_BY_USER = MAX_AMOUNT_BY_USER;
  }
}
