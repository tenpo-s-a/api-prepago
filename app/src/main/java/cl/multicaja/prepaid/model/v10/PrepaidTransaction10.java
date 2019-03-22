package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.constants.TipoOrigen;

import java.math.BigDecimal;
import java.util.Date;

public class PrepaidTransaction10 extends BaseModel {


  private Date date;
  private String eCommerceName;
  private String commerceCode;
  private String gloss;
  private String country;
  private TipoFactura invoiceType;
  private boolean corrector;
  private String type;
  private NewAmountAndCurrency10 fee;
  private NewAmountAndCurrency10 amountPrimary;
  private NewAmountAndCurrency10 amountSecondary;
  private NewAmountAndCurrency10 finalAmount;
  private NewAmountAndCurrency10 price;
  private NewAmountAndCurrency10 usdValue;
  private TipoOrigen originType;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  //TODO: debe ser el valor de venta o el valor del día?.
  public NewAmountAndCurrency10 getUsdValue() {
    return usdValue;
  }

  public void setUsdValue(NewAmountAndCurrency10 usdValue) {
    this.usdValue = usdValue;
  }

  public String geteCommerceName() {
    return eCommerceName;
  }

  public void seteCommerceName(String eCommerceName) {
    this.eCommerceName = eCommerceName;
  }

  public String getCommerceCode() {
    return commerceCode;
  }

  public void setCommerceCode(String commerceCode) {
    this.commerceCode = commerceCode;
  }

  public String getGloss() {
    return gloss;
  }

  public void setGloss(String gloss) {
    this.gloss = gloss;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public NewAmountAndCurrency10 getFee() {
    return fee;
  }

  public void setFee(NewAmountAndCurrency10 fee) {
    this.fee = fee;
  }

  public NewAmountAndCurrency10 getAmountPrimary() {
    return amountPrimary;
  }

  public void setAmountPrimary(NewAmountAndCurrency10 amountPrimary) {
    this.amountPrimary = amountPrimary;
  }

  public NewAmountAndCurrency10 getAmountSecondary() {
    return amountSecondary;
  }

  public void setAmountSecondary(NewAmountAndCurrency10 amountSecondary) {
    this.amountSecondary = amountSecondary;
  }

  public NewAmountAndCurrency10 getFinalAmount() {
    return finalAmount;
  }

  public void setFinalAmount(NewAmountAndCurrency10 finalAmount) {
    this.finalAmount = finalAmount;
  }

  public NewAmountAndCurrency10 getPrice() {
    return price;
  }

  public void setPrice(NewAmountAndCurrency10 price) {
    this.price = price;
  }

  public TipoFactura getInvoiceType() {
    return invoiceType;
  }

  public void setInvoiceType(TipoFactura invoiceType) {
    this.invoiceType = invoiceType;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isCorrector() {
    return corrector;
  }

  public void setCorrector(boolean corrector) {
    this.corrector = corrector;
  }

  public TipoOrigen getOriginType() {
    return originType;
  }

  public void setOriginType(TipoOrigen originType) {
    this.originType = originType;
  }
}
