package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.helpers.users.model.Timestamps;

public class ReconciliationFile10 {
  Long id;
  String fileName;
  ReconciliationOriginType process;
  ReconciliationFileType type;
  FileStatus status;
  Timestamps timestamps;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public ReconciliationOriginType getProcess() {
    return process;
  }

  public void setProcess(ReconciliationOriginType process) {
    this.process = process;
  }

  public ReconciliationFileType getType() {
    return type;
  }

  public void setType(ReconciliationFileType type) {
    this.type = type;
  }

  public FileStatus getStatus() {
    return status;
  }

  public void setStatus(FileStatus status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
