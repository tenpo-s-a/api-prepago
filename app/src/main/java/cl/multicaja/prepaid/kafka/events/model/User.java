package cl.multicaja.prepaid.kafka.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User extends BaseModel{

  private @JsonProperty("address") String address;
  private @JsonProperty("countryCode") Long countryCode;
  private @JsonProperty("documentNumber") String documentNumber;
  private @JsonProperty("documentSeries") String documentSeries;
  private @JsonProperty("email") String email;
  private @JsonProperty("firstName") String firstName;
  private @JsonProperty("id") String id;
  private @JsonProperty("lastName") String lastName;
  private @JsonProperty("level") String level;
  private @JsonProperty("nationality") String nationality;
  private @JsonProperty("phone") String phone;
  private @JsonProperty("plan") String plan;
  private @JsonProperty("profession") String profession;
  private @JsonProperty("qrContent") String qrContent;
  private @JsonProperty("regionCode") Long regionCode;
  private @JsonProperty("state") String state;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Long getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(Long countryCode) {
    this.countryCode = countryCode;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public String getDocumentSeries() {
    return documentSeries;
  }

  public void setDocumentSeries(String documentSeries) {
    this.documentSeries = documentSeries;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPlan() {
    return plan;
  }

  public void setPlan(String plan) {
    this.plan = plan;
  }

  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
  }

  public String getQrContent() {
    return qrContent;
  }

  public void setQrContent(String qrContent) {
    this.qrContent = qrContent;
  }

  public Long getRegionCode() {
    return regionCode;
  }

  public void setRegionCode(Long regionCode) {
    this.regionCode = regionCode;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
