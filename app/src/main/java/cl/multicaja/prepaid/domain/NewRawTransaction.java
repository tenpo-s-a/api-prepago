package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class NewRawTransaction {

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
  private String centroAlta;
  private String cuenta;
  private String pan;

  public String getEntidad() {
    return entidad;
  }

  public void setEntidad(String entidad) {
    this.entidad = entidad;
  }

  public String getcentroAlta() {
    return centroAlta;
  }

  public void setcentroAlta(String centroAlta) {
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

class NewRawTransactionBody {

  private NewAmountAndCurrency saldoDisponible;
  private NewAmountAndCurrency importeLocal;
  private NewAmountAndCurrency importeDivisa;
  private Integer tipoTx;
  private Integer idMensaje;
  private Place lugar;
  private Merchant merchant;
  private Integer resolucionTx;

  public NewAmountAndCurrency getSaldoDisponible() {
    return saldoDisponible;
  }

  public void setSaldoDisponible(NewAmountAndCurrency saldoDisponible) {
    this.saldoDisponible = saldoDisponible;
  }

  public NewAmountAndCurrency getImporteLocal() {
    return importeLocal;
  }

  public void setImporteLocal(NewAmountAndCurrency importeLocal) {
    this.importeLocal = importeLocal;
  }

  public NewAmountAndCurrency getImporteDivisa() {
    return importeDivisa;
  }

  public void setImporteDivisa(NewAmountAndCurrency importeDivisa) {
    this.importeDivisa = importeDivisa;
  }

  public Integer getTipoTx() {
    return tipoTx;
  }

  public void setTipoTx(Integer tipoTx) {
    this.tipoTx = tipoTx;
  }

  public Integer getIdMensaje() {
    return idMensaje;
  }

  public void setIdMensaje(Integer idMensaje) {
    this.idMensaje = idMensaje;
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

  public Integer getResolucionTx() {
    return resolucionTx;
  }

  public void setResolucionTx(Integer resolucionTx) {
    this.resolucionTx = resolucionTx;
  }
}
