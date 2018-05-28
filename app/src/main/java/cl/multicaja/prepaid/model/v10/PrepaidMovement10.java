package cl.multicaja.prepaid.model.v10;


import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class PrepaidMovement10 implements Serializable {

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
  private Long impdiv;
  private BigDecimal impfac;
  private Integer cmbapli;
  private String numaut;
  private IndicadorPropiaAjena indproaje;
  private String codcom;
  private String codact;
  private Long impliq;
  private Integer clamonliq;
  private CodigoPais codpais;
  private String nompob;
  private Integer numextcta;
  private Integer nummovext;
  private CodigoMoneda clamone;
  private String tipolin;
  private Integer linref;
  private Integer numbencta;
  private Long numplastico;

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

  public Timestamp getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(Timestamp fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

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

  public Long getImpdiv() {
    return impdiv;
  }

  public void setImpdiv(Long impdiv) {
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
    this.codcom = codcom;
  }

  public String getCodact() {
    return codact;
  }

  public void setCodact(String codact) {
    this.codact = codact;
  }

  public Long getImpliq() {
    return impliq;
  }

  public void setImpliq(Long impliq) {
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

  public CodigoMoneda getClamone() {
    return clamone;
  }

  public void setClamone(CodigoMoneda clamone) {
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

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
