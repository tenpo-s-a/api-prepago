package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;


public class SignUPNew extends BaseModel {
	
	public SignUPNew(){
		super();
	}

  public SignUPNew(String email, Integer rut) {
    this.email = email;
    this.rut = rut;
  }

  private String email;
	private Integer rut;
	
	public String getEmail() {
		return email;
	}
	public Integer getRut() {
		return rut;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setRut(Integer rut) {
		this.rut = rut;
	}
}
