package cl.multicaja.prepaid.model.v10;

import java.sql.Timestamp;

public class ReconciliedMovement10 {

  private Long id;
  private Long idMovRef;
  private ReconciliationStatusType reconciliationStatusType;
  private ReconciliationActionType actionType;
  private Timestamp fechaRegistro;

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

  public Timestamp getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(Timestamp fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }
}
