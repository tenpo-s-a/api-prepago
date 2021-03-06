package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class NewPrepaidBaseTransaction10 extends BaseModel {

  @JsonIgnore
  public static final String WEB_MERCHANT_CODE = "999999999999991";

  private NewAmountAndCurrency10 amount;
  private String transactionId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer rut;
  private String merchantCode;
  private String merchantName;
  private Integer merchantCategory;
  @JsonIgnore
  private PrepaidMovementType movementType;

  public NewPrepaidBaseTransaction10() {
    super();
  }

  public NewPrepaidBaseTransaction10(PrepaidMovementType movementType) {
    super();
    this.movementType = movementType;
  }

  public NewPrepaidBaseTransaction10(NewAmountAndCurrency10 amount, String transactionId, Integer rut, String merchantCode, String merchantName, Integer merchantCategory, PrepaidMovementType movementType) {
    super();
    this.amount = amount;
    this.transactionId = transactionId;
    this.rut = rut;
    this.merchantCode = merchantCode;
    this.merchantName = merchantName;
    this.merchantCategory = merchantCategory;
    this.movementType = movementType;
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

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  public Integer getMerchantCategory() {
    return merchantCategory;
  }

  public void setMerchantCategory(Integer merchantCategory) {
    this.merchantCategory = merchantCategory;
  }

  @JsonIgnore
  public TransactionOriginType getTransactionOriginType () {
    return WEB_MERCHANT_CODE.equals(this.getMerchantCode()) ? TransactionOriginType.WEB : TransactionOriginType.POS;
  }

  public PrepaidMovementType getMovementType() {
    return movementType;
  }

  public void setMovementType(PrepaidMovementType movementType) {
    this.movementType = movementType;
  }
}
