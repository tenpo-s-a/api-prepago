package cl.multicaja.prepaid.helpers.tenpo.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import cl.multicaja.core.model.BaseModel;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenpoUser extends BaseModel {

  private static final long serialVersionUID = 8399887731426206305L;

  private UUID id;
  private String phone;
  private String firstName;
  private String lastName;
  private String email;
  private String regionCode;
  private String documentNumber;
  private String tributaryIdentifier;
  private String address;
  private String profession;
  private String countryCode;
  private State state;
  private Level level;
  private Plan plan;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean agreeTermsConditions;
  private Boolean cardActive;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRegionCode() {
    return regionCode;
  }

  public void setRegionCode(String regionCode) {
    this.regionCode = regionCode;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public String getTributaryIdentifier() {
    return tributaryIdentifier;
  }

  public void setTributaryIdentifier(String tributaryIdentifier) {
    this.tributaryIdentifier = tributaryIdentifier;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  public Plan getPlan() {
    return plan;
  }

  public void setPlan(Plan plan) {
    this.plan = plan;
  }

  public Boolean getAgreeTermsConditions() {
    return agreeTermsConditions;
  }

  public void setAgreeTermsConditions(Boolean agreeTermsConditions) {
    this.agreeTermsConditions = agreeTermsConditions;
  }

  public Boolean getCardActive() {
    return cardActive;
  }

  public void setCardActive(Boolean cardActive) {
    this.cardActive = cardActive;
  }
}
