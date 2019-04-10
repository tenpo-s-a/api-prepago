package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.prepaid.model.v11.DocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * @author abarazarte
 */
public class PrepaidUser10 extends BaseModel {

  private Long id;
  @JsonProperty("user_id")
  private Long userIdMc;
  @Deprecated
  private Integer rut;
  private PrepaidUserStatus status;
  private cl.multicaja.prepaid.model.v10.Timestamps timestamps;
  private PrepaidUserLevel userLevel;
  @JsonIgnore
  private PrepaidBalanceInfo10 balance;

  @Deprecated
  @JsonIgnore
  private Long balanceExpiration;
  @Deprecated
  private boolean hasPrepaidCard;
  private boolean hasPendingFirstTopup;
  @Deprecated
  @JsonIgnore
  private Integer identityVerificationAttempts;

  // Campos nuevos
  private String name;
  private String lastName;
  private String documentNumber;
  private DocumentType documentType;
  private String uuid;


  public PrepaidUser10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserIdMc() {
    return userIdMc;
  }

  public void setUserIdMc(Long userIdMc) {
    this.userIdMc = userIdMc;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public PrepaidUserStatus getStatus() {
    return status;
  }

  public void setStatus(PrepaidUserStatus status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  public PrepaidBalanceInfo10 getBalance() {
    return balance;
  }

  public void setBalance(PrepaidBalanceInfo10 balance) {
    this.balance = balance;
  }

  public Long getBalanceExpiration() {
    return balanceExpiration;
  }

  public void setBalanceExpiration(Long balanceExpiration) {
    this.balanceExpiration = balanceExpiration;
  }

  public PrepaidUserLevel getUserLevel() {
    return userLevel;
  }

  public void setUserLevel(PrepaidUserLevel userLevel) {
    this.userLevel = userLevel;
  }

  public boolean isHasPrepaidCard() {
    return hasPrepaidCard;
  }

  public void setHasPrepaidCard(boolean hasPrepaidCard) {
    this.hasPrepaidCard = hasPrepaidCard;
  }

  public boolean isHasPendingFirstTopup() {
    return hasPendingFirstTopup;
  }

  public void setHasPendingFirstTopup(boolean hasPendingFirstTopup) {
    this.hasPendingFirstTopup = hasPendingFirstTopup;
  }

  public Integer getIdentityVerificationAttempts() {
    return identityVerificationAttempts;
  }

  public void setIdentityVerificationAttempts(Integer identityVerificationAttempts) {
    this.identityVerificationAttempts = identityVerificationAttempts;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(DocumentType documentType) {
    this.documentType = documentType;
  }


  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidUser10)) return false;
    PrepaidUser10 that = (PrepaidUser10) o;
    return Objects.equals(getId(), that.getId()) &&
      Objects.equals(getUserIdMc(), that.getUserIdMc()) &&
      Objects.equals(getRut(), that.getRut()) &&
      getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getUserIdMc(), getRut(), getStatus(), getTimestamps());
  }

  @Override
  public String toString() {
    return "PrepaidUser10{" +
      "id=" + id +
      ", userIdMc=" + userIdMc +
      ", rut=" + rut +
      ", status=" + status +
      ", timestamps=" + timestamps +
      ", userLevel=" + userLevel +
      ", balance=" + balance +
      ", balanceExpiration=" + balanceExpiration +
      ", hasPrepaidCard=" + hasPrepaidCard +
      ", hasPendingFirstTopup=" + hasPendingFirstTopup +
      ", identityVerificationAttempts=" + identityVerificationAttempts +
      ", name='" + name + '\'' +
      ", lastName='" + lastName + '\'' +
      ", documentNumber='" + documentNumber + '\'' +
      ", documentType=" + documentType +
      ", uuid='" + uuid + '\'' +
      '}';
  }
}
