package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseModel {

  public User() {
    super();
  }

  private Long id;
  private Email email;
  private String name;
  private String lastname_1;
  private String lastname_2;
  private NameStatus nameStatus;

  @JsonIgnore
  private String password;
  private LocalDate birthday;
  private Character gender;
  private Rut rut;
  private CellPhone cellphone;
  private CompanyData companyData;
  private Timestamps timestamps;
  private UserStatus globalStatus;
  private Boolean hasPassword;
  private UserIdentityStatus identityStatus;
  private String FreshDeskId;

  private String occupation;

  private Boolean isBlacklisted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Email getEmail() {
    return email;
  }

  public void setEmail(Email email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastname_1() {
    return lastname_1;
  }

  public void setLastname_1(String lastname_1) {
    this.lastname_1 = lastname_1;
  }

  public String getLastname_2() {
    return lastname_2;
  }

  public void setLastname_2(String lastname_2) {
    this.lastname_2 = lastname_2;
  }

  public NameStatus getNameStatus() {
    return nameStatus;
  }

  public void setNameStatus(NameStatus nameStatus) {
    this.nameStatus = nameStatus;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public Character getGender() {
    return gender;
  }

  public void setGender(Character gender) {
    this.gender = gender;
  }

  public Rut getRut() {
    return rut;
  }

  public void setRut(Rut rut) {
    this.rut = rut;
  }

  public CellPhone getCellphone() {
    return cellphone;
  }

  public void setCellphone(CellPhone cellphone) {
    this.cellphone = cellphone;
  }

  public CompanyData getCompanyData() {
    return companyData;
  }

  public void setCompanyData(CompanyData companyData) {
    this.companyData = companyData;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  public UserStatus getGlobalStatus() {
    return globalStatus;
  }

  public void setGlobalStatus(UserStatus globalStatus) {
    this.globalStatus = globalStatus;
  }

  public Boolean getHasPassword() {
    return hasPassword;
  }

  public void setHasPassword(Boolean hasPassword) {
    this.hasPassword = hasPassword;
  }

  //@JsonProperty("is_blacklisted")
  @JsonIgnore
  public Boolean getIsBlacklisted() {
    return this.identityStatus != null ? UserIdentityStatus.TERRORIST.equals(this.identityStatus) : null;
  }

  public void setIsBlacklisted(Boolean isBlacklisted) {
    this.isBlacklisted = isBlacklisted;
  }

  public UserIdentityStatus getIdentityStatus() {
    return identityStatus;
  }

  public void setIdentityStatus(UserIdentityStatus identityStatus) {
    this.identityStatus = identityStatus;
  }

  public String getFreshDeskId() {
    return FreshDeskId;
  }

  public void setFreshDeskId(String freshDeskId) {
    FreshDeskId = freshDeskId;
  }

  public String getOccupation() {
    return occupation;
  }

  public void setOccupation(String occupation) {
    this.occupation = occupation;
  }
}
