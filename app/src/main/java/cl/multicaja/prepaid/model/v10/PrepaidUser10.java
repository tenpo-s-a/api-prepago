package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.users.model.v10.Timestamps;

/**
 * @author abarazarte
 */
public class PrepaidUser10 extends BaseModel {

  private Long id;
  private Long idUser;
  private Integer rut;
  private PrepaidUserStatus status;
  private Timestamps timestamps;

  public PrepaidUser10() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdUser() {
    return idUser;
  }

  public void setIdUser(Long idUser) {
    this.idUser = idUser;
  }

  public Integer getRut() {
    return rut;
  }

  public void setRut(Integer rut) {
    this.rut = rut;
  }

  public PrepaidUserStatus getStatus() {
    return status;
  }

  public void setStatus(PrepaidUserStatus status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
