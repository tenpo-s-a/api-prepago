package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

public class Email extends BaseModel {

	public Email(){
		super();
	}

	private String value;
	private EmailStatus status;
	private Timestamps timestamps;

	public String getValue() {
		return value;
	}
	public EmailStatus getStatus() {
		return status;
	}
	public Timestamps getTimestamps() {
		return timestamps;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setStatus(EmailStatus status) {
		this.status = status;
	}
	public void setTimestamps(Timestamps timestamps) {
		this.timestamps = timestamps;
	}


}
