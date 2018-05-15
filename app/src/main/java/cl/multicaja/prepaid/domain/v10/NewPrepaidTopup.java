package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author abarazarte
 */
public class NewPrepaidTopup extends BaseModel {

  //TODO: externalizar este numero?
  @JsonIgnore
  private final String WEB_MERCHANT_CODE = "999999999999991";

  private NewAmountAndCurrency10 amount;
  private String transactionId;
  private Integer rut;
  private String merchantCode;

  @JsonIgnore
  private Boolean isFirstTopup = Boolean.TRUE;

  public NewPrepaidTopup() {
    super();
  }

  public NewPrepaidTopup(NewAmountAndCurrency10 amount, String transactionId, Integer rut, String merchantCode) {
    super();

    this.amount = amount;
    this.transactionId = transactionId;
    this.rut = rut;
    this.merchantCode = merchantCode;
  }

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
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

  @JsonIgnore
  public Boolean isFirstTopup() {
    return isFirstTopup;
  }

  public void setFirstTopup(Boolean firstTopup) {
    isFirstTopup = firstTopup;
  }

  @JsonIgnore
  public TopupType getType () {
    return this.getMerchantCode().equals(WEB_MERCHANT_CODE) ? TopupType.WEB : TopupType.POS;
  }

  @JsonIgnore
  public CdtTransactionType getCdtTransactionType() {
    //Si es N = 1 -> Solicitud primera carga
    if(this.isFirstTopup()){
      return CdtTransactionType.SOL_1_CARGA;
    }
    else {
      // es N = 2
      return this.getType().equals(TopupType.WEB) ? CdtTransactionType.SOL_CARGA_WEB : CdtTransactionType.SOL_CARGA_POS;
    }
  }

}
