package cl.multicaja.prepaid.model.v10;

import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.TipoFactura;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class MovimientoTecnocom10 {

  private Long id;
  private Long idArchivo;
  private String cuenta;
  private String pan;
  private String codEnt;
  private String centAlta;
  private NewAmountAndCurrency10 impFac;
  private Integer indNorCor;
  private TipoFactura TipoFac;
  private Date  fecFac;
  private String numRefFac;
  private NewAmountAndCurrency10 impDiv;
  private BigDecimal cmbApli;
  private String numAut;
  private String indProaje;
  private String codCom;
  private Integer codAct;
  private NewAmountAndCurrency10 impLiq;
  private Integer codPais;
  private String nomPob;
  private Long numExtCta;
  private Long numMovExt;
  private CodigoMoneda clamone;
  private String tipoLin;
  private Integer linRef;
  private Timestamp fechaCreacion;
  private Timestamp fechaActualizacion;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdArchivo() {
    return idArchivo;
  }

  public void setIdArchivo(Long idArchivo) {
    this.idArchivo = idArchivo;
  }

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getCodEnt() {
    return codEnt;
  }

  public void setCodEnt(String codEnt) {
    this.codEnt = codEnt;
  }

  public String getCentAlta() {
    return centAlta;
  }

  public void setCentAlta(String centAlta) {
    this.centAlta = centAlta;
  }

  public NewAmountAndCurrency10 getImpFac() {
    return impFac;
  }

  public void setImpFac(NewAmountAndCurrency10 impFac) {
    this.impFac = impFac;
  }

  public Integer getIndNorCor() {
    return indNorCor;
  }

  public void setIndNorCor(Integer indNorCor) {
    this.indNorCor = indNorCor;
  }

  public TipoFactura getTipoFac() {
    return TipoFac;
  }

  public void setTipoFac(TipoFactura tipoFac) {
    TipoFac = tipoFac;
  }

  public Date getFecFac() {
    return fecFac;
  }

  public void setFecFac(Date fecFac) {
    this.fecFac = fecFac;
  }

  public String getNumRefFac() {
    return numRefFac;
  }

  public void setNumRefFac(String numRefFac) {
    this.numRefFac = numRefFac;
  }

  public NewAmountAndCurrency10 getImpDiv() {
    return impDiv;
  }

  public void setImpDiv(NewAmountAndCurrency10 impDiv) {
    this.impDiv = impDiv;
  }

  public BigDecimal getCmbApli() {
    return cmbApli;
  }

  public void setCmbApli(BigDecimal cmbApli) {
    this.cmbApli = cmbApli;
  }

  public String getNumAut() {
    return numAut;
  }

  public void setNumAut(String numAut) {
    this.numAut = numAut;
  }

  public String getIndProaje() {
    return indProaje;
  }

  public void setIndProaje(String indProaje) {
    this.indProaje = indProaje;
  }

  public String getCodCom() {
    return codCom;
  }

  public void setCodCom(String codCom) {
    this.codCom = codCom;
  }

  public Integer getCodAct() {
    return codAct;
  }

  public void setCodAct(Integer codAct) {
    this.codAct = codAct;
  }

  public NewAmountAndCurrency10 getImpLiq() {
    return impLiq;
  }

  public void setImpLiq(NewAmountAndCurrency10 impLiq) {
    this.impLiq = impLiq;
  }

  public Integer getCodPais() {
    return codPais;
  }

  public void setCodPais(Integer codPais) {
    this.codPais = codPais;
  }

  public String getNomPob() {
    return nomPob;
  }

  public void setNomPob(String nomPob) {
    this.nomPob = nomPob;
  }

  public Long getNumExtCta() {
    return numExtCta;
  }

  public void setNumExtCta(Long numExtCta) {
    this.numExtCta = numExtCta;
  }

  public Long getNumMovExt() {
    return numMovExt;
  }

  public void setNumMovExt(Long numMovExt) {
    this.numMovExt = numMovExt;
  }

  public CodigoMoneda getClamone() {
    return clamone;
  }

  public void setClamone(CodigoMoneda clamone) {
    this.clamone = clamone;
  }

  public String getTipoLin() {
    return tipoLin;
  }

  public void setTipoLin(String tipoLin) {
    this.tipoLin = tipoLin;
  }

  public Integer getLinRef() {
    return linRef;
  }

  public void setLinRef(Integer linRef) {
    this.linRef = linRef;
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
}
