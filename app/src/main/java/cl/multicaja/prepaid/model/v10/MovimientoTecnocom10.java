package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationRegisterType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.TipoFactura;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public class MovimientoTecnocom10 implements Serializable{

  private Long id;
  private Long idArchivo;
  private String cuenta;
  private String pan;
  private String codEnt;
  private String centAlta;
  private NewAmountAndCurrency10 impFac;
  private Integer indNorCor;
  private TipoFactura tipoFac;
  private LocalDate fecFac;
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
  private Timestamp fecTrn;
  private NewAmountAndCurrency10 impautcon;
  private String contrato;
  // Variables para el proceso.
  private Boolean hasError;
  private String errorDetails;
  private String originOpe;

  private TecnocomReconciliationRegisterType tipoReg;

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
    return tipoFac;
  }

  public void setTipoFac(TipoFactura tipoFac) {
    this.tipoFac = tipoFac;
  }

  public LocalDate getFecFac() {
    return fecFac;
  }

  public void setFecFac(LocalDate fecFac) {
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

  public Timestamp getFecTrn() {
    return fecTrn;
  }

  public void setFecTrn(Timestamp fecTrn) {
    this.fecTrn = fecTrn;
  }

  public NewAmountAndCurrency10 getImpautcon() {
    return impautcon;
  }

  public void setImpautcon(NewAmountAndCurrency10 impautcon) {
    this.impautcon = impautcon;
  }

  public String getOriginOpe() {
    return originOpe;
  }

  public void setOriginOpe(String originOpe) {
    this.originOpe = originOpe;
  }

  public Boolean getHasError() {
    return hasError;
  }

  public void setHasError(Boolean hasError) {
    this.hasError = hasError;
  }

  public String getErrorDetails() {
    return errorDetails;
  }

  public void setErrorDetails(String errorDetails) {
    this.errorDetails = errorDetails;
  }

  public String getContrato() {
    return contrato;
  }

  public void setContrato(String contrato) {
    this.contrato = contrato;
  }

  public TecnocomOperationType getOperationType() {
    TecnocomOperationType operationType;
    switch (tipoFac){
      case COMPRA_INTERNACIONAL:
      case SUSCRIPCION_INTERNACIONAL:
      case ANULA_COMPRA_INTERNACIONAL:
      case ANULA_SUSCRIPCION_INTERNACIONAL:
        operationType = TecnocomOperationType.PURCHASES;
        break;
      default:
        operationType = TecnocomOperationType.REGULAR;
        break;
    }
    return operationType;
  }

  public PrepaidMovementType getMovementType() {
    PrepaidMovementType type = null;
    switch (this.getTipoFac()) {
      case CARGA_EFECTIVO_COMERCIO_MULTICAJA:
      case ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA:
      case CARGA_TRANSFERENCIA:
      case ANULA_CARGA_TRANSFERENCIA:
        type = PrepaidMovementType.TOPUP;
        break;
      case RETIRO_EFECTIVO_COMERCIO_MULTICJA:
      case ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA:
      case RETIRO_TRANSFERENCIA:
      case ANULA_RETIRO_TRANSFERENCIA:
        type = PrepaidMovementType.WITHDRAW;
        break;
      case COMISION_APERTURA:
        type = PrepaidMovementType.ISSUANCE_FEE;
        break;
      case SUSCRIPCION_INTERNACIONAL:
      case ANULA_SUSCRIPCION_INTERNACIONAL:
        type = PrepaidMovementType.SUSCRIPTION;
        break;
      case COMPRA_INTERNACIONAL:
      case ANULA_COMPRA_INTERNACIONAL:
        type = PrepaidMovementType.PURCHASE;
        break;
    }
    return type;
  }

  public TecnocomReconciliationRegisterType getTipoReg() {
    return tipoReg;
  }

  public void setTipoReg(TecnocomReconciliationRegisterType tipoReg) {
    this.tipoReg = tipoReg;
  }

  public String getIdForResearch() {
    return String.format("[Numaut:%s.Date:%s]", getNumAut(), getFecFac().toString());
  }

}
