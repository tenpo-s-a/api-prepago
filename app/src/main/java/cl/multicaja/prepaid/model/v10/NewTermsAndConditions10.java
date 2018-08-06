package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 **/
public class NewTermsAndConditions10 extends BaseModel {

  private String version;
  private Boolean benefitsAccepted;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getBenefitsAccepted() {
    return benefitsAccepted;
  }

  public void setBenefitsAccepted(Boolean benefitsAccepted) {
    this.benefitsAccepted = benefitsAccepted;
  }
}
