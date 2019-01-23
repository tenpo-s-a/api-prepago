package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author abarazarte
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewPrepaidWithdraw10 extends NewPrepaidBaseTransaction10 {

  private String password;
  private Long bankAccountId;

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Long getBankAccountId() {
    return bankAccountId;
  }

  public void setBankAccountId(Long bankAccountId) {
    this.bankAccountId = bankAccountId;
  }

  @Override
  public String toString() {
    return "NewPrepaidBaseTransaction10{" +
      "amount=" + this.getAmount() +
      ", transactionId='" + this.getTransactionId() + '\'' +
      ", rut=" + this.getRut() +
      ", merchantCode='" + this.getMerchantCode() + '\'' +
      ", merchantName='" + this.getMerchantName() + '\'' +
      ", merchantCategory=" + this.getMerchantCategory() +
      ", movementType=" + this.getMovementType() +
      '}';
  }
}
