package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class NewPrepaidTopup {

  private NewAmountAndCurrency amount;
  private String transactionId;
  private Integer rut;
  private String merchantCode;

  public NewAmountAndCurrency getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency amount) {
    this.amount = amount;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public String getMerchantCode() {
    return merchantCode;
  }

  public void setMerchantCode(String merchantCode) {
    this.merchantCode = merchantCode;
  }

}
