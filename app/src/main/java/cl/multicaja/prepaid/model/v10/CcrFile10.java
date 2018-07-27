package cl.multicaja.prepaid.model.v10;

public class CcrFile10 {
  private CcrHeaderRecord10 ccrHeaderRecord10 = new CcrHeaderRecord10();
  private CcrDetailRecord10 ccrDetailRecord10 = new CcrDetailRecord10();
  private CcrTrailerRecord10 ccrTrailerRecord10 = new CcrTrailerRecord10();

  public CcrHeaderRecord10 getCcrHeaderRecord10() {
    return ccrHeaderRecord10;
  }

  public void setCcrHeaderRecord10(CcrHeaderRecord10 ccrHeaderRecord10) {
    this.ccrHeaderRecord10 = ccrHeaderRecord10;
  }

  public CcrDetailRecord10 getCcrDetailRecord10() {
    return ccrDetailRecord10;
  }

  public void setCcrDetailRecord10(CcrDetailRecord10 ccrDetailRecord10) {
    this.ccrDetailRecord10 = ccrDetailRecord10;
  }

  public CcrTrailerRecord10 getCcrTrailerRecord10() {
    return ccrTrailerRecord10;
  }

  public void setCcrTrailerRecord10(CcrTrailerRecord10 ccrTrailerRecord10) {
    this.ccrTrailerRecord10 = ccrTrailerRecord10;
  }

}
