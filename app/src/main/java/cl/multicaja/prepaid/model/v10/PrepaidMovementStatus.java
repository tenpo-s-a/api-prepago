package cl.multicaja.prepaid.model.v10;

public enum PrepaidMovementStatus {

  PENDING("PENDING"),
  IN_PROCESS("IN_PROCESS"),
  PROCESS_OK("PROCESS_OK"),
  ERROR_IN_PROCESS("ERROR_IN_PROCESS"),
  PROCESSED_WITH_ERROR("PROCESSED_WITH_ERROR");

  private String value;

  PrepaidMovementStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
