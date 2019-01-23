package cl.multicaja.users.model.v10;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAccountNew implements Serializable {

  private String accountAlias;
  private Long bankId;
  private String accountType;
  private String accountNumber;
  private Integer rut;

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

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }
}
