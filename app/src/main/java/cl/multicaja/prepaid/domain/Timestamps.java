package cl.multicaja.prepaid.domain;

import cl.multicaja.core.model.BaseModel;

import java.time.LocalDateTime;

/**
 * @author abarazarte
 */
public class Timestamps extends BaseModel {

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Timestamps() {
    super();
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
