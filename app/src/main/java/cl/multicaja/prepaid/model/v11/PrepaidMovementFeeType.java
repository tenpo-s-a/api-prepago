package cl.multicaja.prepaid.model.v11;

public enum PrepaidMovementFeeType {
  GENERIC_FEE, // Usada en casos en que el origen del fee no esta determinado
  EXCHANGE_RATE_DIF,
  TOPUP_POS_FEE,
  TOPUP_WEB_FEE,
  WITHDRAW_POS_FEE,
  WITHDRAW_WEB_FEE,
  PURCHASE_INT_FEE, // Compras internacionales
  SUSCRIPTION_INT_FEE, // Suscripciones internacionales
  IVA;

  public static PrepaidMovementFeeType valueOfEnum(String name) {
    try {
      return PrepaidMovementFeeType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
