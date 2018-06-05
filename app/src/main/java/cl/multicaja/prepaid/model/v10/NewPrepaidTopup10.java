package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author abarazarte
 */
public class NewPrepaidTopup10 extends NewPrepaidBaseTransaction10 {

  @JsonIgnore
  private Boolean isFirstTopup = Boolean.TRUE;

  public NewPrepaidTopup10() {
    super();
  }

  public NewPrepaidTopup10(NewAmountAndCurrency10 amount, String transactionId, Integer rut, String merchantCode, String merchantName, Integer merchantCategory) {
    super(amount, transactionId, rut, merchantCode, merchantName, merchantCategory, PrepaidMovementType.TOPUP);
  }

  @JsonIgnore
  public Boolean isFirstTopup() {
    return isFirstTopup;
  }

  public void setFirstTopup(Boolean firstTopup) {
    isFirstTopup = firstTopup;
  }

  @JsonIgnore
  public CdtTransactionType getCdtTransactionType() {
    //Si es N = 1 -> Solicitud primera carga
    if(this.isFirstTopup()){
      return CdtTransactionType.PRIMERA_CARGA;
    }
    else {
      // es N = 2
      return TransactionOriginType.WEB.equals(this.getTransactionOriginType()) ? CdtTransactionType.CARGA_WEB : CdtTransactionType.CARGA_POS;
    }
  }

  @JsonIgnore
  public CdtTransactionType getCdtTransactionTypeConfirm() {
    //Si es N = 1 -> Solicitud primera carga
    if(this.isFirstTopup()){
      return CdtTransactionType.PRIMERA_CARGA_CONF;
    }
    else {
      // es N = 2
      return TransactionOriginType.WEB.equals(this.getTransactionOriginType()) ? CdtTransactionType.CARGA_WEB_CONF : CdtTransactionType.CARGA_POS_CONF;
    }
  }

}
