package cl.multicaja.prepaid.model.v11;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class Account implements Serializable {

  private Long id;
  private Long userId;
  private String uuid;
  private String accountNumber; // Numero de contrato en procesadora
  private String processor;
  private String balanceInfo;
  private Long expireBalance;
  private AccountStatus status;
  private LocalDateTime createdAt;
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

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
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

  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus status) {
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

  @PrePersist
  public void beforePersist() {
    this.setUuid(UUID.randomUUID().toString());
    this.setCreatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
    this.setUpdatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
  }

  @PreUpdate
  public void beforeUpdate() {
    this.setUpdatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
  }
}
