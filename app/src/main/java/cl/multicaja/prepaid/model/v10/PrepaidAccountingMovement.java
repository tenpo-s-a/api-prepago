package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.sql.Timestamp;

public class PrepaidAccountingMovement extends BaseModel {

  private PrepaidMovement10 prepaidMovement10;
  private Timestamp reconciliationDate;

  public PrepaidAccountingMovement() {
    super();
  }

  public Timestamp getReconciliationDate() {
    return reconciliationDate;
  }

  public void setReconciliationDate(Timestamp reconciliationDate) {
    this.reconciliationDate = reconciliationDate;
  }

  public PrepaidMovement10 getPrepaidMovement10() {
    return prepaidMovement10;
  }

  public void setPrepaidMovement10(PrepaidMovement10 prepaidMovement10) {
    this.prepaidMovement10 = prepaidMovement10;
  }
}
