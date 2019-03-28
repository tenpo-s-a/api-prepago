package cl.multicaja.prepaid.helpers.tenpo.model;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;


public class User extends BaseModel {

  private String address;
  private String apartment;
  @JsonProperty("countryCode")
  private String countryCode;
  private String document;
  @JsonProperty("documentNumber")
  private String documentNumber;
  @JsonProperty("documentType")
  private String documentType;
  private String email;
  @JsonProperty("firstName")
  private String firstName;
  private UUID id;
  @JsonProperty("idIdentityProvider")
  private UUID idIdentityProvider;
  @JsonProperty("lastName")
  private String lastName;
  private String phone;
  @JsonProperty("qrKey")
  private String qrKey;
  private String street;
  @JsonProperty("streetNumber")
  private String streetNumber;
  @JsonProperty("userId")
  private UUID userId;
  private State state;
  private Level level;
  private Plan plan;
  @JsonProperty("regionCode")
  private String regionCode;
  private String nationality;
  private String profession;
  @JsonProperty("documentSeries")
  private String documentSeries;

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }



  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
  }



  public String getDocumentSeries() {
    return documentSeries;
  }

  public void setDocumentSeries(String documentSeries) {
    this.documentSeries = documentSeries;
  }



  public String getRegionCode() {
    return regionCode;
  }

  public void setRegionCode(String regionCode) {
    this.regionCode = regionCode;
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



  public String getApartment() {
    return apartment;
  }

  public void setApartment(String apartment) {
    this.apartment = apartment;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getDocument() {
    return document;
  }

  public void setDocument(String document) {
    this.document = document;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public String getDocumentType() {
    return documentType;
  }

  public void setDocumentType(String documentType) {
    this.documentType = documentType;
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

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getIdIdentityProvider() {
    return idIdentityProvider;
  }

  public void setIdIdentityProvider(UUID idIdentityProvider) {
    this.idIdentityProvider = idIdentityProvider;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getQrKey() {
    return qrKey;
  }

  public void setQrKey(String qrKey) {
    this.qrKey = qrKey;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getStreetNumber() {
    return streetNumber;
  }

  public void setStreetNumber(String streetNumber) {
    this.streetNumber = streetNumber;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }



  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

}
