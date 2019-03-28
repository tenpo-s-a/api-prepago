package cl.multicaja.prepaid.model.v11;

import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "prp_tarjeta", schema ="prepago")
public class Card implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String pan;

  @Column(name = "pan_encriptado")
  private String cryptedPan;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado")
  private PrepaidCardStatus status;

  @Column(name = "nombre_tarjeta")
  private String cardName;


  @Column(name = "fecha_creacion")
  private LocalDateTime createdAt;

  @Column(name = "fecha_actualizacion")
  private LocalDateTime updatedAt;

  @Transient
  private String uuid;

  @Column(name = "pan_hash")
  private String panHash;

  @Column(name = "id_cuenta")
  private Long accountId;

  // Campos heredados (Deprecados)
  @Column(name = "id_usuario")
  private Long idUsuario = 0L;

  @Column(name = "numero_unico")
  private String numeroUnico = "";

  private Integer expiracion = 0;

  private String contrato = "";

  private String producto = "";


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

  public String getCryptedPan() {
    return cryptedPan;
  }

  public void setCryptedPan(String cryptedPan) {
    this.cryptedPan = cryptedPan;
  }

  public PrepaidCardStatus getStatus() {
    return status;
  }

  public void setStatus(PrepaidCardStatus status) {
    this.status = status;
  }

  public String getCardName() {
    return cardName;
  }

  public void setCardName(String cardName) {
    this.cardName = cardName;
  }

  public String getProducto() {
    return producto;
  }

  public void setProducto(String producto) {
    this.producto = producto;
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

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getPanHash() {
    return panHash;
  }

  public void setPanHash(String panHash) {
    this.panHash = panHash;
  }

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long accountId) {
    this.accountId = accountId;
  }

  public Long getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(Long idUsuario) {
    this.idUsuario = idUsuario;
  }

  public String getNumeroUnico() {
    return numeroUnico;
  }

  public void setNumeroUnico(String numeroUnico) {
    this.numeroUnico = numeroUnico;
  }

  public Integer getExpiracion() {
    return expiracion;
  }

  public void setExpiracion(Integer expiracion) {
    this.expiracion = expiracion;
  }

  public String getContrato() {
    return contrato;
  }

  public void setContrato(String contrato) {
    this.contrato = contrato;
  }
}
