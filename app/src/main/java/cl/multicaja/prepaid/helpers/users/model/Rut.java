package cl.multicaja.prepaid.helpers.users.model;


import cl.multicaja.core.model.BaseModel;

public class Rut extends BaseModel {

	public Rut() {
		super();
	}

  public Rut(Integer value) {
    this.value = value;
  }

  private Integer value;
	private String dv;
	private RutStatus status;
	private String serialNumber;
	private Timestamps timestamps;
	public Integer getValue() {
		return value;
	}
	public String getDv() {
		return dv;
	}
	public RutStatus getStatus() {
		return status;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public Timestamps getTimestamps() {
		return timestamps;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	public void setDv(String dv) {
		this.dv = dv;
	}
	public void setStatus(RutStatus status) {
		this.status = status;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public void setTimestamps(Timestamps timestamps) {
		this.timestamps = timestamps;
	}


}
