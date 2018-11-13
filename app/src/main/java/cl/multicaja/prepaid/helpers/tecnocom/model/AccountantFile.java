package cl.multicaja.prepaid.helpers.tecnocom.model;

import java.util.ArrayList;
import java.util.List;

public class AccountantFile {

  private List<AccountantFileDetail> details = new ArrayList<>();

  private boolean hasError;

  public List<AccountantFileDetail> getDetails() {
    return details;
  }

  public void setDetails(List<AccountantFileDetail> details) {
    this.details = details;
  }

  public boolean hasError() {
    return hasError;
  }

  public void setError(boolean hasError) {
    this.hasError = hasError;
  }

}
