package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.helpers.freshdesk.model.v10.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResearchMovementInformationFiles extends BaseModel {

  private @JsonProperty("id_archivo") Long idArchivo;
  private @JsonProperty("tipo_archivo") String tipoArchivo;
  private @JsonProperty("nombre_archivo") String nombreArchivo;
  private @JsonProperty("id_en_archivo") String idEnArchivo;

  public ResearchMovementInformationFiles() {
    super();
  }

  public ResearchMovementInformationFiles(Long idArchivo, String tipoArchivo, String nombreArchivo, String idEnArchivo) {
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
