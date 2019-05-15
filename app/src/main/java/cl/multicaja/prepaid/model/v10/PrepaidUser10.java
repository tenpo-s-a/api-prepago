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

  private PrepaidUserStatus status;
  private cl.multicaja.prepaid.model.v10.Timestamps timestamps;
  private PrepaidUserLevel userLevel;
  @JsonIgnore
  private PrepaidBalanceInfo10 balance;
  private boolean hasPendingFirstTopup;

  // Campos nuevos
  private String name;
  private String lastName;
  private String documentNumber;
  private DocumentType documentType;
  private String uuid;
  private UserPlanType userPlan;

  public PrepaidUser10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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



  public PrepaidUserLevel getUserLevel() {
    return userLevel;
  }

  public void setUserLevel(PrepaidUserLevel userLevel) {
    this.userLevel = userLevel;
  }



  public boolean isHasPendingFirstTopup() {
    return hasPendingFirstTopup;
  }

  public void setHasPendingFirstTopup(boolean hasPendingFirstTopup) {
    this.hasPendingFirstTopup = hasPendingFirstTopup;
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

  public UserPlanType getUserPlan() {
    return userPlan;
  }

  public void setUserPlan(UserPlanType userPlan) {
    this.userPlan = userPlan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidUser10)) return false;
    PrepaidUser10 that = (PrepaidUser10) o;
    return Objects.equals(getId(), that.getId()) &&
      Objects.equals(getDocumentNumber(), that.getDocumentNumber()) &&
      Objects.equals(getUuid(), that.getUuid()) &&
      getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getUuid(), getDocumentNumber(), getStatus(), getTimestamps());
  }

  @Override
  public String toString() {
    return "PrepaidUser10{" +
      "id=" + id +
      ", status=" + status +
      ", timestamps=" + timestamps +
      ", userLevel=" + userLevel +
      ", balance=" + balance +
      ", hasPendingFirstTopup=" + hasPendingFirstTopup +
      ", name='" + name + '\'' +
      ", lastName='" + lastName + '\'' +
      ", documentNumber='" + documentNumber + '\'' +
      ", documentType=" + documentType +
      ", uuid='" + uuid + '\'' +
      '}';
  }
}
