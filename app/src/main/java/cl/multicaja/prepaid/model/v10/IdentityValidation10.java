package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 **/
public class IdentityValidation10 extends BaseModel {

  private String isCiValid;
  private String userPhotoMatchesCi;
  private String nameAndLastnameMatchesCi;
  private String newName;
  private String newLastname;
  private String isGsintelOk;
  private String rutMatchesCi;

  public IdentityValidation10() {
    super();
  }

  public String getIsCiValid() {
    return isCiValid;
  }

  public void setIsCiValid(String isCiValid) {
    this.isCiValid = isCiValid;
  }

  public String getUserPhotoMatchesCi() {
    return userPhotoMatchesCi;
  }

  public void setUserPhotoMatchesCi(String userPhotoMatchesCi) {
    this.userPhotoMatchesCi = userPhotoMatchesCi;
  }

  public String getNameAndLastnameMatchesCi() {
    return nameAndLastnameMatchesCi;
  }

  public void setNameAndLastnameMatchesCi(String nameAndLastnameMatchesCi) {
    this.nameAndLastnameMatchesCi = nameAndLastnameMatchesCi;
  }

  public String getNewName() {
    return newName;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public String getNewLastname() {
    return newLastname;
  }

  public void setNewLastname(String newLastname) {
    this.newLastname = newLastname;
  }

  public String getIsGsintelOk() {
    return isGsintelOk;
  }

  public void setIsGsintelOk(String isGsintelOk) {
    this.isGsintelOk = isGsintelOk;
  }

  public String getRutMatchesCi() {
    return rutMatchesCi;
  }

  public void setRutMatchesCi(String rutMatchesCi) {
    this.rutMatchesCi = rutMatchesCi;
  }
}
