package cl.multicaja.prepaid.model.v10;

public enum PrepaidUserLevel {
  LEVEL_1,
  LEVEL_2,
  LEVEL_3;

  public static PrepaidUserLevel valueOfEnum(String name) {
    try {
      return PrepaidUserLevel.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
