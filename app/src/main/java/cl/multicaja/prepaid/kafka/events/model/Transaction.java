package cl.multicaja.prepaid.kafka.events.model;

import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;

import java.util.List;

public class Transaction extends BaseModel {

  private String remoteTransactionId;
  private String authCode;
  private NewAmountAndCurrency10 primaryAmount;
  private NewAmountAndCurrency10 secondaryAmount;
  private Merchant merchant;
  private String type;
  private Integer countryCode;
  private List<Fee> fees;

  public Transaction() {
    super();
  }

  public String getRemoteTransactionId() {
    return remoteTransactionId;
  }

  public void setRemoteTransactionId(String remoteTransactionId) {
    this.remoteTransactionId = remoteTransactionId;
  }

  public String getAuthCode() {
    return authCode;
  }

  public void setAuthCode(String authCode) {
    this.authCode = authCode;
  }

  public NewAmountAndCurrency10 getPrimaryAmount() {
    return primaryAmount;
  }

  public void setPrimaryAmount(NewAmountAndCurrency10 primaryAmount) {
    this.primaryAmount = primaryAmount;
  }

  public NewAmountAndCurrency10 getSecondaryAmount() {
    return secondaryAmount;
  }

  public void setSecondaryAmount(NewAmountAndCurrency10 secondaryAmount) {
    this.secondaryAmount = secondaryAmount;
  }

  public Merchant getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant merchant) {
    this.merchant = merchant;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(Integer countryCode) {
    this.countryCode = countryCode;
  }

  public List<Fee> getFees() {
    return fees;
  }

  public void setFees(List<Fee> fees) {
    this.fees = fees;
  }

  @Override
  public String toString() {
    return "Transaction{" +
      "remoteTransactionId='" + remoteTransactionId + '\'' +
      ", authCode='" + authCode + '\'' +
      ", primaryAmount=" + primaryAmount +
      ", secondaryAmount=" + secondaryAmount +
      ", merchant=" + merchant +
      ", type='" + type + '\'' +
      ", countryCode=" + countryCode +
      ", fees=" + fees +
      '}';
  }
}
