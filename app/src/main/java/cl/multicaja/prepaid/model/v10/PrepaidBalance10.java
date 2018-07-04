package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author vutreras
 */
public class PrepaidBalance10 extends BaseModel {

  private NewAmountAndCurrency10 balance;
  private NewAmountAndCurrency10 pcaMain;
  private NewAmountAndCurrency10 pcaSecondary;
  private Integer usdValue;
  private Double usdMultiplier;
  private boolean updated;

  public PrepaidBalance10() {
    super();
  }

  public PrepaidBalance10(NewAmountAndCurrency10 balance, NewAmountAndCurrency10 pcaMain, NewAmountAndCurrency10 pcaSecondary, Integer usdValue, Double usdMultiplier, boolean updated) {
    this.balance = balance;
    this.pcaMain = pcaMain;
    this.pcaSecondary = pcaSecondary;
    this.usdValue = usdValue;
    this.usdMultiplier = usdMultiplier;
    this.updated = updated;
  }

  public NewAmountAndCurrency10 getBalance() {
    return balance;
  }

  public void setBalance(NewAmountAndCurrency10 balance) {
    this.balance = balance;
  }

  public NewAmountAndCurrency10 getPcaMain() {
    return pcaMain;
  }

  public void setPcaMain(NewAmountAndCurrency10 pcaMain) {
    this.pcaMain = pcaMain;
  }

  public NewAmountAndCurrency10 getPcaSecondary() {
    return pcaSecondary;
  }

  public void setPcaSecondary(NewAmountAndCurrency10 pcaSecondary) {
    this.pcaSecondary = pcaSecondary;
  }

  public Integer getUsdValue() {
    return usdValue;
  }

  public void setUsdValue(Integer usdValue) {
    this.usdValue = usdValue;
  }

  public Double getUsdMultiplier() {
    return usdMultiplier;
  }

  public void setUsdMultiplier(Double usdMultiplier) {
    this.usdMultiplier = usdMultiplier;
  }

  public boolean isUpdated() {
    return updated;
  }

  public void setUpdated(boolean updated) {
    this.updated = updated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidBalance10)) return false;
    PrepaidBalance10 that = (PrepaidBalance10) o;
    return isUpdated() == that.isUpdated() &&
      Objects.equals(getBalance(), that.getBalance()) &&
      Objects.equals(getPcaMain(), that.getPcaMain()) &&
      Objects.equals(getPcaSecondary(), that.getPcaSecondary());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getBalance(), getPcaMain(), getPcaSecondary(), isUpdated());
  }
}
