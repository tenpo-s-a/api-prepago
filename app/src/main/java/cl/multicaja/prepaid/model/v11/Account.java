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
  private Long userId;

  @Transient
  private String uuid;

  @Column(name = "cuenta")
  private String account;

  @Column(name = "procesador")
  private String processor;

  @Column(name = "saldo_info")
  private String balanceInfo;

  @Column(name = "saldo_expiracion")
  private Long expireBalance;

  @Column(name = "estado")
  private String status;

  @Column(name = "creacion")
  private LocalDateTime createdAt;

  @Column(name = "actualizacion")
  private LocalDateTime updatedAt;

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

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getProcessor() {
    return processor;
  }

  public void setProcessor(String processor) {
    this.processor = processor;
  }

  public String getBalanceInfo() {
    return balanceInfo;
  }

  public void setBalanceInfo(String balanceInfo) {
    this.balanceInfo = balanceInfo;
  }

  public Long getExpireBalance() {
    return expireBalance;
  }

  public void setExpireBalance(Long expireBalance) {
    this.expireBalance = expireBalance;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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
}
