package cl.multicaja.prepaid.model.v10;

public class ReconciliedMovement {

  private Long id;
  private Long idMovRef;
  private ReconciliationStatusType reconciliationStatusType;
  private ReconciliationActionType actionType;
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
}
