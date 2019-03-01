package cl.multicaja.prepaid.model.v10;

import java.sql.Timestamp;

public class ResearchMovement10 {

  private Long id;
  private String idFileOrigin;
  private ReconciliationOriginType origin;
  private String fileName;
  private Timestamp createdAt;
  private Timestamp dateOfTransaction;
  private ResearchMovementResponsibleStatusType responsible;
  private ResearchMovementDescriptionType description;
  private Long movRef;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIdFileOrigin() {
    return idFileOrigin;
  }

  public void setIdFileOrigin(String idFileOrigin) {
    this.idFileOrigin = idFileOrigin;
  }

  public ReconciliationOriginType getOrigin() {
    return origin;
  }

  public void setOrigin(ReconciliationOriginType origin) {
    this.origin = origin;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getDateOfTransaction() {
    return dateOfTransaction;
  }

  public void setDateOfTransaction(Timestamp dateOfTransaction) {
    this.dateOfTransaction = dateOfTransaction;
  }

  public ResearchMovementResponsibleStatusType getResponsible() {
    return responsible;
  }

  public void setResponsible(ResearchMovementResponsibleStatusType responsible) {
    this.responsible = responsible;
  }

  public ResearchMovementDescriptionType getDescription() {
    return description;
  }

  public void setDescription(ResearchMovementDescriptionType description) {
    this.description = description;
  }

  public Long getMovRef() {
    return movRef;
  }

  public void setMovRef(Long movRef) {
    this.movRef = movRef;
  }
}
