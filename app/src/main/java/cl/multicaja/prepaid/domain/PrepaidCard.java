package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author abarazarte
 */
public class PrepaidCard extends BaseModel {

  private final DateTimeFormatter expirationDateFormatter = DateTimeFormatter.ofPattern("MM/YY");

  private Long id;
  //Id Interno de prepago
  @JsonIgnore
  private Long userId;
  //Contrato - Tecnocom
  private String processorUserId;
  private String pan;
  @JsonIgnore
  private String encryptedPan;
  private LocalDate expiration;
  private String nameOnCard;
  // TODO: manejar el status de la tarjeta como enum?
  private String status;
  private Timestamps timestamps;

  public PrepaidCard() {
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
  public String getExpiration() {
    return expirationDateFormatter.format(this.expiration);
  }

  public void setExpiration(LocalDate expiration) {
    this.expiration = expiration;
  }

  public String getNameOnCard() {
    return nameOnCard;
  }

  public void setNameOnCard(String nameOnCard) {
    this.nameOnCard = nameOnCard;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
