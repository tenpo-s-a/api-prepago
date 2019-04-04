package cl.multicaja.prepaid.model.v11;

import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "prp_usuario", schema ="prepago")
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "id_usuario_mc")
  private Long userId;

  @Column(name = "estado")
  private UserStatus status;

  @Column(name = "nombre")
  private String name;

  @Column(name = "apellido")
  private String lastName;

  @Column(name = "numero_documento")
  private String documentNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_documento")
  private DocumentType documentType;

  @Column(name = "nivel")
  private Integer level;

  @Column(name = "fecha_creacion")
  private LocalDateTime createdAt;

  @Column(name = "fecha_actualizacion")
  private LocalDateTime updatedAt;

  // Campos heredados (Deprecados)
  private Integer rut = 0;

  @Column(name = "saldo_info")
  private String saldo = "";

  @Column(name = "saldo_expiracion")
  private Long saldoExpiracion = 0L;

  @Column(name = "intentos_validacion")
  private Long intentosValidacion = 0L;

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

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
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

  public String getSaldo() {
    return saldo;
  }

  public void setSaldo(String saldo) {
    this.saldo = saldo;
  }

  public Long getSaldoExpiracion() {
    return saldoExpiracion;
  }

  public void setSaldoExpiracion(Long saldoExpiracion) {
    this.saldoExpiracion = saldoExpiracion;
  }

  public Long getIntentosValidacion() {
    return intentosValidacion;
  }

  public void setIntentosValidacion(Long intentosValidacion) {
    this.intentosValidacion = intentosValidacion;
  }


  @Override
  public String toString() {
    return "User{" +
      "id=" + id +
      ", userId=" + userId +
      ", status=" + status +
      ", name='" + name + '\'' +
      ", lastName='" + lastName + '\'' +
      ", documentNumber='" + documentNumber + '\'' +
      ", documentType=" + documentType +
      ", level=" + level +
      ", createdAt=" + createdAt +
      ", updatedAt=" + updatedAt +
      ", rut=" + rut +
      ", saldo='" + saldo + '\'' +
      ", saldoExpiracion=" + saldoExpiracion +
      ", intentosValidacion=" + intentosValidacion +
      '}';
  }
}
