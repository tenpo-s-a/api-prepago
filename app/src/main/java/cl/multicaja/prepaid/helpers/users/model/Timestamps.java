package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

import java.sql.Timestamp;

public class Timestamps extends BaseModel {

	public Timestamps(){
		super();
	}

	private Timestamp createdAt;
	private Timestamp updatedAt;

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
