package cl.multicaja.accounting.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.prepaid.helpers.users.model.UserBankAccountStatus;
import org.apache.commons.lang3.StringUtils;

public class UserAccount extends BaseModel {

  private Long id;
  private String accountAlias;
  private Long bankId;
  private String bankName;
  private String accountType;
  private Long accountNumber;
  private UserBankAccountStatus status;
  private String rut;

  public UserAccount() {
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAccountAlias() {
    return accountAlias;
  }

  public void setAccountAlias(String accountAlias) {
    this.accountAlias = accountAlias;
  }

  public Long getBankId() {
    return bankId;
  }

  public void setBankId(Long bankId) {
    this.bankId = bankId;
  }

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public String getAccountType() {
    return accountType;
  }

  public void setAccountType(String accountType) {
    this.accountType = accountType;
  }

  public Long getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(Long accountNumber) {
    this.accountNumber = accountNumber;
  }

  public UserBankAccountStatus getStatus() {
    return status;
  }

  public void setStatus(UserBankAccountStatus status) {
    this.status = status;
  }

  public String getRut() {
    return rut;
  }

  public void setRut(String rut) {
    this.rut = rut;
  }

  public String getCensoredAccount() {
    int numberOfDigits = String.valueOf(accountNumber).length();
    int numberOfX = Math.max(numberOfDigits - 4, 0);
    String censoredAccount = StringUtils.repeat("X", numberOfX);
    censoredAccount = censoredAccount.concat(String.valueOf(accountNumber).substring(numberOfX));
    return censoredAccount;
  }
}
