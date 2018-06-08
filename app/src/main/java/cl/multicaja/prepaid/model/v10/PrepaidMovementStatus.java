package cl.multicaja.prepaid.model.v10;

public enum PrepaidMovementStatus {

  PENDING,
  IN_PROCESS,
  PROCESS_OK,
  ERROR_IN_PROCESS_PENDING_TOPUP,
  ERROR_IN_PROCESS_CREATE_CARD,
  ERROR_IN_PROCESS_EMISSION_CARD,
  ERROR_IN_PROCESS_CARD_ISSUANCE_FEE,
  ERROR_POS_WITHDRAW,
  ERROR_WEB_WITHDRAW,
  REVERSED;

  public static PrepaidMovementStatus valueOfEnum(String name) {
    try {
      return PrepaidMovementStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
