package cl.multicaja.prepaid.model.v10;

public enum MimeType {

  MS_WORD("application/msword"),
  HTML("text/html"),
  JPG("image/jpeg"),
  JSON("application/json"),
  PDF("application/pdf"),
  PPT("application/vnd.ms-powerpoint"),
  RAR("application/x-rar-compressed"),
  XLS("application/vnd.ms-excel"),
  XML("application/xml"),
  ZIP("application/zip"),
  CSV("text/csv");

  MimeType(String value) {
    this.value = value;
  }
  String value;
  public String getValue() {
    return value;
  }
}
