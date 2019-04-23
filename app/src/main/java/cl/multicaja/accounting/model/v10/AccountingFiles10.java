package cl.multicaja.accounting.model.v10;


import cl.multicaja.prepaid.model.v10.Timestamps;

public class AccountingFiles10 {

  private Long id;
  private String name;
  private String fileId;
  private AccountingFileType fileType;
  private AccountingFileFormatType fileFormatType;
  private String url;
  private AccountingStatusType status;
  private Timestamps timestamps;
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public AccountingFileType getFileType() {
    return fileType;
  }

  public void setFileType(AccountingFileType fileType) {
    this.fileType = fileType;
  }

  public AccountingFileFormatType getFileFormatType() {
    return fileFormatType;
  }

  public void setFileFormatType(AccountingFileFormatType fileFormatType) {
    this.fileFormatType = fileFormatType;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public AccountingStatusType getStatus() {
    return status;
  }

  public void setStatus(AccountingStatusType status) {
    this.status = status;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }
}
