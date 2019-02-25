package cl.multicaja.prepaid.model.v10;

public enum ReconciliationFileType {
  TECNOCOM_FILE,
  SWITCH_TOPUP,
  SWITCH_REJECTED_TOPUP,
  SWITCH_REVERSED_TOPUP,
  SWITCH_WITHDRAW,
  SWITCH_REJECTED_WITHDRAW,
  SWITCH_REVERSED_WITHDRAW;

  public static ReconciliationFileType valueOfEnum(String name) {
    try {
      return ReconciliationFileType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
