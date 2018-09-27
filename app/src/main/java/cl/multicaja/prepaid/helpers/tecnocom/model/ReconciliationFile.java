package cl.multicaja.prepaid.helpers.tecnocom.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author abarazarte
 **/
public class ReconciliationFile {

  private ReconciliationFileHeader header;
  private ReconciliationFileHeader footer;
  private List<ReconciliationFileDetail> details = new ArrayList<>();
  private boolean isSuspicious = Boolean.FALSE;

  public ReconciliationFileHeader getHeader() {
    return header;
  }

  public void setHeader(ReconciliationFileHeader header) {
    this.header = header;
  }

  public ReconciliationFileHeader getFooter() {
    return footer;
  }

  public void setFooter(ReconciliationFileHeader footer) {
    this.footer = footer;
  }

  public List<ReconciliationFileDetail> getDetails() {
    return details;
  }

  public void setDetails(List<ReconciliationFileDetail> details) {
    this.details = details;
  }

  public boolean isSuspicious() {
    return isSuspicious;
  }

  public void setSuspicious(boolean suspicious) {
    isSuspicious = suspicious;
  }
}
