package cl.multicaja.prepaid.model.v11;

public enum PrepaidMovementFeeType {
  EXCHANGE_RATE_DIF,
  TOPUP_POS_FEE,
  TOPUP_WEB_FEE,
  WITHDRAW_POS_FEE,
  WITHDRAW_WEB_FEE,
  PURCHASE_INTERNATIONAL_FEE,
  SUSCRIPTION_INTERNATIONAL_FEE,
  IVA;

  public static PrepaidMovementFeeType valueOfEnum(String name) {
    try {
      return PrepaidMovementFeeType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
