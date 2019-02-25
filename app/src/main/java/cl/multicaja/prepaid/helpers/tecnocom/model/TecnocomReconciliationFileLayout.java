package cl.multicaja.prepaid.helpers.tecnocom.model;

/**
 * @author abarazarte
 **/
public class TecnocomReconciliationFileLayout {

  public static final String HEADER_PREFIX = "C";
  public static final String DETAIL_PREFIX = "D";
  public static final String FOOTER_PREFIX = "P";

  private Integer start;
  private Integer length;
  private Integer decimal;

  public TecnocomReconciliationFileLayout(Integer start, Integer length, Integer decimal) {
    this.start = start;
    this.length = length;
    this.decimal = decimal;
  }

  public Integer getStart() {
    return start;
  }

  public Integer getLength() {
    return length;
  }

  public Integer getDecimal() {
    return decimal;
  }

  public Integer getEnd(){
    return this.getStart() + this.getLength();
  }
}
