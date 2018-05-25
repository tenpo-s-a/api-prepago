package cl.multicaja.prepaid.model.v10;

public enum PrepaidMovementStateType {
  PENDING("PENDING"),
  INPROCESS("INPROCESS"),
  PROCESSOK("PROCESSOK"),
  ERRORINPROCESS("ERRORPROC"),
  PROCESSEDWHITERROR("PROCWERROR");
  private String state;

  PrepaidMovementStateType(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }
}
