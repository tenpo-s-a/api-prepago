package cl.multicaja.prepaid.model.v11;

public enum DocumentType {
  DNI_CL,
  PASSPORT_CL;

  public static DocumentType valueOfEnum(String name) {
    try {
      return DocumentType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
