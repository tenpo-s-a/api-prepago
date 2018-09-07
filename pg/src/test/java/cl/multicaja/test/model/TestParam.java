package cl.multicaja.test.model;

import cl.multicaja.core.model.BaseModel;

public class TestParam extends BaseModel {

  private String type;
  private String value;

  public TestParam() {
    super();
  }

  public TestParam(String type, String value) {
    this.type = type;
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
