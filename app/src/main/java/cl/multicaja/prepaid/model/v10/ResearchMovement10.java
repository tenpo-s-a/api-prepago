package cl.multicaja.prepaid.model.v10;

import java.sql.Timestamp;

public class ResearchMovement10 {

  private Long id;
  private String idRef;
  private String fileName;
  private ReconciliationOriginType origen;
  private Timestamp createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIdRef() {
    return idRef;
  }

  public void setIdRef(String idRef) {
    this.idRef = idRef;
  }

  public String getFileName() { return fileName; }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public ReconciliationOriginType getOrigen() { return origen; }

  public void setOrigen(ReconciliationOriginType origen) {
    this.origen = origen;
  }

  public Timestamp getCreatedAt() { return createdAt; }

  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
