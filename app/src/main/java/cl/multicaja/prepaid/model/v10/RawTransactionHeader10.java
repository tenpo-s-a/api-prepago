package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class RawTransactionHeader10 extends BaseModel {

  private String endidad;
  private String centroAlta;
  private String cuenta;
  private String pan;

  public RawTransactionHeader10() {
    super();
  }

  public String getEndidad() {
    return endidad;
  }

  public void setEndidad(String endidad) {
    this.endidad = endidad;
  }

  public String getCentroAlta() {
    return centroAlta;
  }

  public void setCentroAlta(String centroAlta) {
    this.centroAlta = centroAlta;
  }

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

}
