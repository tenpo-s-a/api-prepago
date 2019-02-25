package cl.multicaja.prepaid.helpers.tecnocom.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author abarazarte
 **/
public class TecnocomReconciliationFile {

  private TecnocomReconciliationFileHeader header;
  private TecnocomReconciliationFileHeader footer;
  private List<TecnocomReconciliationFileDetail> details = new ArrayList<>();
  private boolean isSuspicious = Boolean.FALSE;

  public TecnocomReconciliationFileHeader getHeader() {
    return header;
  }

  public void setHeader(TecnocomReconciliationFileHeader header) {
    this.header = header;
  }

  public TecnocomReconciliationFileHeader getFooter() {
    return footer;
  }

  public void setFooter(TecnocomReconciliationFileHeader footer) {
    this.footer = footer;
  }

  public List<TecnocomReconciliationFileDetail> getDetails() {
    return details;
  }

  public void setDetails(List<TecnocomReconciliationFileDetail> details) {
    this.details = details;
  }

  public boolean isSuspicious() {
    return isSuspicious;
  }

  public void setSuspicious(boolean suspicious) {
    isSuspicious = suspicious;
  }
}
