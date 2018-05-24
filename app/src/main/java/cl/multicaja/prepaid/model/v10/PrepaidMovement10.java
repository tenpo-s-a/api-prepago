package cl.multicaja.prepaid.model.v10;

import org.apache.commons.net.ntp.TimeStamp;

import java.io.Serializable;
import java.math.BigDecimal;

public class PrepaidMovement10 implements Serializable {

  private Long id;
  private Long idMovimientoRef;
  private String idTxExterno;
  private Long idUsuario;
  private String tipoMovimiento;
  private BigDecimal monto;
  private String moneda;
  private PrepaidMovementStateType estado;
  private TimeStamp fechaCreacion;
  private TimeStamp fechaActualizacion;
  private String codEntidad;
  private String cenAlta;
  private String cuenta;
  private Integer codMoneda;
  private Integer indNorcor;
  private Integer tipoFactura;
  private TimeStamp fechaFactura;
  private String numFacturaRef;
  private String pan;
  private Integer codMondiv;
  private Long impDiv;
  private Long impFac;
  private Integer cmpApli;
  private String numAutorizacion;
  private String indProaje;
  private String codComercio;
  private String codActividad;
  private Long impLiq;
  private Integer codMonliq;
  private Integer codPais;
  private String nomPoblacion;
  private Integer numExtracto;
  private Integer numMovExtracto;
  private Integer claveMoneda;
  private String tipoLinea;
  private Integer referenciaLinea;
  private Integer numBenefCta;
  private Long numeroPlastico;

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

  public Long getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(Long idUsuario) {
    this.idUsuario = idUsuario;
  }

  public String getTipoMovimiento() {
    return tipoMovimiento;
  }

  public void setTipoMovimiento(String tipoMovimiento) {
    this.tipoMovimiento = tipoMovimiento;
  }

  public BigDecimal getMonto() {
    return monto;
  }

  public void setMonto(BigDecimal monto) {
    this.monto = monto;
  }

  public String getMoneda() {
    return moneda;
  }

  public void setMoneda(String moneda) {
    this.moneda = moneda;
  }

  public PrepaidMovementStateType getEstado() {
    return estado;
  }

  public void setEstado(PrepaidMovementStateType estado) {
    this.estado = estado;
  }

  public TimeStamp getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(TimeStamp fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public TimeStamp getFechaActualizacion() {
    return fechaActualizacion;
  }

  public void setFechaActualizacion(TimeStamp fechaActualizacion) {
    this.fechaActualizacion = fechaActualizacion;
  }

  public String getCodEntidad() {
    return codEntidad;
  }

  public void setCodEntidad(String codEntidad) {
    this.codEntidad = codEntidad;
  }

  public String getCenAlta() {
    return cenAlta;
  }

  public void setCenAlta(String cenAlta) {
    this.cenAlta = cenAlta;
  }

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }

  public Integer getCodMoneda() {
    return codMoneda;
  }

  public void setCodMoneda(Integer codMoneda) {
    this.codMoneda = codMoneda;
  }

  public Integer getIndNorcor() {
    return indNorcor;
  }

  public void setIndNorcor(Integer indNorcor) {
    this.indNorcor = indNorcor;
  }

  public Integer getTipoFactura() {
    return tipoFactura;
  }

  public void setTipoFactura(Integer tipoFactura) {
    this.tipoFactura = tipoFactura;
  }

  public TimeStamp getFechaFactura() {
    return fechaFactura;
  }

  public void setFechaFactura(TimeStamp fechaFactura) {
    this.fechaFactura = fechaFactura;
  }

  public String getNumFacturaRef() {
    return numFacturaRef;
  }

  public void setNumFacturaRef(String numFacturaRef) {
    this.numFacturaRef = numFacturaRef;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public Integer getCodMondiv() {
    return codMondiv;
  }

  public void setCodMondiv(Integer codMondiv) {
    this.codMondiv = codMondiv;
  }

  public Long getImpDiv() {
    return impDiv;
  }

  public void setImpDiv(Long impDiv) {
    this.impDiv = impDiv;
  }

  public Long getImpFac() {
    return impFac;
  }

  public void setImpFac(Long impFac) {
    this.impFac = impFac;
  }

  public Integer getCmpApli() {
    return cmpApli;
  }

  public void setCmpApli(Integer cmpApli) {
    this.cmpApli = cmpApli;
  }

  public String getNumAutorizacion() {
    return numAutorizacion;
  }

  public void setNumAutorizacion(String numAutorizacion) {
    this.numAutorizacion = numAutorizacion;
  }

  public String getIndProaje() {
    return indProaje;
  }

  public void setIndProaje(String indProaje) {
    this.indProaje = indProaje;
  }

  public String getCodComercio() {
    return codComercio;
  }

  public void setCodComercio(String codComercio) {
    this.codComercio = codComercio;
  }

  public String getCodActividad() {
    return codActividad;
  }

  public void setCodActividad(String codActividad) {
    this.codActividad = codActividad;
  }

  public Long getImpLiq() {
    return impLiq;
  }

  public void setImpLiq(Long impLiq) {
    this.impLiq = impLiq;
  }

  public Integer getCodMonliq() {
    return codMonliq;
  }

  public void setCodMonliq(Integer codMonliq) {
    this.codMonliq = codMonliq;
  }

  public Integer getCodPais() {
    return codPais;
  }

  public void setCodPais(Integer codPais) {
    this.codPais = codPais;
  }

  public String getNomPoblacion() {
    return nomPoblacion;
  }

  public void setNomPoblacion(String nomPoblacion) {
    this.nomPoblacion = nomPoblacion;
  }

  public Integer getNumExtracto() {
    return numExtracto;
  }

  public void setNumExtracto(Integer numExtracto) {
    this.numExtracto = numExtracto;
  }

  public Integer getNumMovExtracto() {
    return numMovExtracto;
  }

  public void setNumMovExtracto(Integer numMovExtracto) {
    this.numMovExtracto = numMovExtracto;
  }

  public Integer getClaveMoneda() {
    return claveMoneda;
  }

  public void setClaveMoneda(Integer claveMoneda) {
    this.claveMoneda = claveMoneda;
  }

  public String getTipoLinea() {
    return tipoLinea;
  }

  public void setTipoLinea(String tipoLinea) {
    this.tipoLinea = tipoLinea;
  }

  public Integer getReferenciaLinea() {
    return referenciaLinea;
  }

  public void setReferenciaLinea(Integer referenciaLinea) {
    this.referenciaLinea = referenciaLinea;
  }

  public Integer getNumBenefCta() {
    return numBenefCta;
  }

  public void setNumBenefCta(Integer numBenefCta) {
    this.numBenefCta = numBenefCta;
  }

  public Long getNumeroPlastico() {
    return numeroPlastico;
  }

  public void setNumeroPlastico(Long numeroPlastico) {
    this.numeroPlastico = numeroPlastico;
  }
}
