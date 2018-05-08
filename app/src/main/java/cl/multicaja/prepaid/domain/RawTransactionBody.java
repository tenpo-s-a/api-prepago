package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class RawTransactionBody {

  private NewAmountAndCurrency saldoDisponible;
  private NewAmountAndCurrency importeLocal;
  private NewAmountAndCurrency importeDivisa;
  private Integer tipoTx;
  private Integer idMensaje;
  private Merchant merchant;
  private Place place;
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

  public Merchant getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant merchant) {
    this.merchant = merchant;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(Place place) {
    this.place = place;
  }

  public Integer getResolucionTx() {
    return resolucionTx;
  }

  public void setResolucionTx(Integer resolucionTx) {
    this.resolucionTx = resolucionTx;
  }

}
