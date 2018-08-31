package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

public class UserPasswordNew extends BaseModel {

  public UserPasswordNew(){
      super();
  }

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
