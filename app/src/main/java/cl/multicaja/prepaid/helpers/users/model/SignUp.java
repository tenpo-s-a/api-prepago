package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

public class SignUp extends BaseModel {

	public SignUp(){
		super();
	}

	private String email;
	private Integer rut;
	private Long id;
	private Long userId;
	private String name;
	private String lastname_1;
	private Boolean mustValidateEmail;
	private Boolean mustAcceptTermsAndConditions;
	private Boolean mustChoosePassword;
  private Timestamps timestamps;

	public String getEmail(){
		return this.email;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public Integer getRut(){
		return this.rut;
	}

	public void setRut(Integer rut){
		this.rut = rut;
	}

	public Long getId(){
		return this.id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public Long getUserId(){
		return this.userId;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public String getName(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getLastname_1(){
		return this.lastname_1;
	}

	public void setLastname_1(String lastname_1){
		this.lastname_1 = lastname_1;
	}

	public Boolean getMustValidateEmail(){
		return this.mustValidateEmail;
	}

	public void isMustValidateEmail(Boolean mustValidateEmail){
		this.mustValidateEmail = mustValidateEmail;
	}

	public Boolean getMustAcceptTermsAndConditions(){
		return this.mustAcceptTermsAndConditions;
	}

	public void isMustAcceptTermsAndConditions(Boolean mustAcceptTermsAndConditions){
		this.mustAcceptTermsAndConditions = mustAcceptTermsAndConditions;
	}

	public Boolean getMustChoosePassword(){
		return this.mustChoosePassword;
	}

	public void isMustChoosePassword(Boolean mustChoosePassword){
		this.mustChoosePassword = mustChoosePassword;
	}

	public Timestamps getTimestamps(){
		return this.timestamps;
	}

	public void setTimestamps(Timestamps timestamps){
		this.timestamps = timestamps;
	}
}
