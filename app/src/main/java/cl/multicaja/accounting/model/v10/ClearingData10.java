package cl.multicaja.accounting.model.v10;

public class ClearingData10 extends AccountingData10 {

  private Long accountingId;
  private UserAccount userBankAccount;

  public ClearingData10() {
    super();
  }

  public Long getAccountingId() {
    return accountingId;
  }

  public void setAccountingId(Long accountingId) {
    this.accountingId = accountingId;
  }

  public UserAccount getUserBankAccount() {
    return userBankAccount;
  }

  public void setUserBankAccount(UserAccount userBankAccount) {
    this.userBankAccount = userBankAccount;
  }

  public void setUserAccountId(Long userAccountId){
    if(userBankAccount == null){
      userBankAccount = new UserAccount();
    }
    userBankAccount.setId(userAccountId);
  }
}
