package cl.multicaja.prepaid.helpers.tecnocom.model;

import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.TipoFactura;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author abarazarte
 **/
public class ReconciliationFileDetail {

  public static final String FECFACT_DATE_FORMAT = "yyyy-MM-dd";
  public static final String OPERATION_TYPE = "OP";
  public static final String SAT_ORIGIN = "ONLI";

  private String detail;
  private Boolean hasError;
  private String errorDetails;

  private final Set<TipoFactura> reconcilableInvoiceTypes = new HashSet<>(Arrays.asList(TipoFactura.CARGA_TRANSFERENCIA,
    TipoFactura.ANULA_CARGA_TRANSFERENCIA,
    TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA,
    TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA,
    TipoFactura.RETIRO_TRANSFERENCIA,
    TipoFactura.ANULA_RETIRO_TRANSFERENCIA,
    TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA,
    TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA));

  private final ReconciliationFileLayout CODENT = new ReconciliationFileLayout(120, 4, null);
  private final ReconciliationFileLayout CENTALTA = new ReconciliationFileLayout(124, 4, null);
  private final ReconciliationFileLayout CUENTA = new ReconciliationFileLayout(128, 12, null);
  private final ReconciliationFileLayout PAN = new ReconciliationFileLayout(140, 22, null);
  private final ReconciliationFileLayout TIPOREG = new ReconciliationFileLayout(162, 2, null);
  private final ReconciliationFileLayout CLAMON = new ReconciliationFileLayout(176, 3, null);
  private final ReconciliationFileLayout INDNORCOR = new ReconciliationFileLayout(209, 1, null);
  private final ReconciliationFileLayout TIPOFAC = new ReconciliationFileLayout(225, 4, null);
  private final ReconciliationFileLayout FECFAC = new ReconciliationFileLayout(259, 10, null);
  private final ReconciliationFileLayout IMPFAC = new ReconciliationFileLayout(344, 17, 2);
  private final ReconciliationFileLayout NUMAUT = new ReconciliationFileLayout(370, 6, null);
  private final ReconciliationFileLayout CODCOM = new ReconciliationFileLayout(376, 15, null);
  private final ReconciliationFileLayout CODACT = new ReconciliationFileLayout(418, 4, null);
  private final ReconciliationFileLayout CODPAIS = new ReconciliationFileLayout(531, 3, null);
  private final ReconciliationFileLayout ORIGENOPE = new ReconciliationFileLayout(600, 4, null);
  private final ReconciliationFileLayout NUMMOVEXT = new ReconciliationFileLayout(769, 7, null);
  private final ReconciliationFileLayout NUMEXTCTA = new ReconciliationFileLayout(776, 3, null);
  private final ReconciliationFileLayout TIPOLIN = new ReconciliationFileLayout(942, 4, null);
  private final ReconciliationFileLayout LINREF = new ReconciliationFileLayout(988, 8, null);


  public ReconciliationFileDetail() {
  }

  public ReconciliationFileDetail(String detail) {
    this.detail = detail;
  }

  public String getCodent() {
    return this.detail.substring(this.CODENT.getStart(), this.CODENT.getEnd()).trim();
  }

  public String getCentalta() {
    return this.detail.substring(this.CENTALTA.getStart(), this.CENTALTA.getEnd()).trim();
  }

  public String getCuenta() {
    return this.detail.substring(this.CUENTA.getStart(), this.CUENTA.getEnd()).trim();
  }

  public String getContrato() {
    return String.format("%s%s%s", this.getCodent(), this.getCentalta(), this.getCuenta()).trim();
  }

  public String getPan() {
    return this.detail.substring(this.PAN.getStart(), this.PAN.getEnd()).trim();
  }

  public String getTiporeg() {
    return this.detail.substring(this.TIPOREG.getStart(), this.TIPOREG.getEnd());
  }

  public String getClamon() {
    return this.detail.substring(this.CLAMON.getStart(), this.CLAMON.getEnd());
  }

  private String getIndnorcor() {
    return this.detail.substring(this.INDNORCOR.getStart(), this.INDNORCOR.getEnd());
  }

  private String getTipoFactura() {
    return this.detail.substring(this.TIPOFAC.getStart(), this.TIPOFAC.getEnd());
  }

  public TipoFactura getTipoFac() {
    return TipoFactura.valueOfEnumByCodeAndCorrector(NumberUtils.getInstance().toInt(this.getTipoFactura()), NumberUtils.getInstance().toInt(this.getIndnorcor()));
  }

  public String getFecfac() {
    return this.detail.substring(this.FECFAC.getStart(), this.FECFAC.getEnd());
  }

  public String getNumaut() {
    return this.detail.substring(this.NUMAUT.getStart(), this.NUMAUT.getEnd());
  }

  public BigDecimal getImpfac() {
    return this.getScaledValue(this.detail.substring(this.IMPFAC.getStart(), this.IMPFAC.getEnd()), this.IMPFAC.getDecimal());
  }

  public String getCodcom() {
    return this.detail.substring(this.CODCOM.getStart(), this.CODCOM.getEnd());
  }

  public String getCodact() {
    return this.detail.substring(this.CODACT.getStart(), this.CODACT.getEnd());
  }

  public String getCodpais() {
    return this.detail.substring(this.CODPAIS.getStart(), this.CODPAIS.getEnd());
  }

  public String getOrigenope() {
    return this.detail.substring(this.ORIGENOPE.getStart(), this.ORIGENOPE.getEnd());
  }

  public String getNummovext() {
    return this.detail.substring(this.NUMMOVEXT.getStart(), this.NUMMOVEXT.getEnd());
  }

  public String getNumextcta() {
    return this.detail.substring(this.NUMEXTCTA.getStart(), this.NUMEXTCTA.getEnd());
  }

  public String getTipolin() {
    return this.detail.substring(this.TIPOLIN.getStart(), this.TIPOLIN.getEnd());
  }

  public String getLinref() {
    return this.detail.substring(this.LINREF.getStart(), this.LINREF.getEnd());
  }


  public Boolean isFromSat() {
    return SAT_ORIGIN.equals(this.getOrigenope());
  }

  public Boolean isReconcilable() {
    return reconcilableInvoiceTypes.stream().anyMatch(type -> type.equals(this.getTipoFac()));
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
    }

    return type;
  }

  private BigDecimal getScaledValue(String currencyValue, Integer decimal) {
    if(decimal == null){
      decimal = 0;
    }
    Long field = Long.parseLong(currencyValue);
    BigDecimal scaled = new BigDecimal(field).scaleByPowerOfTen(-decimal);
    return scaled;
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

  @Override
  public String toString() {
    return "ReconciliationFileDetail{" +
      "contrato='" + this.getContrato() + '\'' +
      "indnorcor='" + this.getIndnorcor() + '\'' +
      "tipofac='" + this.getTipoFac() + '\'' +
      "desctipofac='" + this.getTipoFac().getDescription() + '\'' +
      "fecfac='" + this.getFecfac() + '\'' +
      "numaut='" + this.getNumaut() + '\'' +
      "impfac='" + this.getImpfac() + '\'' +
      "origenope='" + this.getOrigenope() + '\'' +
      '}';
  }
}
