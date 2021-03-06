package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.util.Objects;

public class SimulationTopup10 extends BaseModel {

  private NewAmountAndCurrency10 fee;
  private NewAmountAndCurrency10 pca;
  private NewAmountAndCurrency10 eed;
  private NewAmountAndCurrency10 amountToPay;
  private NewAmountAndCurrency10 openingFee;
  private NewAmountAndCurrency10 initialAmount;
  private Integer code;
  private String message;
  private Boolean isFirstTopup;

  public SimulationTopup10(){
    super();
  }

  public SimulationTopup10(NewAmountAndCurrency10 fee, NewAmountAndCurrency10 pca, NewAmountAndCurrency10 eed, NewAmountAndCurrency10 amountToPay, NewAmountAndCurrency10 openingFee) {
    this.fee = fee;
    this.pca = pca;
    this.eed = eed;
    this.amountToPay = amountToPay;
    this.openingFee = openingFee;
  }

  public NewAmountAndCurrency10 getFee() {
    return fee;
  }

  public void setFee(NewAmountAndCurrency10 fee) {
    this.fee = fee;
  }

  public NewAmountAndCurrency10 getPca() {
    return pca;
  }

  public void setPca(NewAmountAndCurrency10 pca) {
    this.pca = pca;
  }

  public NewAmountAndCurrency10 getEed() {
    return eed;
  }

  public void setEed(NewAmountAndCurrency10 eed) {
    this.eed = eed;
  }

  public NewAmountAndCurrency10 getAmountToPay() {
    return amountToPay;
  }

  public void setAmountToPay(NewAmountAndCurrency10 amountToPay) {
    this.amountToPay = amountToPay;
  }

  public NewAmountAndCurrency10 getOpeningFee() {
    return openingFee;
  }

  public void setOpeningFee(NewAmountAndCurrency10 openingFee) {
    this.openingFee = openingFee;
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Boolean getFirstTopup() {
    return isFirstTopup;
  }

  public void setFirstTopup(Boolean firstTopup) {
    isFirstTopup = firstTopup;
  }

  public NewAmountAndCurrency10 getInitialAmount() {
    return initialAmount;
  }

  public void setInitialAmount(NewAmountAndCurrency10 initialAmount) {
    this.initialAmount = initialAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimulationTopup10)) return false;
    SimulationTopup10 that = (SimulationTopup10) o;
    return Objects.equals(getFee(), that.getFee()) &&
      Objects.equals(getPca(), that.getPca()) &&
      Objects.equals(getEed(), that.getEed()) &&
      Objects.equals(getAmountToPay(), that.getAmountToPay()) &&
      Objects.equals(getOpeningFee(), that.getOpeningFee()) &&
      Objects.equals(getInitialAmount(), that.getInitialAmount());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFee(), getPca(), getEed(), getAmountToPay(),getOpeningFee());
  }
}
