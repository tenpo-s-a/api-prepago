package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IpmMovement10 extends BaseModel {
  private Long id;
  private Long fileId;
  private Integer messageType;
  private Integer functionCode;
  private Integer messageReason;
  private Integer messageNumber;
  private String pan;
  private BigDecimal transactionAmount;
  private BigDecimal reconciliationAmount;
  private BigDecimal cardholderBillingAmount;
  private BigDecimal reconciliationConversionRate;
  private BigDecimal cardholderBillingConversionRate;
  private LocalDateTime transactionLocalDate;
  private String approvalCode;
  private Integer transactionCurrencyCode;
  private Integer reconciliationCurrencyCode;
  private Integer cardholderBillingCurrencyCode;
  private String merchantCode;
  private String merchantState;
  private String merchantCountry;
  private String transactionLifeCycleId;
  private String merchantName;
  private Boolean reconciled;
  Timestamps timestamps;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getFileId() {
    return fileId;
  }

  public void setFileId(Long fileId) {
    this.fileId = fileId;
  }

  public Integer getMessageType() {
    return messageType;
  }

  public void setMessageType(Integer messageType) {
    this.messageType = messageType;
  }

  public Integer getFunctionCode() {
    return functionCode;
  }

  public void setFunctionCode(Integer functionCode) {
    this.functionCode = functionCode;
  }

  public Integer getMessageReason() {
    return messageReason;
  }

  public void setMessageReason(Integer messageReason) {
    this.messageReason = messageReason;
  }

  public Integer getMessageNumber() {
    return messageNumber;
  }

  public void setMessageNumber(Integer messageNumber) {
    this.messageNumber = messageNumber;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public BigDecimal getTransactionAmount() {
    return transactionAmount;
  }

  public void setTransactionAmount(BigDecimal transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public BigDecimal getReconciliationAmount() {
    return reconciliationAmount;
  }

  public void setReconciliationAmount(BigDecimal reconciliationAmount) {
    this.reconciliationAmount = reconciliationAmount;
  }

  public BigDecimal getCardholderBillingAmount() {
    return cardholderBillingAmount;
  }

  public void setCardholderBillingAmount(BigDecimal cardholderBillingAmount) {
    this.cardholderBillingAmount = cardholderBillingAmount;
  }

  public BigDecimal getReconciliationConversionRate() {
    return reconciliationConversionRate;
  }

  public void setReconciliationConversionRate(BigDecimal reconciliationConversionRate) {
    this.reconciliationConversionRate = reconciliationConversionRate;
  }

  public BigDecimal getCardholderBillingConversionRate() {
    return cardholderBillingConversionRate;
  }

  public void setCardholderBillingConversionRate(BigDecimal cardholderBillingConversionRate) {
    this.cardholderBillingConversionRate = cardholderBillingConversionRate;
  }

  public LocalDateTime getTransactionLocalDate() {
    return transactionLocalDate;
  }

  public void setTransactionLocalDate(LocalDateTime transactionLocalDate) {
    this.transactionLocalDate = transactionLocalDate;
  }

  public String getApprovalCode() {
    return approvalCode;
  }

  public void setApprovalCode(String approvalCode) {
    this.approvalCode = approvalCode;
  }

  public Integer getTransactionCurrencyCode() {
    return transactionCurrencyCode;
  }

  public void setTransactionCurrencyCode(Integer transactionCurrencyCode) {
    this.transactionCurrencyCode = transactionCurrencyCode;
  }

  public Integer getReconciliationCurrencyCode() {
    return reconciliationCurrencyCode;
  }

  public void setReconciliationCurrencyCode(Integer reconciliationCurrencyCode) {
    this.reconciliationCurrencyCode = reconciliationCurrencyCode;
  }

  public Integer getCardholderBillingCurrencyCode() {
    return cardholderBillingCurrencyCode;
  }

  public void setCardholderBillingCurrencyCode(Integer cardholderBillingCurrencyCode) {
    this.cardholderBillingCurrencyCode = cardholderBillingCurrencyCode;
  }

  public String getMerchantCode() {
    return merchantCode;
  }

  public void setMerchantCode(String merchantCode) {
    this.merchantCode = merchantCode;
  }

  public String getMerchantState() {
    return merchantState;
  }

  public void setMerchantState(String merchantState) {
    this.merchantState = merchantState;
  }

  public String getMerchantCountry() {
    return merchantCountry;
  }

  public void setMerchantCountry(String merchantCountry) {
    this.merchantCountry = merchantCountry;
  }

  public String getTransactionLifeCycleId() {
    return transactionLifeCycleId;
  }

  public void setTransactionLifeCycleId(String transactionLifeCycleId) {
    this.transactionLifeCycleId = transactionLifeCycleId;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  public Boolean getReconciled() {
    return reconciled;
  }

  public void setReconciled(Boolean reconciled) {
    this.reconciled = reconciled;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
