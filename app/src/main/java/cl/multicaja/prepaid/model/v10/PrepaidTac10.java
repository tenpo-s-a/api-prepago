package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 **/
public class PrepaidTac10 extends BaseModel {

  private String version;
  private String location;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }
}
