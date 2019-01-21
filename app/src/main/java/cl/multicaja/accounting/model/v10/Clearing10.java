package cl.multicaja.accounting.model.v10;

import cl.multicaja.prepaid.helpers.users.model.Timestamps;

public class Clearing10 extends Accounting10 {

  //TODO: Revisar estos 3 atributos y la herencia
  private Long clearingId;
  private Long clearingFileId;
  private AccountingStatusType clearingStatus;
  private UserAccount userAccount;
  private Timestamps timestamps;

  public Long getClearingFileId() {
    return clearingFileId;
  }

  public void setClearingFileId(Long clearingFileId) {
    this.clearingFileId = clearingFileId;
  }

  public AccountingStatusType getClearingStatus() {
    return clearingStatus;
  }

  public void setClearingStatus(AccountingStatusType clearingStatus) {
    this.clearingStatus = clearingStatus;
  }

  public Long getClearingId() {
    return clearingId;
  }

  public void setClearingId(Long clearingId) {
    this.clearingId = clearingId;
  }

  public UserAccount getUserAccount() {
    return userAccount;
  }

  public void setUserAccount(UserAccount userAccount) {
    this.userAccount = userAccount;
  }

  public void setUserAccountId(Long userAccountId){
    if(userAccount == null){
      userAccount = new UserAccount();
    }
    userAccount.setId(userAccountId);
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
