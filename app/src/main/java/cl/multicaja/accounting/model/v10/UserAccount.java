package cl.multicaja.accounting.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.helpers.users.model.UserBankAccountStatus;
import cl.multicaja.tecnocom.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

public class UserAccount extends BaseModel {

  private Long id;
  private String accountAlias;
  private Long bankId;
  private String bankName;
  private String accountType;
  private String accountNumber;
  private UserBankAccountStatus status;
  private Timestamps timestamps;
  private Rut rut;


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

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public UserBankAccountStatus getStatus() {
    return status;
  }

  public void setStatus(UserBankAccountStatus status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  public Rut getRut() {
    return rut;
  }

  public void setRut(Rut rut) {
    this.rut = rut;
  }

  public String getCensoredAccount() {
    int numberOfDigits = accountNumber.length();
    int numberOfX = numberOfDigits - 4;
    String censoredAccount = StringUtils.repeat("X", numberOfX);
    censoredAccount = censoredAccount.concat(accountNumber.substring(numberOfX));
    return censoredAccount;
  }
}
