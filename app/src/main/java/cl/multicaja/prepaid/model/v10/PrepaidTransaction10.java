package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class PrepaidTransaction10 extends BaseModel {

  private TransactionType10 transactionType;
  private String authCode;
  private String processorTransactionId;
  private String pan;
  private String realDate;
  private String accountingDate;
  private String processingDate;
  private NewAmountAndCurrency10 amountPrimary;
  private NewAmountAndCurrency10 amountForeign;
  private Place10 place;
  private Merchant10 merchant;

  public PrepaidTransaction10() {
    super();
  }

  public TransactionType10 getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(TransactionType10 transactionType) {
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

  public NewAmountAndCurrency10 getAmountPrimary() {
    return amountPrimary;
  }

  public void setAmountPrimary(NewAmountAndCurrency10 amountPrimary) {
    this.amountPrimary = amountPrimary;
  }

  public NewAmountAndCurrency10 getAmountForeign() {
    return amountForeign;
  }

  public void setAmountForeign(NewAmountAndCurrency10 amountForeign) {
    this.amountForeign = amountForeign;
  }

  public Place10 getPlace() {
    return place;
  }

  public void setPlace(Place10 place) {
    this.place = place;
  }

  public Merchant10 getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant10 merchant) {
    this.merchant = merchant;
  }

}
