package cl.multicaja.prepaid.helpers.tecnocom.model;

import cl.multicaja.core.utils.NumberUtils;

/**
 * @author abarazarte
 **/
public class ReconciliationFileHeader {

  public static final String DATE_FORMAT = "yyyy-MM-dd";
  public static final String TIME_FORMAT = "HH.mm.ss";

  private String detail;

  private final ReconciliationFileLayout CODENT = new ReconciliationFileLayout(0, 4, null);
  private final ReconciliationFileLayout NSECFIC = new ReconciliationFileLayout(4, 10, null);
  private final ReconciliationFileLayout TIPOCINTA = new ReconciliationFileLayout(14, 2, null);
  private final ReconciliationFileLayout TIPOREG = new ReconciliationFileLayout(16, 1, null);
  private final ReconciliationFileLayout FECENVIO = new ReconciliationFileLayout(17, 10, null);
  private final ReconciliationFileLayout HORAENVIO = new ReconciliationFileLayout(27, 8, null);
  private final ReconciliationFileLayout NUMREGTOT = new ReconciliationFileLayout(35, 12, 0);

  public ReconciliationFileHeader() {
  }

  public ReconciliationFileHeader(String detail) {
    this.detail = detail;
  }

  public String getCodent() {
    return this.detail.substring(this.CODENT.getStart(), this.CODENT.getEnd());
  }

  public String getNsecfic() {
    return this.detail.substring(this.NSECFIC.getStart(), this.NSECFIC.getEnd());
  }

  public String getTipocinta() {
    return this.detail.substring(this.TIPOCINTA.getStart(), this.TIPOCINTA.getEnd());
  }

  public String getTiporeg() {
    return this.detail.substring(this.TIPOREG.getStart(), this.TIPOREG.getEnd());
  }

  public String getFecenvio() {
    return this.detail.substring(this.FECENVIO.getStart(), this.FECENVIO.getEnd());
  }

  public String getHoraenvio() {
    return this.detail.substring(this.HORAENVIO.getStart(), this.HORAENVIO.getEnd());
  }

  public Integer getNumregtot() {
    return NumberUtils.getInstance().toInteger(this.detail.substring(this.NUMREGTOT.getStart(), this.NUMREGTOT.getEnd()), null);
  }
}
