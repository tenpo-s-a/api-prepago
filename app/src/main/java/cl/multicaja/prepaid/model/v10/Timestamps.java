package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Timestamps extends BaseModel {

	public Timestamps(){
		super();
	}

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

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
