package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.helpers.users.model.Timestamps;

import java.util.List;

/**
 * @author abarazarte
 */
public class PrepaidUserSignup10 extends NewPrepaidUserSignup10 {

  private Long id;
  private Long userId;
  private String name;
  private String lastname_1;
  private Integer cellphone;
  private List<String> termsAndConditionsList;
  private Boolean mustValidateEmail;
  private Boolean mustValidateCellphone;
  private Boolean mustAcceptTermsAndConditions;
  private Boolean mustChoosePassword;
  private Timestamps timestamps;

  public PrepaidUserSignup10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLastname_1() {
    return lastname_1;
  }

  public void setLastname_1(String lastname_1) {
    this.lastname_1 = lastname_1;
  }

  public Integer getCellphone() {
    return cellphone;
  }

  public void setCellphone(Integer cellphone) {
    this.cellphone = cellphone;
  }

  public List<String> getTermsAndConditionsList() {
    return termsAndConditionsList;
  }

  public void setTermsAndConditionsList(List<String> termsAndConditionsList) {
    this.termsAndConditionsList = termsAndConditionsList;
  }

  public Boolean getMustValidateEmail() {
    return mustValidateEmail;
  }

  public void setMustValidateEmail(Boolean mustValidateEmail) {
    this.mustValidateEmail = mustValidateEmail;
  }

  public Boolean getMustValidateCellphone() {
    return mustValidateCellphone;
  }

  public void setMustValidateCellphone(Boolean mustValidateCellphone) {
    this.mustValidateCellphone = mustValidateCellphone;
  }

  public Boolean getMustAcceptTermsAndConditions() {
    return mustAcceptTermsAndConditions;
  }

  public void setMustAcceptTermsAndConditions(Boolean mustAcceptTermsAndConditions) {
    this.mustAcceptTermsAndConditions = mustAcceptTermsAndConditions;
  }

  public Boolean getMustChoosePassword() {
    return mustChoosePassword;
  }

  public void setMustChoosePassword(Boolean mustChoosePassword) {
    this.mustChoosePassword = mustChoosePassword;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

}
