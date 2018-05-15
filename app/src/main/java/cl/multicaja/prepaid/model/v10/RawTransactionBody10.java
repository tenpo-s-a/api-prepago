package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class RawTransactionBody10 extends BaseModel {

  private NewAmountAndCurrency10 saldoDisponible;
  private NewAmountAndCurrency10 importeLocal;
  private NewAmountAndCurrency10 importeDivisa;
  private Integer tipoTx;
  private Integer idMensaje;
  private Merchant10 merchant;
  private Place10 place;
  private Integer resolucionTx;

  public RawTransactionBody10() {
    super();
  }

  public NewAmountAndCurrency10 getSaldoDisponible() {
    return saldoDisponible;
  }

  public void setSaldoDisponible(NewAmountAndCurrency10 saldoDisponible) {
    this.saldoDisponible = saldoDisponible;
  }

  public NewAmountAndCurrency10 getImporteLocal() {
    return importeLocal;
  }

  public void setImporteLocal(NewAmountAndCurrency10 importeLocal) {
    this.importeLocal = importeLocal;
  }

  public NewAmountAndCurrency10 getImporteDivisa() {
    return importeDivisa;
  }

  public void setImporteDivisa(NewAmountAndCurrency10 importeDivisa) {
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

  public Merchant10 getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant10 merchant) {
    this.merchant = merchant;
  }

  public Place10 getPlace() {
    return place;
  }

  public void setPlace(Place10 place) {
    this.place = place;
  }

  public Integer getResolucionTx() {
    return resolucionTx;
  }

  public void setResolucionTx(Integer resolucionTx) {
    this.resolucionTx = resolucionTx;
  }

}
