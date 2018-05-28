package cl.multicaja.prepaid.model.v10;

public enum PrepaidMovementStatus {

  PENDING("PENDING"),
  INPROCESS("INPROCESS"),
  PROCESSOK("PROCESSOK"),
  ERRORINPROCESS("ERRORPROC"),
  PROCESSEDWHITERROR("PROCWERROR");

  private String value;

  PrepaidMovementStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
