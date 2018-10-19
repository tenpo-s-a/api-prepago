package cl.multicaja.prepaid.helpers.freshdesk.model.v10;


import java.io.Serializable;

public class Contact extends BaseModel implements Serializable {

  private Boolean active;
  private String description;
  private String email;
  private Long id;

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
