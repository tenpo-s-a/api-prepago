package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class PrepaidTransactionDTO {

  private TransactionType transactionType;
  private String authCode;
  private String processorTransactionId;
  private String pan;
  private String realDate;
  private String accountingDate;
  private String processingDate;
  private Amount amountPrimary;
  private Amount amountForeign;
  private Place place;
  private Merchant merchant;

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

  public Amount getAmountPrimary() {
    return amountPrimary;
  }

  public void setAmountPrimary(Amount amountPrimary) {
    this.amountPrimary = amountPrimary;
  }

  public Amount getAmountForeign() {
    return amountForeign;
  }

  public void setAmountForeign(Amount amountForeign) {
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

