package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

public class CellPhone extends BaseModel {

	public CellPhone() {
		super();
	}

	private Long value;
	private CellphoneStatus status;
	private Timestamps timestamps;

	public Long getValue() {
		return value;
	}
	public CellphoneStatus getStatus() {
		return status;
	}
	public Timestamps getTimestamps() {
		return timestamps;
	}
	public void setValue(Long value) {
		this.value = value;
	}
	public void setStatus(CellphoneStatus status) {
		this.status = status;
	}
	public void setTimestamps(Timestamps timestamps) {
		this.timestamps = timestamps;
	}

}
