package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class PrepaidCardDTO {

  private Integer id;
  private String processor_user_id;
  private String pan;
  private String expiration;
  private String name_on_card;
  private String status;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getProcessor_user_id() {
    return processor_user_id;
  }

  public void setProcessor_user_id(String processor_user_id) {
    this.processor_user_id = processor_user_id;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getExpiration() {
    return expiration;
  }

  public void setExpiration(String expiration) {
    this.expiration = expiration;
  }

  public String getName_on_card() {
    return name_on_card;
  }

  public void setName_on_card(String name_on_card) {
    this.name_on_card = name_on_card;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
