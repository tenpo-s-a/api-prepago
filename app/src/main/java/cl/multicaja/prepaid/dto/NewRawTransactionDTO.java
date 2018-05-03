package cl.multicaja.prepaid.dto;

/**
 * @author abarazarte
 */
public class NewRawTransactionDTO {

  private NewRawTransactionHeader header;
  private NewRawTransactionBody body;
  private String base64_data;

  public NewRawTransactionHeader getHeader() {
    return header;
  }

  public void setHeader(NewRawTransactionHeader header) {
    this.header = header;
  }

  public NewRawTransactionBody getBody() {
    return body;
  }

  public void setBody(NewRawTransactionBody body) {
    this.body = body;
  }

  public String getBase64_data() {
    return base64_data;
  }

  public void setBase64_data(String base64_data) {
    this.base64_data = base64_data;
  }
}

class NewRawTransactionHeader {
  private String entidad;
  private String centro_alta;
  private String cuenta;
  private String pan;

  public String getEntidad() {
    return entidad;
  }

  public void setEntidad(String entidad) {
    this.entidad = entidad;
  }

  public String getCentro_alta() {
    return centro_alta;
  }

  public void setCentro_alta(String centro_alta) {
    this.centro_alta = centro_alta;
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

class NewRawTransactionBody {

  private Amount saldo_disponible;
  private Amount importe_local;
  private Amount importe_divisa;
  private Integer tipo_tx;
  private Integer id_mensaje;
  private Place lugar;
  private Merchant merchant;
  private Integer resolucion_tx;

  public Amount getSaldo_disponible() {
    return saldo_disponible;
  }

  public void setSaldo_disponible(Amount saldo_disponible) {
    this.saldo_disponible = saldo_disponible;
  }

  public Amount getImporte_local() {
    return importe_local;
  }

  public void setImporte_local(Amount importe_local) {
    this.importe_local = importe_local;
  }

  public Amount getImporte_divisa() {
    return importe_divisa;
  }

  public void setImporte_divisa(Amount importe_divisa) {
    this.importe_divisa = importe_divisa;
  }

  public Integer getTipo_tx() {
    return tipo_tx;
  }

  public void setTipo_tx(Integer tipo_tx) {
    this.tipo_tx = tipo_tx;
  }

  public Integer getId_mensaje() {
    return id_mensaje;
  }

  public void setId_mensaje(Integer id_mensaje) {
    this.id_mensaje = id_mensaje;
  }

  public Place getLugar() {
    return lugar;
  }

  public void setLugar(Place lugar) {
    this.lugar = lugar;
  }

  public Merchant getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant merchant) {
    this.merchant = merchant;
  }

  public Integer getResolucion_tx() {
    return resolucion_tx;
  }

  public void setResolucion_tx(Integer resolucion_tx) {
    this.resolucion_tx = resolucion_tx;
  }
}
