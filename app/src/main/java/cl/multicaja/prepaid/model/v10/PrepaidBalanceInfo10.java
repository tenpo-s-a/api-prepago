package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @autor vutreras
 */
public class PrepaidBalanceInfo10 extends BaseModel {

  private Integer clamonp;
  private Integer clamons;
  private String conprod;
  private String producto;
  private BigDecimal salautconp = new BigDecimal(0l);
  private BigDecimal salautcons = new BigDecimal(0l);
  private BigDecimal saldisconp = new BigDecimal(0l);
  private BigDecimal saldiscons = new BigDecimal(0l);
  private String subprodu;

  public PrepaidBalanceInfo10() {
    super();
  }

  public PrepaidBalanceInfo10(ConsultaSaldoDTO consultaSaldoDTO) {
    super();
    this.setClamonp(consultaSaldoDTO.getClamonp());
    this.setClamons(consultaSaldoDTO.getClamons());
    this.setConprod(consultaSaldoDTO.getConprod());
    this.setProducto(consultaSaldoDTO.getProducto());
    this.setSalautconp(consultaSaldoDTO.getSalautconp());
    this.setSalautcons(consultaSaldoDTO.getSalautcons());
    this.setSaldisconp(consultaSaldoDTO.getSaldisconp());
    this.setSaldiscons(consultaSaldoDTO.getSaldiscons());
    this.setSubprodu(consultaSaldoDTO.getSubprodu());
  }

  public PrepaidBalanceInfo10(Integer clamonp, Integer clamons, BigDecimal salautconp, BigDecimal salautcons, BigDecimal saldisconp, BigDecimal saldiscons) {
    this.clamonp = clamonp;
    this.clamons = clamons;
    this.salautconp = salautconp;
    this.salautcons = salautcons;
    this.saldisconp = saldisconp;
    this.saldiscons = saldiscons;
  }

  public Integer getClamonp() {
    return clamonp;
  }

  public void setClamonp(Integer clamonp) {
    this.clamonp = clamonp;
  }

  public Integer getClamons() {
    return clamons;
  }

  public void setClamons(Integer clamons) {
    this.clamons = clamons;
  }

  public String getConprod() {
    return conprod;
  }

  public void setConprod(String conprod) {
    this.conprod = conprod;
  }

  public String getProducto() {
    return producto;
  }

  public void setProducto(String producto) {
    this.producto = producto;
  }

  public BigDecimal getSalautconp() {
    return salautconp;
  }

  public void setSalautconp(BigDecimal salautconp) {
    this.salautconp = salautconp;
  }

  public BigDecimal getSalautcons() {
    return salautcons;
  }

  public void setSalautcons(BigDecimal salautcons) {
    this.salautcons = salautcons;
  }

  public BigDecimal getSaldisconp() {
    return saldisconp;
  }

  public void setSaldisconp(BigDecimal saldisconp) {
    this.saldisconp = saldisconp;
  }

  public BigDecimal getSaldiscons() {
    return saldiscons;
  }

  public void setSaldiscons(BigDecimal saldiscons) {
    this.saldiscons = saldiscons;
  }

  public String getSubprodu() {
    return subprodu;
  }

  public void setSubprodu(String subprodu) {
    this.subprodu = subprodu;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidBalanceInfo10)) return false;
    PrepaidBalanceInfo10 that = (PrepaidBalanceInfo10) o;
    return Objects.equals(getClamonp(), that.getClamonp()) &&
      Objects.equals(getClamons(), that.getClamons()) &&
      Objects.equals(getConprod(), that.getConprod()) &&
      Objects.equals(getProducto(), that.getProducto()) &&
      Objects.equals(getSalautconp(), that.getSalautconp()) &&
      Objects.equals(getSalautcons(), that.getSalautcons()) &&
      Objects.equals(getSaldisconp(), that.getSaldisconp()) &&
      Objects.equals(getSaldiscons(), that.getSaldiscons()) &&
      Objects.equals(getSubprodu(), that.getSubprodu());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClamonp(), getClamons(), getConprod(), getProducto(), getSalautconp(), getSalautcons(), getSaldisconp(), getSaldiscons(), getSubprodu());
  }
}
