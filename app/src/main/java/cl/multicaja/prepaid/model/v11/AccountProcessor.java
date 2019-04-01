package cl.multicaja.prepaid.model.v11;

public enum AccountProcessor {
  TECNOCOM_CL;

  public static AccountProcessor valueOfEnum(String name) {
    try {
      return AccountProcessor.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
