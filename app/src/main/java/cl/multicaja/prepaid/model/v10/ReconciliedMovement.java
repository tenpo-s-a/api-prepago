package cl.multicaja.prepaid.model.v10;

import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;

import java.sql.Timestamp;
import java.util.Map;

public class ReconciliedMovement {

  private Long id;
  private Long idMovRef;
  private ReconciliationStatusType reconciliationStatusType;
  private ReconciliationActionType actionType;

  private Timestamp createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdMovRef() {
    return idMovRef;
  }

  public void setIdMovRef(Long idMovRef) {
    this.idMovRef = idMovRef;
  }

  public ReconciliationStatusType getReconciliationStatusType() {
    return reconciliationStatusType;
  }

  public void setReconciliationStatusType(ReconciliationStatusType reconciliationStatusType) {
    this.reconciliationStatusType = reconciliationStatusType;
  }

  public ReconciliationActionType getActionType() {
    return actionType;
  }

  public void setActionType(ReconciliationActionType actionType) {
    this.actionType = actionType;
  }

  public Timestamp getCreatedAt() { return createdAt; }

  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
