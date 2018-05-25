package cl.multicaja.cdt.model.v10;

import cl.multicaja.prepaid.model.v10.CdtTransactionType;

import java.math.BigDecimal;


public class CdtTransaction10 {

  private String accountId;
  private CdtTransactionType transactionType;
  private Long transactionReference;
  private String externalTransactionId;
  private String gloss;
  private BigDecimal amount;

  private String numError = "0";
  private String msjError = "";

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public CdtTransactionType getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(CdtTransactionType transactionType) {
    this.transactionType = transactionType;
  }

  public Long getTransactionReference() {
    return transactionReference;
  }

  public void setTransactionReference(Long transactionReference) {
    this.transactionReference = transactionReference;
  }

  public String getExternalTransactionId() {
    return externalTransactionId;
  }

  public void setExternalTransactionId(String externalTransactionId) {
    this.externalTransactionId = externalTransactionId;
  }

  public String getGloss() {
    return gloss;
  }

  public void setGloss(String gloss) {
    this.gloss = gloss;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getNumError() {
    return numError;
  }

  public void setNumError(String numError) {
    this.numError = numError;
  }

  public String getMsjError() {
    return msjError;
  }

  public void setMsjError(String msjError) {
    this.msjError = msjError;
  }
}
