package cl.multicaja.prepaid.helpers.mastercard.model;

import java.util.ArrayList;
import java.util.List;

public class AccountingFile {

  private List<AccountingFileDetail> details = new ArrayList<>();

  private boolean hasError;

  public List<AccountingFileDetail> getDetails() {
    return details;
  }

  public void setDetails(List<AccountingFileDetail> details) {
    this.details = details;
  }

  public boolean hasError() {
    return hasError;
  }

  public void setError(boolean hasError) {
    this.hasError = hasError;
  }

}
