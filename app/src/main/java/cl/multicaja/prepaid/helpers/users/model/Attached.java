package cl.multicaja.prepaid.helpers.users.model;

import cl.multicaja.core.model.BaseModel;

public class Attached extends BaseModel {
  /*
   * El texto del contenido en base64 no debe contener 
   * data:image/jpeg;base64,
   * Solo el cuerpo del archivo
   * **/
  private String contentFile;
  private String mimeType;
  private String fileName;
  
  public String getContentFile() {
    return contentFile;
  }
  public void setContentFile(String contentFile) {
    this.contentFile = contentFile;
  }
  public String getMimeType() {
    return mimeType;
  }
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

}
