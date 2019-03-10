package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ResearchMovement10 extends BaseModel {

  private Long id;
  private String filesInfo;
  private ReconciliationOriginType originType;
  private Timestamp createdAt;
  private Timestamp dateOfTransaction;
  private ResearchMovementResponsibleStatusType responsible;
  private ResearchMovementDescriptionType description;
  private BigDecimal movRef;
  private String movementType;
  private String sentStatus;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFilesInfo() {
    return filesInfo;
  }

  public void setFilesInfo(String filesInfo) {
    this.filesInfo = filesInfo;
  }

  public ReconciliationOriginType getOriginType() {
    return originType;
  }

  public void setOriginType(ReconciliationOriginType originType) {
    this.originType = originType;
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

  public BigDecimal getMovRef() {
    return movRef;
  }

  public void setMovRef(BigDecimal movRef) {
    this.movRef = movRef;
  }

  public String getMovementType() {
    return movementType;
  }

  public void setMovementType(String movementType) {
    this.movementType = movementType;
  }

  public String getSentStatus() {
    return sentStatus;
  }

  public void setSentStatus(String sentStatus) {
    this.sentStatus = sentStatus;
  }

  /*private Long id;
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
  */


}
