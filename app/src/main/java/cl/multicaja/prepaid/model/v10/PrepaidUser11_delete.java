package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.model.v11.UserStatus;
import cl.multicaja.prepaid.model.v11.DocumentType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class PrepaidUser11_delete {

  private Long id;
  private String uuid;
  private Long idUserMc;
  private UserStatus status;
  private String name;
  private String lastName;
  private String documentNumber;
  private DocumentType documentType;
  private String level;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Integer rut = 0;
  private String infoBalance = "";
  private Long expirationBalance = 0L;
  private Long attemptsValidation = 0L;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Long getIdUserMc() {
    return idUserMc;
  }

  public void setIdUserMc(Long idUserMc) {
    this.idUserMc = idUserMc;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
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

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public String getInfoBalance() {
    return infoBalance;
  }

  public void setInfoBalance(String infoBalance) {
    this.infoBalance = infoBalance;
  }

  public Long getExpirationBalance() {
    return expirationBalance;
  }

  public void setExpirationBalance(Long expirationBalance) {
    this.expirationBalance = expirationBalance;
  }

  public Long getAttemptsValidation() {
    return attemptsValidation;
  }

  public void setAttemptsValidation(Long attemptsValidation) {
    this.attemptsValidation = attemptsValidation;
  }

}
