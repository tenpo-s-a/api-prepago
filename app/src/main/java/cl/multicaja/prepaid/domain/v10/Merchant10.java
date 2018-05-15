package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class Merchant10 extends BaseModel {

  private String code;
  private String name;

  public Merchant10() {
    super();
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
