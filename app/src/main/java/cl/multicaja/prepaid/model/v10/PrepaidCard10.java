package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * @author abarazarte
 */
public class PrepaidCard10 extends BaseModel {

  private Long id;
  private String pan;
  @JsonIgnore
  private String encryptedPan;
  private Integer expiration;
  private String nameOnCard;
  private PrepaidCardStatus status;
  private Timestamps timestamps;
  @JsonIgnore
  private String producto;
  @JsonIgnore
  private String numeroUnico;

  // Nuevos campos
  @JsonIgnore
  private String uuid;

  @JsonIgnore
  private String hashedPan;

  @JsonIgnore
  private Long accountId;


  public PrepaidCard10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getEncryptedPan() {
    return encryptedPan;
  }

  public void setEncryptedPan(String encryptedPan) {
    this.encryptedPan = encryptedPan;
  }

  public Integer getExpiration() {
    return expiration;
  }

  public void setExpiration(Integer expiration) {
    this.expiration = expiration;
  }

  public String getNameOnCard() {
    return nameOnCard;
  }

  public void setNameOnCard(String nameOnCard) {
    this.nameOnCard = nameOnCard;
  }

  public PrepaidCardStatus getStatus() {
    return status;
  }

  public void setStatus(PrepaidCardStatus status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  public String getProducto() {
    return producto;
  }

  public void setProducto(String producto) {
    this.producto = producto;
  }

  public String getNumeroUnico() {
    return numeroUnico;
  }

  public void setNumeroUnico(String numeroUnico) {
    this.numeroUnico = numeroUnico;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getHashedPan() {
    return hashedPan;
  }

  public void setHashedPan(String hashedPan) {
    this.hashedPan = hashedPan;
  }

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long accountId) {
    this.accountId = accountId;
  }

  @JsonIgnore
  public String getFormattedExpiration() {
    String exp = this.getExpiration() != null ? this.getExpiration().toString() : null;
    String formattedExpiration = exp;
    if (exp != null && exp.length() > 4) {
      try {
        formattedExpiration = exp.substring(4) + "/" + exp.substring(0, 4);
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    return formattedExpiration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidCard10)) return false;
    PrepaidCard10 that = (PrepaidCard10) o;
    return Objects.equals(getId(), that.getId()) &&
      Objects.equals(getPan(), that.getPan()) &&
      Objects.equals(getEncryptedPan(), that.getEncryptedPan()) &&
      Objects.equals(getExpiration(), that.getExpiration()) &&
      Objects.equals(getNameOnCard(), that.getNameOnCard()) &&
      Objects.equals(getProducto(), that.getProducto()) &&
      Objects.equals(getNumeroUnico(), that.getNumeroUnico()) &&
      Objects.equals(getUuid(), that.getUuid()) &&
      Objects.equals(getHashedPan(), that.getHashedPan()) &&
      Objects.equals(getAccountId(), that.getAccountId()) &&
      getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getPan(), getEncryptedPan(), getExpiration(), getNameOnCard(), getStatus());
  }
  @JsonIgnore
  public boolean isActive(){
    return this.getStatus() != null && PrepaidCardStatus.ACTIVE.equals(this.getStatus());
  }
  @JsonIgnore
  public boolean isLocked(){
    return this.getStatus() != null && PrepaidCardStatus.LOCKED.equals(this.getStatus());
  }

}
