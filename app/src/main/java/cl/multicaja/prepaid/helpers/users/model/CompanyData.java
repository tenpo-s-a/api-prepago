package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

public class CompanyData extends BaseModel {

	public CompanyData(){
		super();
	}

	private String businessName;
	private Integer commercialActivity;
	private CompanyDataStatus status;
	private Timestamps timestamps;
	public String getBusinessName() {
		return businessName;
	}
	public Integer getCommercialActivity() {
		return commercialActivity;
	}
	public CompanyDataStatus getStatus() {
		return status;
	}
	public Timestamps getTimestamps() {
		return timestamps;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public void setCommercialActivity(Integer commercialActivity) {
		this.commercialActivity = commercialActivity;
	}
	public void setStatus(CompanyDataStatus status) {
		this.status = status;
	}
	public void setTimestamps(Timestamps timestamps) {
		this.timestamps = timestamps;
	}



}

