package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author abarazarte
 */
public class Timestamps10 extends BaseModel {

  private Timestamp createdAt;
  private Timestamp updatedAt;

  public Timestamps10() {
    super();
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }
}
