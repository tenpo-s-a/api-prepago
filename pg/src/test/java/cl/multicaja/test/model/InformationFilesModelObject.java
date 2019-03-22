package cl.multicaja.test.model;

import cl.multicaja.core.model.BaseModel;

public class InformationFilesModelObject  extends BaseModel {

  private Long idArchivo;
  private String tipoArchivo;
  private String nombreArchivo;
  private String idEnArchivo;

  public InformationFilesModelObject() {
    super();
  }

  public InformationFilesModelObject(Long idArchivo, String tipoArchivo, String nombreArchivo, String idEnArchivo) {
    this.idArchivo = idArchivo;
    this.tipoArchivo = tipoArchivo;
    this.nombreArchivo = nombreArchivo;
    this.idEnArchivo = idEnArchivo;
  }

  public Long getIdArchivo() {
    return idArchivo;
  }

  public void setIdArchivo(Long idArchivo) {
    this.idArchivo = idArchivo;
  }

  public String getTipoArchivo() {
    return tipoArchivo;
  }

  public void setTipoArchivo(String tipoArchivo) {
    this.tipoArchivo = tipoArchivo;
  }

  public String getNombreArchivo() {
    return nombreArchivo;
  }

  public void setNombreArchivo(String nombreArchivo) {
    this.nombreArchivo = nombreArchivo;
  }

  public String getIdEnArchivo() {
    return idEnArchivo;
  }

  public void setIdEnArchivo(String idEnArchivo) {
    this.idEnArchivo = idEnArchivo;
  }
}
