package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

public class ParamValue extends BaseModel {

  public ParamValue(){
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
