package cl.multicaja.prepaid.async.v10.model;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @autor vutreras
 */
//TODO: Revisar implementacion (esta clase hace muchas cosas)
public class PrepaidTopupData10 implements Serializable {

  private PrepaidTopup10 prepaidTopup10;
  private PrepaidWithdraw10 prepaidWithdraw10;

  private PrepaidUser10 prepaidUser10;
  private PrepaidCard10 prepaidCard10;
  private CdtTransaction10 cdtTransaction10;
  private CdtTransaction10 cdtTransactionConfirm10;
  private PrepaidMovement10 prepaidMovement10;
  private PrepaidMovement10 issuanceFeeMovement10;
  private UserAccount userAccount;
  private Account account;

  private Errors numError;
  private String msjError;

  public PrepaidTopupData10() {
    super();
  }


  public PrepaidTopupData10(PrepaidTopup10 prepaidTopup, PrepaidUser10 prepaidUser10, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {

    this.prepaidTopup10 = prepaidTopup;
    this.prepaidUser10 = prepaidUser10;
    this.cdtTransaction10 = cdtTransaction;
    this.prepaidMovement10 = prepaidMovement;

  }

  public PrepaidTopup10 getPrepaidTopup10() {
    return prepaidTopup10;
  }

  public void setPrepaidTopup10(PrepaidTopup10 prepaidTopup10) {
    this.prepaidTopup10 = prepaidTopup10;
  }

  public PrepaidWithdraw10 getPrepaidWithdraw10() {
    return prepaidWithdraw10;
  }

  public void setPrepaidWithdraw10(PrepaidWithdraw10 prepaidWithdraw10) {
    this.prepaidWithdraw10 = prepaidWithdraw10;
  }


  public PrepaidUser10 getPrepaidUser10() {
    return prepaidUser10;
  }

  public void setPrepaidUser10(PrepaidUser10 prepaidUser10) {
    this.prepaidUser10 = prepaidUser10;
  }

  public PrepaidCard10 getPrepaidCard10() {
    return prepaidCard10;
  }

  public void setPrepaidCard10(PrepaidCard10 prepaidCard10) {
    this.prepaidCard10 = prepaidCard10;
  }

  public CdtTransaction10 getCdtTransaction10() {
    return cdtTransaction10;
  }

  public void setCdtTransaction10(CdtTransaction10 cdtTransaction10) {
    this.cdtTransaction10 = cdtTransaction10;
  }

  public PrepaidMovement10 getPrepaidMovement10() {
    return prepaidMovement10;
  }

  public void setPrepaidMovement10(PrepaidMovement10 prepaidMovement10) {
    this.prepaidMovement10 = prepaidMovement10;
  }

  public PrepaidMovement10 getIssuanceFeeMovement10() {
    return issuanceFeeMovement10;
  }

  public void setIssuanceFeeMovement10(PrepaidMovement10 issuanceFeeMovement10) {
    this.issuanceFeeMovement10 = issuanceFeeMovement10;
  }

  public UserAccount getUserAccount() {
    return userAccount;
  }

  public void setUserAccount(UserAccount userAccount) {
    this.userAccount = userAccount;
  }

  public CdtTransaction10 getCdtTransactionConfirm10() {
    return cdtTransactionConfirm10;
  }

  public void setCdtTransactionConfirm10(CdtTransaction10 cdtTransactionConfirm10) {
    this.cdtTransactionConfirm10 = cdtTransactionConfirm10;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public Errors getNumError() {
    return numError;
  }

  public void setNumError(Errors numError) {
    this.numError = numError;
  }

  public String getMsjError() {
    return msjError;
  }

  public void setMsjError(String msjError) {
    this.msjError = msjError;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
