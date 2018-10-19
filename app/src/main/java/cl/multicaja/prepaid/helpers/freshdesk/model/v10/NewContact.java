package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.io.Serializable;
import java.util.Map;

public class NewContact extends BaseModel implements Serializable {

    private String name;
    private String email;
    private String phone;
    private String mobile;
    private String uniqueExternalId;
    private String description;
    private Map<String,Object> customFields;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getUniqueExternalId() {
    return uniqueExternalId;
  }

  public void setUniqueExternalId(String uniqueExternalId) {
    this.uniqueExternalId = uniqueExternalId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, Object> getCustomFields() {
    return customFields;
  }

  public void setCustomFields(Map<String, Object> customFields) {
    this.customFields = customFields;
  }
}
