package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

import java.time.LocalDateTime;

/**
 * @author abarazarte
 */
public class Timestamps10 extends BaseModel {

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Timestamps10() {
    super();
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public String getCreatedAt() {
    return createdAt.toString();
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
  public void setCreatedAt(String createdAt) {
    this.createdAt = LocalDateTime.parse(createdAt);
  }

  public String getUpdatedAt() {
    return updatedAt.toString();
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = LocalDateTime.parse(updatedAt);;
  }

}
