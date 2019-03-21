package cl.multicaja.prepaid.model.v10;


import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PrepaidMovement10 implements Serializable, Cloneable {

  private Long id;
  private Long idMovimientoRef;
  private String idTxExterno;
  private Long idPrepaidUser;
  private PrepaidMovementType tipoMovimiento;
  private BigDecimal monto;
  private PrepaidMovementStatus estado;
  private Timestamp fechaCreacion;
  private Timestamp fechaActualizacion;

  private String codent;
  private String centalta;
  private String cuenta;
  private CodigoMoneda clamon;
  private IndicadorNormalCorrector indnorcor;
  private TipoFactura tipofac;
  private Date fecfac;
  private String numreffac;
  private String pan;
  private Integer clamondiv;
  private BigDecimal impdiv;
  private BigDecimal impfac;
  private Integer cmbapli;
  private String numaut;
  private IndicadorPropiaAjena indproaje;
  private String codcom;
  private Integer codact;
  private BigDecimal impliq;
  private Integer clamonliq;
  private CodigoPais codpais;
  private String nompob;
  private Integer numextcta;
  private Integer nummovext;
  private Integer clamone;
  private String tipolin;
  private Integer linref;
  private Integer numbencta;
  private Long numplastico;
  private String nomcomred; // Nombre de comercio, Mechant name.

  private BusinessStatusType estadoNegocio;
  private ReconciliationStatusType conSwitch;
  private ReconciliationStatusType conTecnocom;
  private MovementOriginType originType;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdMovimientoRef() {
    return idMovimientoRef;
  }

  public void setIdMovimientoRef(Long idMovimientoRef) {
    this.idMovimientoRef = idMovimientoRef;
  }

  public String getIdTxExterno() {
    return idTxExterno;
  }

  public void setIdTxExterno(String idTxExterno) {
    this.idTxExterno = idTxExterno;
  }

  public Long getIdPrepaidUser() {
    return idPrepaidUser;
  }

  public void setIdPrepaidUser(Long idPrepaidUser) {
    this.idPrepaidUser = idPrepaidUser;
  }

  public PrepaidMovementType getTipoMovimiento() {
    return tipoMovimiento;
  }

  public void setTipoMovimiento(PrepaidMovementType tipoMovimiento) {
    this.tipoMovimiento = tipoMovimiento;
  }

  public BigDecimal getMonto() {
    return monto;
  }

  public void setMonto(BigDecimal monto) {
    this.monto = monto;
  }

  public PrepaidMovementStatus getEstado() {
    return estado;
  }

  public void setEstado(PrepaidMovementStatus estado) {
    this.estado = estado;
  }

  public Timestamp getFechaCreacion() { return fechaCreacion; }

  public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

  public Timestamp getFechaActualizacion() {
    return fechaActualizacion;
  }

  public void setFechaActualizacion(Timestamp fechaActualizacion) {
    this.fechaActualizacion = fechaActualizacion;
  }

  public String getCodent() {
    return codent;
  }

  public void setCodent(String codent) {
    this.codent = codent;
  }

  public String getCentalta() {
    return centalta;
  }

  public void setCentalta(String centalta) {
    this.centalta = centalta;
  }

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }

  public CodigoMoneda getClamon() {
    return clamon;
  }

  public void setClamon(CodigoMoneda clamon) {
    this.clamon = clamon;
  }

  public IndicadorNormalCorrector getIndnorcor() {
    return indnorcor;
  }

  public void setIndnorcor(IndicadorNormalCorrector indnorcor) {
    this.indnorcor = indnorcor;
  }

  public TipoFactura getTipofac() {
    return tipofac;
  }

  public void setTipofac(TipoFactura tipofac) {
    this.tipofac = tipofac;
  }

  public Date getFecfac() {
    return fecfac;
  }

  public void setFecfac(Date fecfac) {
    this.fecfac = fecfac;
  }

  public String getNumreffac() {
    return numreffac;
  }

  public void setNumreffac(String numreffac) {
    this.numreffac = numreffac;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public Integer getClamondiv() {
    return clamondiv;
  }

  public void setClamondiv(Integer clamondiv) {
    this.clamondiv = clamondiv;
  }

  public BigDecimal getImpdiv() {
    return impdiv;
  }

  public void setImpdiv(BigDecimal impdiv) {
    this.impdiv = impdiv;
  }

  public BigDecimal getImpfac() {
    return impfac;
  }

  public void setImpfac(BigDecimal impfac) {
    this.impfac = impfac;
  }

  public Integer getCmbapli() {
    return cmbapli;
  }

  public void setCmbapli(Integer cmbapli) {
    this.cmbapli = cmbapli;
  }

  public String getNumaut() {
    return numaut;
  }

  public void setNumaut(String numaut) {
    this.numaut = numaut;
  }

  public IndicadorPropiaAjena getIndproaje() {
    return indproaje;
  }

  public void setIndproaje(IndicadorPropiaAjena indproaje) {
    this.indproaje = indproaje;
  }

  public String getCodcom() {
    return codcom;
  }

  public void setCodcom(String codcom) {
    this.codcom = StringUtils.leftPad(codcom, 15, '0');
  }

  public Integer getCodact() {
    return codact;
  }

  public void setCodact(Integer codact) {
    this.codact = codact;
  }

  public BigDecimal getImpliq() {
    return impliq;
  }

  public void setImpliq(BigDecimal impliq) {
    this.impliq = impliq;
  }

  public Integer getClamonliq() {
    return clamonliq;
  }

  public void setClamonliq(Integer clamonliq) {
    this.clamonliq = clamonliq;
  }

  public CodigoPais getCodpais() {
    return codpais;
  }

  public void setCodpais(CodigoPais codpais) {
    this.codpais = codpais;
  }

  public String getNompob() {
    return nompob;
  }

  public void setNompob(String nompob) {
    this.nompob = nompob;
  }

  public Integer getNumextcta() {
    return numextcta;
  }

  public void setNumextcta(Integer numextcta) {
    this.numextcta = numextcta;
  }

  public Integer getNummovext() {
    return nummovext;
  }

  public void setNummovext(Integer nummovext) {
    this.nummovext = nummovext;
  }

  public Integer getClamone() {
    return clamone;
  }

  public void setClamone(Integer clamone) {
    this.clamone = clamone;
  }

  public String getTipolin() {
    return tipolin;
  }

  public void setTipolin(String tipolin) {
    this.tipolin = tipolin;
  }

  public Integer getLinref() {
    return linref;
  }

  public void setLinref(Integer linref) {
    this.linref = linref;
  }

  public Integer getNumbencta() {
    return numbencta;
  }

  public void setNumbencta(Integer numbencta) {
    this.numbencta = numbencta;
  }

  public Long getNumplastico() {
    return numplastico;
  }

  public void setNumplastico(Long numplastico) {
    this.numplastico = numplastico;
  }

  public String getNomcomred() {
    return nomcomred;
  }

  public void setNomcomred(String nomcomred) {
    this.nomcomred = nomcomred;
  }

  public ReconciliationStatusType getConSwitch() {
    return conSwitch;
  }

  public void setConSwitch(ReconciliationStatusType conSwitch) {
    this.conSwitch = conSwitch;
  }

  public ReconciliationStatusType getConTecnocom() {
    return conTecnocom;
  }

  public void setConTecnocom(ReconciliationStatusType conTecnocom) {
    this.conTecnocom = conTecnocom;
  }

  public void setEstadoNegocio(BusinessStatusType estadoNegocio) { this.estadoNegocio = estadoNegocio; }

  public BusinessStatusType getEstadoNegocio() { return estadoNegocio; };

  public MovementOriginType getOriginType() {
    return originType;
  }

  public void setOriginType(MovementOriginType originType) {
    this.originType = originType;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PrepaidMovement10)) return false;
    PrepaidMovement10 that = (PrepaidMovement10) o;
    return eql(getId(), that.getId()) &&
      eql(getIdMovimientoRef(), that.getIdMovimientoRef()) &&
      eql(getIdTxExterno(), that.getIdTxExterno()) &&
      eql(getIdPrepaidUser(), that.getIdPrepaidUser()) &&
      getTipoMovimiento() == that.getTipoMovimiento() &&
      eql(getMonto(), that.getMonto()) &&
      getEstado() == that.getEstado() &&
      eql(getEstadoNegocio(), that.getEstadoNegocio()) &&
      eql(getConSwitch(), that.getConSwitch()) &&
      eql(getConTecnocom(), that.getConTecnocom()) &&
      eql(getCodent(), that.getCodent()) &&
      eql(getCentalta(), that.getCentalta()) &&
      eql(getCuenta(), that.getCuenta()) &&
      getClamon() == that.getClamon() &&
      getIndnorcor() == that.getIndnorcor() &&
      eql_tipofac(getTipofac(), that.getTipofac()) &&
      eql_fecfac(getFecfac(), that.getFecfac()) &&
      eql(getNumreffac(), that.getNumreffac()) &&
      eql(getPan(), that.getPan()) &&
      eql(getClamondiv(), that.getClamondiv()) &&
      eql(getImpdiv(), that.getImpdiv()) &&
      eql(getImpfac(), that.getImpfac()) &&
      eql(getCmbapli(), that.getCmbapli()) &&
      eql(getNumaut(), that.getNumaut()) &&
      getIndproaje() == that.getIndproaje() &&
      eql(getCodcom(), that.getCodcom()) &&
      eql(getCodact(), that.getCodact()) &&
      eql(getImpliq(), that.getImpliq()) &&
      eql(getClamonliq(), that.getClamonliq()) &&
      getCodpais() == that.getCodpais() &&
      eql(getNompob(), that.getNompob()) &&
      eql(getNumextcta(), that.getNumextcta()) &&
      eql(getNummovext(), that.getNummovext()) &&
      eql(getClamone(), that.getClamone()) &&
      eql(getTipolin(), that.getTipolin()) &&
      eql(getLinref(), that.getLinref()) &&
      eql(getNumbencta(), that.getNumbencta()) &&
      eql(getNumplastico(), that.getNumplastico()) &&
      eql(getNomcomred(), that.getNomcomred());
  }

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

  /**
   * La fecha factura puede llegar en diferentes formatos, por eso se realiza una comparacion personalizada
   * @param o1
   * @param o2
   * @return
   */
  private boolean eql_fecfac(Date o1, Date o2) {
    boolean eql = Objects.equals(o1, o2);
    if (!eql) {
      if (o1 != null && o2 != null) {
        eql = sdf.format(o1).equals(sdf.format(o2));
      }
    }
    if (!eql) {
      System.out.println("fecfac del movimiento son distintos: " + o1 + " != " + o2);
    }
    return eql;
  }

  /**
   * el tipo factura si es distinto importa el code de el
   *
   * @param o1
   * @param o2
   * @return
   */
  private boolean eql_tipofac(TipoFactura o1, TipoFactura o2) {
    boolean eql = Objects.equals(o1, o2);
    if (!eql) {
      if (o1 != null && o2 != null) {
        eql = o1.getCode() == o2.getCode();
      }
    }
    if (!eql) {
      System.out.println("tipofac del movimiento son distintos: " + o1 + " != " + o2);
    }
    return eql;
  }

  private boolean eql(Object o1, Object o2) {
    boolean eql = Objects.equals(o1, o2);
    if (!eql) {
      System.out.println("Datos del movimiento son distintos: " + o1 + " != " + o2);
    }
    return eql;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getIdMovimientoRef(), getIdTxExterno(), getIdPrepaidUser(), getTipoMovimiento(), getMonto(), getEstado(), getCodent(), getCentalta(), getCuenta(), getClamon(), getIndnorcor(), getTipofac(), getFecfac(), getNumreffac(), getPan(), getClamondiv(), getImpdiv(), getImpfac(), getCmbapli(), getNumaut(), getIndproaje(), getCodcom(), getCodact(), getImpliq(), getClamonliq(), getCodpais(), getNompob(), getNumextcta(), getNummovext(), getClamone(), getTipolin(), getLinref(), getNumbencta(), getNumplastico(), getNomcomred());
  }
}
