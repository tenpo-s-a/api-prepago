package cl.multicaja.prepaid.model.v10;

/**
 * @autor vutreras
 */
public enum PrepaidMovementType {
  ISSUANCE_FEE,
  TOPUP,
  WITHDRAW,
  SUSCRIPTION,
  PURCHASE,
  REFUND;

  public static PrepaidMovementType valueOfEnum(String name) {
    try {
      return PrepaidMovementType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
