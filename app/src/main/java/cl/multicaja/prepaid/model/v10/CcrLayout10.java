package cl.multicaja.prepaid.model.v10;

public class CcrLayout10 {

  public static final String DETAIL_PREFIX = "D";
  public static final String TRAILER_PREFIX = "T";
  public static final String HEADER_PREFIX = "H";

  private Integer start;
  private Integer length;
  private String format;

  public CcrLayout10(Integer start, Integer length, String format) {
    this.start = start;
    this.length = length;
    this.format = format;
  }

  public Integer getStart() {
    return start;
  }

  public Integer getLength() {
    return length;
  }

  public String getFormat() {
    return format;
  }

  public Integer getEnd(){
    return this.getStart() + this.getLength();
  }
}
