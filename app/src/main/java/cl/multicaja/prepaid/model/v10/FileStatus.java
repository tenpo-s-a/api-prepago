package cl.multicaja.prepaid.model.v10;

public enum FileStatus {
  READING,
  OK;

  public static FileStatus valueOfEnum(String name) {
    try {
      return FileStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
