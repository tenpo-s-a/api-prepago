package cl.multicaja.prepaid.model.v10;

public enum ConciliationStatusType {

  PENDIENTE("PENDING"),
  CONCILIADO("CONCILATE"),
  NO_CONCILIADO("NO_CONCILIATE");

  public String getValue() {
    return value;
  }

  String value;

  ConciliationStatusType(String value) {
    this.value = value;
  }
  public static ConciliationStatusType valueOfEnum(String name) {
    try {
      return ConciliationStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
