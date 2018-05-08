package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class TransactionType {

  private String typeCode;
  private String typeDescription;
  private String isAmmendment;
  private String isPositive;

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

  public String getIsAmmendment() {
    return isAmmendment;
  }

  public void setIsAmmendment(String isAmmendment) {
    this.isAmmendment = isAmmendment;
  }

  public String getIsPositive() {
    return isPositive;
  }

  public void setIsPositive(String isPositive) {
    this.isPositive = isPositive;
  }

}
