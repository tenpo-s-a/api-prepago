package cl.multicaja.accounting.helpers.mastercard.model;


import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;

public class IpmMessage extends BaseModel {

  /**
   * MTI Message Type Identifier
   */
  private Integer mti;

  /**
   * DE24 Function Code
   */
  private Integer functionCode;

  /**
   * DE25 Message reason code
   */
  private Integer messageReasonCode;

  /**
   * DE71 Message number
   */
  private Integer messageNumber;

  /**
   * DE2 Pan
   */
  private String pan;

  /**
   * DE4 Transaction amount
   */
  private BigDecimal transactionAmount;

  /**
   * DE5 Reconciliation amount
   */
  private BigDecimal reconciliationAmount;

  /**
   * DE6 Cardholder billing amount
   */
  private BigDecimal cardholderBillingAmount;

  /**
   * DE9 Reconciliation conversion rate
   */
  private String reconciliationConversionRate;

  /**
   * DE10 Cardholder billing conversion rate
   */
  private String cardholderBillingConversionRate;

  /**
   * DE12 Transaction local date time
   */
  private String transactionLocalDate;

  /**
   * DE38 Approval code
   */
  private Integer approvalCode;

  /**
   *  DE49 Transaction currency code
   */
  private Integer transactionCurrencyCode;

  /**
   * DE50 Reconciliation currency code
   */
  private Integer reconciliationCurrencyCode;

  /**
   * DE51 Cardholder billing currency code
   */
  private Integer cardholderBillingCurrencyCode;

  /**
   * PDS0148 Currency exponents
   */
  private String currencyExponents;

  /**
   * DE43 Merchat info
   */
  private String merchantInfo;

  /**
   * DE43 - Merchant name
   */
  private String merchatName;

  /**
   * DE43 - Merchant suburb
   */
  private String merchantSuburb;

  /**
   * DE43 - Merchant postal code
   */
  private String merchantPostalCode;

  /**
   * DE43 - Merchant country
   */
  private String merchantCountry;

  /**
   * PDS0306 - Message count
   */
  private Integer messageCount;

  /**
   * PDS0105 - File Id
   */
  private String fileId;

  public IpmMessage() {
    super();
  }

  public Integer getMti() {
    return mti;
  }

  public void setMti(Integer mti) {
    this.mti = mti;
  }

  public Integer getFunctionCode() {
    return functionCode;
  }

  public void setFunctionCode(Integer functionCode) {
    this.functionCode = functionCode;
  }

  public Integer getMessageReasonCode() {
    return messageReasonCode;
  }

  public void setMessageReasonCode(Integer messageReasonCode) {
    this.messageReasonCode = messageReasonCode;
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

  public String getReconciliationConversionRate() {
    return reconciliationConversionRate;
  }

  public void setReconciliationConversionRate(String reconciliationConversionRate) {
    this.reconciliationConversionRate = reconciliationConversionRate;
  }

  public String getCardholderBillingConversionRate() {
    return cardholderBillingConversionRate;
  }

  public void setCardholderBillingConversionRate(String cardholderBillingConversionRate) {
    this.cardholderBillingConversionRate = cardholderBillingConversionRate;
  }

  public String getTransactionLocalDate() {
    return transactionLocalDate;
  }

  public void setTransactionLocalDate(String transactionLocalDate) {
    this.transactionLocalDate = transactionLocalDate;
  }

  public Integer getApprovalCode() {
    return approvalCode;
  }

  public void setApprovalCode(Integer approvalCode) {
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

  public String getCurrencyExponents() {
    return currencyExponents;
  }

  public void setCurrencyExponents(String currencyExponents) {
    this.currencyExponents = currencyExponents;
  }

  public String getMerchantInfo() {
    return merchantInfo;
  }

  public void setMerchantInfo(String merchantInfo) {
    this.merchantInfo = merchantInfo;
  }

  public String getMerchatName() {
    return merchatName;
  }

  public void setMerchatName(String merchatName) {
    this.merchatName = merchatName;
  }

  public String getMerchantSuburb() {
    return merchantSuburb;
  }

  public void setMerchantSuburb(String merchantSuburb) {
    this.merchantSuburb = merchantSuburb;
  }

  public String getMerchantPostalCode() {
    return merchantPostalCode;
  }

  public void setMerchantPostalCode(String merchantPostalCode) {
    this.merchantPostalCode = merchantPostalCode;
  }

  public String getMerchantCountry() {
    return merchantCountry;
  }

  public void setMerchantCountry(String merchantCountry) {
    this.merchantCountry = merchantCountry;
  }

  public Integer getMessageCount() {
    return messageCount;
  }

  public void setMessageCount(Integer messageCount) {
    this.messageCount = messageCount;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
}
