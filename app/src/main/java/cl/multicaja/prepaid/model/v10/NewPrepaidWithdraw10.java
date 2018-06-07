package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author abarazarte
 */
public class NewPrepaidWithdraw10 extends NewPrepaidBaseTransaction10 {

  public NewPrepaidWithdraw10() {
    super(PrepaidMovementType.WITHDRAW);
  }

  public NewPrepaidWithdraw10(NewAmountAndCurrency10 amount, String transactionId, Integer rut, String merchantCode, String merchantName, Integer merchantCategory) {
    super(amount, transactionId, rut, merchantCode, merchantName, merchantCategory, PrepaidMovementType.WITHDRAW);
  }

  @JsonIgnore
  public CdtTransactionType getCdtTransactionType() {
    return TransactionOriginType.WEB.equals(this.getTransactionOriginType()) ? CdtTransactionType.RETIRO_WEB : CdtTransactionType.RETIRO_POS;
  }

  @JsonIgnore
  public CdtTransactionType getCdtTransactionTypeConfirm() {
    return TransactionOriginType.WEB.equals(this.getTransactionOriginType()) ? CdtTransactionType.RETIRO_WEB_CONF : CdtTransactionType.RETIRO_POS_CONF;
  }

}
