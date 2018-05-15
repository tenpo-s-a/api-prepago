package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.users.model.v10.Timestamps;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author abarazarte
 */
public class PrepaidCard10 extends BaseModel {

  private Long id;
  //Id Interno de prepago
  @JsonIgnore
  private Long userId;
  //Contrato - Tecnocom
  private String processorUserId;
  private String pan;
  @JsonIgnore
  private String encryptedPan;
  private Integer expiration;
  private String nameOnCard;
  private PrepaidCardStatus status;
  private Timestamps timestamps;

  public PrepaidCard10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
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
}
