package cl.multicaja.accounting.model.v10;

import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class AccountingData10 implements Serializable {

 private Long id;
 private Long idTransaction;
 private AccountingTxType type;
 private AccountingMovementType accountingMovementType;
 private AccountingOriginType origin;
 private NewAmountAndCurrency10 amount;
 private NewAmountAndCurrency10 amountUsd;
 private NewAmountAndCurrency10 amountMastercard;
 private BigDecimal exchangeRateDif;
 private BigDecimal fee;
 private BigDecimal feeIva;
 private BigDecimal collectorFee;
 private BigDecimal collectorFeeIva;
 private NewAmountAndCurrency10 amountBalance;
 private Long fileId;
 private Timestamp transactionDate;
 private Timestamp conciliationDate;
 private AccountingStatusType status;
 private Timestamps timestamps;

  public AccountingData10() {
  }

  public AccountingData10(Long id, Long idTransaction, AccountingTxType type, AccountingOriginType origin, NewAmountAndCurrency10 amount, NewAmountAndCurrency10 amountUsd, BigDecimal exchangeRateDif, BigDecimal fee, BigDecimal feeIva, Timestamp transactionDate) {
    this.id = id;
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getTransactionDateInFormat() {
    return DateUtils.getInstance().dateToStringFormat(new Date(transactionDate.getTime()),"yyyy-MM-dd hh24:mm:ss");
  }
  public String getConciliationDateInFormat() {
    return DateUtils.getInstance().dateToStringFormat(new Date(conciliationDate.getTime()),"yyyy-MM-dd hh24:mm:ss");
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

  public AccountingMovementType getAccountingMovementType() {
    return accountingMovementType;
  }

  public void setAccountingMovementType(AccountingMovementType accountingMovementType) {
    this.accountingMovementType = accountingMovementType;
  }

  public NewAmountAndCurrency10 getAmountMastercard() {
    return amountMastercard;
  }

  public void setAmountMastercard(NewAmountAndCurrency10 amountMastercard) {
    this.amountMastercard = amountMastercard;
  }

  public BigDecimal getCollectorFee() {
    return collectorFee;
  }

  public void setCollectorFee(BigDecimal collectorFee) {
    this.collectorFee = collectorFee;
  }

  public BigDecimal getCollectorFeeIva() {
    return collectorFeeIva;
  }

  public void setCollectorFeeIva(BigDecimal collectorFeeIva) {
    this.collectorFeeIva = collectorFeeIva;
  }

  public NewAmountAndCurrency10 getAmountBalance() {
    return amountBalance;
  }

  public void setAmountBalance(NewAmountAndCurrency10 amountBalance) {
    this.amountBalance = amountBalance;
  }

  public Long getFileId() {
    return fileId;
  }

  public void setFileId(Long fileId) {
    this.fileId = fileId;
  }

  public Timestamp getConciliationDate() {
    return conciliationDate;
  }

  public void setConciliationDate(Timestamp conciliationDate) {
    this.conciliationDate = conciliationDate;
  }

  public AccountingStatusType getStatus() {
    return status;
  }

  public void setStatus(AccountingStatusType status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
