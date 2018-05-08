package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class PrepaidTransaction extends BaseModel {

  private TransactionType transactionType;
  private String authCode;
  private String processorTransactionId;
  private String pan;
  private String realDate;
  private String accountingDate;
  private String processingDate;
  private AmountAndCurrency amountPrimary;
  private AmountAndCurrency amountForeign;
  private Place place;
  private Merchant merchant;

  public PrepaidTransaction() {
    super();
  }

  public TransactionType getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(TransactionType transactionType) {
    this.transactionType = transactionType;
  }

  public String getAuthCode() {
    return authCode;
  }

  public void setAuthCode(String authCode) {
    this.authCode = authCode;
  }

  public String getProcessorTransactionId() {
    return processorTransactionId;
  }

  public void setProcessorTransactionId(String processorTransactionId) {
    this.processorTransactionId = processorTransactionId;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getRealDate() {
    return realDate;
  }

  public void setRealDate(String realDate) {
    this.realDate = realDate;
  }

  public String getAccountingDate() {
    return accountingDate;
  }

  public void setAccountingDate(String accountingDate) {
    this.accountingDate = accountingDate;
  }

  public String getProcessingDate() {
    return processingDate;
  }

  public void setProcessingDate(String processingDate) {
    this.processingDate = processingDate;
  }

  public AmountAndCurrency getAmountPrimary() {
    return amountPrimary;
  }

  public void setAmountPrimary(AmountAndCurrency amountPrimary) {
    this.amountPrimary = amountPrimary;
  }

  public AmountAndCurrency getAmountForeign() {
    return amountForeign;
  }

  public void setAmountForeign(AmountAndCurrency amountForeign) {
    this.amountForeign = amountForeign;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(Place place) {
    this.place = place;
  }

  public Merchant getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant merchant) {
    this.merchant = merchant;
  }

}
