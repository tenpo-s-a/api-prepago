package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.users.model.v10.Timestamps;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * @author abarazarte
 */
public class PrepaidCard10 extends BaseModel {

  private Long id;
  //Id Interno de prepago
  @JsonIgnore
  private Long idUser;
  //Contrato - Tecnocom
  private String processorUserId;
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

  public PrepaidCard10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdUser() {
    return idUser;
  }

  public void setIdUser(Long idUser) {
    this.idUser = idUser;
  }

  public String getProcessorUserId() {
    return processorUserId;
  }

  public void setProcessorUserId(String processorUserId) {
    this.processorUserId = processorUserId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidCard10)) return false;
    PrepaidCard10 that = (PrepaidCard10) o;
    return Objects.equals(getId(), that.getId()) &&
      Objects.equals(getIdUser(), that.getIdUser()) &&
      Objects.equals(getProcessorUserId(), that.getProcessorUserId()) &&
      Objects.equals(getPan(), that.getPan()) &&
      Objects.equals(getEncryptedPan(), that.getEncryptedPan()) &&
      Objects.equals(getExpiration(), that.getExpiration()) &&
      Objects.equals(getNameOnCard(), that.getNameOnCard()) &&
      Objects.equals(getProducto(), that.getProducto()) &&
      Objects.equals(getNumeroUnico(), that.getNumeroUnico()) &&
      getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getIdUser(), getProcessorUserId(), getPan(), getEncryptedPan(), getExpiration(), getNameOnCard(), getStatus());
  }

  public boolean isActive(){
    return this.getStatus() != null && PrepaidCardStatus.ACTIVE.equals(this.getStatus());
  }
}
