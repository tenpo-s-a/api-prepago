package cl.multicaja.prepaid.model.v11;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "prp_cuenta", schema ="prepago")
public class Account implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "id_usuario")
  private Long idUsuario;

  @Transient
  private String uuid;

  private String cuenta;
  private String procesador;

  @Column(name = "saldo_info")
  private String saldoInfo;

  @Column(name = "saldo_expiracion")
  private Long saldoExpiracion;

  private String estado;
  private LocalDateTime creacion;
  private LocalDateTime actualizacion;


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

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }

  public String getProcesador() {
    return procesador;
  }

  public void setProcesador(String procesador) {
    this.procesador = procesador;
  }

  public String getSaldoInfo() {
    return saldoInfo;
  }

  public void setSaldoInfo(String saldoInfo) {
    this.saldoInfo = saldoInfo;
  }

  public Long getSaldoExpiracion() {
    return saldoExpiracion;
  }

  public void setSaldoExpiracion(Long saldoExpiracion) {
    this.saldoExpiracion = saldoExpiracion;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDateTime getCreacion() {
    return creacion;
  }

  public void setCreacion(LocalDateTime creacion) {
    this.creacion = creacion;
  }

  public LocalDateTime getActualizacion() {
    return actualizacion;
  }

  public void setActualizacion(LocalDateTime actualizacion) {
    this.actualizacion = actualizacion;
  }

  public Long getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(Long idUsuario) {
    this.idUsuario = idUsuario;
  }
}
