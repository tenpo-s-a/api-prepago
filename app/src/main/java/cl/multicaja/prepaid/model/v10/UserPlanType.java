package cl.multicaja.prepaid.model.v10;

public enum UserPlanType {
  FREE,
  PREMIUM;

  public static UserPlanType valueOfEnum(String name) {
    try {
      return UserPlanType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
