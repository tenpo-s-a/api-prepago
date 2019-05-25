package cl.multicaja.prepaid.model.v11;

public enum IvaType {
  IVA_INCLUDED,
  PLUS_IVA;

  public static IvaType valueOfEnum(String name) {
    try {
      return IvaType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
