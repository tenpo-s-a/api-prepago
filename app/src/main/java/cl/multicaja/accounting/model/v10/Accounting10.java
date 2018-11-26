package cl.multicaja.accounting.model.v10;

import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Accounting10 implements Serializable {


 private Long idTransaction;
 private AccountingTxType type;
 private AccountingOriginType origin;
 private NewAmountAndCurrency10 amount;
 private NewAmountAndCurrency10 amountUsd;
 private BigDecimal exchangeRateDif;
 private BigDecimal fee;
 private BigDecimal feeIva;
 private Timestamp transactionDate;

  public Accounting10() {
  }

  public Accounting10(Long idTransaction, AccountingTxType type, AccountingOriginType origin, NewAmountAndCurrency10 amount, NewAmountAndCurrency10 amountUsd, BigDecimal exchangeRateDif, BigDecimal fee, BigDecimal feeIva, Timestamp transactionDate) {
    this.idTransaction = idTransaction;
    this.type = type;
    this.origin = origin;
    this.amount = amount;
    this.amountUsd = amountUsd;
    this.exchangeRateDif = exchangeRateDif;
    this.fee = fee;
    this.feeIva = feeIva;
    this.transactionDate = transactionDate;
  }

  public Long getIdTransaction() {
    return idTransaction;
  }

  public void setIdTransaction(Long idTransaction) {
    this.idTransaction = idTransaction;
  }

  public AccountingTxType getType() {
    return type;
  }

  public void setType(AccountingTxType type) {
    this.type = type;
  }

  public AccountingOriginType getOrigin() {
    return origin;
  }

  public void setOrigin(AccountingOriginType origin) {
    this.origin = origin;
  }

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
    this.amount = amount;
  }

  public NewAmountAndCurrency10 getAmountUsd() {
    return amountUsd;
  }

  public void setAmountUsd(NewAmountAndCurrency10 amountUsd) {
    this.amountUsd = amountUsd;
  }

  public Timestamp getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(Timestamp transactionDate) {
    this.transactionDate = transactionDate;
  }

  public BigDecimal getExchangeRateDif() {
    return exchangeRateDif;
  }

  public void setExchangeRateDif(BigDecimal exchangeRateDif) {
    this.exchangeRateDif = exchangeRateDif;
  }

  public BigDecimal getFee() {
    return fee;
  }

  public void setFee(BigDecimal fee) {
    this.fee = fee;
  }

  public BigDecimal getFeeIva() {
    return feeIva;
  }

  public void setFeeIva(BigDecimal feeIva) {
    this.feeIva = feeIva;
  }

}
