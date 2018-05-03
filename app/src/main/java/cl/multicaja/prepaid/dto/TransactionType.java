package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class TransactionType {
  private String type_code;
  private String type_description;
  private Boolean is_ammendment;
  private Boolean is_positive;

  public String getType_code() {
    return type_code;
  }

  public void setType_code(String type_code) {
    this.type_code = type_code;
  }

  public String getType_description() {
    return type_description;
  }

  public void setType_description(String type_description) {
    this.type_description = type_description;
  }

  public Boolean getIs_ammendment() {
    return is_ammendment;
  }

  public void setIs_ammendment(Boolean is_ammendment) {
    this.is_ammendment = is_ammendment;
  }

  public Boolean getIs_positive() {
    return is_positive;
  }

  public void setIs_positive(Boolean is_positive) {
    this.is_positive = is_positive;
  }
}
