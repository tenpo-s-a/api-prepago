package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;

/**
 * @author vutreras
 */
public class PrepaidBalance10 extends BaseModel {

  private NewAmountAndCurrency10 balance;
  private BigDecimal pcaClp;
  private BigDecimal pcaUsd;
  private boolean updated;

  public PrepaidBalance10() {
    super();
  }

  public PrepaidBalance10(NewAmountAndCurrency10 balance, BigDecimal pcaClp, BigDecimal pcaUsd, boolean updated) {
    this.balance = balance;
    this.pcaClp = pcaClp;
    this.pcaUsd = pcaUsd;
    this.updated = updated;
  }

  public NewAmountAndCurrency10 getBalance() {
    return balance;
  }

  public void setBalance(NewAmountAndCurrency10 balance) {
    this.balance = balance;
  }

  public BigDecimal getPcaClp() {
    return pcaClp;
  }

  public void setPcaClp(BigDecimal pcaClp) {
    this.pcaClp = pcaClp;
  }

  public BigDecimal getPcaUsd() {
    return pcaUsd;
  }

  public void setPcaUsd(BigDecimal pcaUsd) {
    this.pcaUsd = pcaUsd;
  }

  public boolean isUpdated() {
    return updated;
  }

  public void setUpdated(boolean updated) {
    this.updated = updated;
  }
}
