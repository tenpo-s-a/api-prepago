package cl.multicaja.prepaid.model.v11;

import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.prepaid.model.v10.TransactionOriginType;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author abarazarte
 */
public class NewPrepaidTopup11 extends NewPrepaidBaseTransaction11 {

  @JsonIgnore
  private Boolean isFirstTopup = Boolean.TRUE;

  public NewPrepaidTopup11() {
    super(PrepaidMovementType.TOPUP);
  }

  public NewPrepaidTopup11(NewAmountAndCurrency10 amount, String transactionId, String merchantCode, String merchantName, Integer merchantCategory, Boolean isFirstTopup) {
    super(amount, transactionId,  merchantCode, merchantName, merchantCategory, PrepaidMovementType.TOPUP);
    this.isFirstTopup = isFirstTopup;
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
