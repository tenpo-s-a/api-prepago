package cl.multicaja.prepaid.model.v11;

public enum PrepaidMovementFeeType {
  EXCHANGE_RATE_DIF,
  TOPUP;

  public static PrepaidMovementFeeType valueOfEnum(String name) {
    try {
      return PrepaidMovementFeeType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
