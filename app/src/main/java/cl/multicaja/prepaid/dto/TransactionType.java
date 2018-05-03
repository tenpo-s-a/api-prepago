package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class TransactionType {

  private String typeCode;
  private String typeDescription;
  private Boolean isAmmendment;
  private Boolean isPositive;

  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

  public String getTypeDescription() {
    return typeDescription;
  }

  public void setTypeDescription(String typeDescription) {
    this.typeDescription = typeDescription;
  }

  public Boolean IsAmmendment() {
    return isAmmendment;
  }

  public void setIsAmmendment(Boolean isAmmendment) {
    this.isAmmendment = isAmmendment;
  }

  public Boolean IsPositive() {
    return isPositive;
  }

  public void setIsPositive(Boolean isPositive) {
    this.isPositive = isPositive;
  }
}
