package cl.multicaja.prepaid.helpers.tecnocom.model;

import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.prepaid.model.v10.TecnocomOperationType;
import cl.multicaja.tecnocom.constants.CodigoPais;
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
  private static final String SAT_ORIGIN = "ONLI";

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

  //Datos Comunes
  private final ReconciliationFileLayout CODENT = new ReconciliationFileLayout(120, 4, null);
  private final ReconciliationFileLayout CENTALTA = new ReconciliationFileLayout(124, 4, null);
  private final ReconciliationFileLayout CUENTA = new ReconciliationFileLayout(128, 12, null);
  private final ReconciliationFileLayout PAN = new ReconciliationFileLayout(140, 22, null);
  private final ReconciliationFileLayout TIPOREG = new ReconciliationFileLayout(162, 2, null);

  //DATOS OP
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
  private final ReconciliationFileLayout NOMCOMRED = new ReconciliationFileLayout(2094, 27, null);

  //DATOS AU
  private final ReconciliationFileLayout CLAMON_AU = new ReconciliationFileLayout(176, 3, null);
  private final ReconciliationFileLayout INDNORCOR_AU = new ReconciliationFileLayout(209, 1, null);
  private final ReconciliationFileLayout TIPOFAC_AU = new ReconciliationFileLayout(240, 4, null);
  private final ReconciliationFileLayout FECTRN_AU = new ReconciliationFileLayout(274, 10, null);
  private final ReconciliationFileLayout HORTRN_AU = new ReconciliationFileLayout(284, 8, null);
  private final ReconciliationFileLayout CLAMONDIV_AU = new ReconciliationFileLayout(292, 3, null);
  private final ReconciliationFileLayout SIGNODIV_AU = new ReconciliationFileLayout(325, 1, null);
  private final ReconciliationFileLayout IMPDIV_AU = new ReconciliationFileLayout(326, 17, 2);
  private final ReconciliationFileLayout SIGNOAUT_AU = new ReconciliationFileLayout(343, 1, null);
  private final ReconciliationFileLayout IMPAUTCON_AU = new ReconciliationFileLayout(344, 17, 2);
  private final ReconciliationFileLayout CMBAPLI_AU = new ReconciliationFileLayout(361, 9, 4);
  private final ReconciliationFileLayout NUMAUT_AU = new ReconciliationFileLayout(370, 6, null);
  private final ReconciliationFileLayout CODCOM_AU = new ReconciliationFileLayout(376, 15, null);
  private final ReconciliationFileLayout NOMCOMRED_AU = new ReconciliationFileLayout(391, 27, null);
  private final ReconciliationFileLayout CODACT_AU = new ReconciliationFileLayout(418, 4, null);



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
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.CLAMON.getStart(), this.CLAMON.getEnd());
    } else if (TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.CLAMON_AU.getStart(), this.CLAMON_AU.getEnd());
    } else{
      return "";
    }
  }

  private String getIndnorcor() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.INDNORCOR.getStart(), this.INDNORCOR.getEnd());
    } else if (TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.INDNORCOR_AU.getStart(), this.INDNORCOR_AU.getEnd());
    } else{
      return "";
    }
  }

  private String getTipoFactura() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.TIPOFAC.getStart(), this.TIPOFAC.getEnd());
    } else if (TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.TIPOFAC_AU.getStart(), this.TIPOFAC_AU.getEnd());
    } else{
      return "";
    }
  }

  public TipoFactura getTipoFac() {
    return TipoFactura.valueOfEnumByCodeAndCorrector(NumberUtils.getInstance().toInt(this.getTipoFactura()), NumberUtils.getInstance().toInt(this.getIndnorcor()));
  }
  public TecnocomOperationType getOperationType(){
    return TecnocomOperationType.fromValue(this.getTiporeg());
  }

  public String getFecfac() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())) {
      return this.detail.substring(this.FECFAC.getStart(), this.FECFAC.getEnd());
    } else {
      return "";
    }

  }

  public String getNumaut() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.NUMAUT.getStart(), this.NUMAUT.getEnd());
    } else if (TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.NUMAUT_AU.getStart(), this.NUMAUT_AU.getEnd());
    } else{
      return "";
    }
  }

  public BigDecimal getImpfac() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.getScaledValue(this.detail.substring(this.IMPFAC.getStart(), this.IMPFAC.getEnd()), this.IMPFAC.getDecimal());
    } else{
      return BigDecimal.ZERO;
    }
  }

  public String getCodcom() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.CODCOM.getStart(), this.CODCOM.getEnd());
    } else if (TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.CODCOM_AU.getStart(), this.CODCOM_AU.getEnd());
    } else{
      return "";
    }
  }

  public String getCodact() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.CODACT.getStart(), this.CODACT.getEnd());
    } else if (TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.CODACT_AU.getStart(), this.CODACT_AU.getEnd());
    } else{
      return "";
    }
  }


  public String getCmbApli() {
    if(TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.CMBAPLI_AU.getStart(), this.CMBAPLI_AU.getEnd());
    } else{
      return "";
    }
  }

  public String getCodpais() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.CODPAIS.getStart(), this.CODPAIS.getEnd());
    } else{
      return CodigoPais.CHILE.getValue().toString();
    }
  }

  public String getOrigenope() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.ORIGENOPE.getStart(), this.ORIGENOPE.getEnd());
    } else{
      return "";
    }
  }

  public String getNummovext() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.NUMMOVEXT.getStart(), this.NUMMOVEXT.getEnd());
    } else{
      return "";
    }

  }

  public String getNumextcta() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.NUMEXTCTA.getStart(), this.NUMEXTCTA.getEnd());
    } else{
      return "";
    }
  }

  public String getTipolin() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.TIPOLIN.getStart(), this.TIPOLIN.getEnd());
    } else{
      return "";
    }

  }

  public String getLinref() {
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.LINREF.getStart(), this.LINREF.getEnd());
    } else{
      return "";
    }
  }

  public String getFecTrn(){
    if(TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.FECTRN_AU.getStart(),this.FECTRN_AU.getEnd());
    } else{
      return "";
    }
  }

  public String getHorTrn(){
    if(TecnocomOperationType.AU.equals(this.getOperationType())){
      return  this.detail.substring(this.HORTRN_AU.getStart(),this.HORTRN_AU.getEnd());
    } else{
      return "";
    }
  }

  public BigDecimal getImpDiv() {
    if(TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.getScaledValue(this.detail.substring(this.IMPDIV_AU.getStart(), this.IMPDIV_AU.getEnd()), this.IMPDIV_AU.getDecimal());
    } else{
      return BigDecimal.ZERO;
    }
  }

  public String getClamonDiv() {
    if(TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.CLAMONDIV_AU.getStart(), this.CLAMONDIV_AU.getEnd());
    } else{
      return "";
    }
  }


  public BigDecimal getImpAutCon() {
    return this.getScaledValue(this.detail.substring(this.IMPAUTCON_AU.getStart(), this.IMPAUTCON_AU.getEnd()), this.IMPAUTCON_AU.getDecimal());
  }

  public String getNomComRed(){
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
      return this.detail.substring(this.NOMCOMRED.getStart(),this.NOMCOMRED.getEnd());
    } else if (TecnocomOperationType.AU.equals(this.getOperationType())){
      return this.detail.substring(this.NOMCOMRED_AU.getStart(),this.NOMCOMRED_AU.getEnd());
    } else{
      return "";
    }

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
      case SUSCRIPCION_INTERNACIONAL:
        type = PrepaidMovementType.SUSCRIPTION;
        break;
      case COMPRA_INTERNACIONAL:
        type = PrepaidMovementType.PURCHASE;
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
    if(TecnocomOperationType.OP.equals(this.getOperationType())){
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
    }else if(TecnocomOperationType.AU.equals(this.getOperationType())){
      return "ReconciliationFileDetail{" +
        "pan='" + this.getPan() + '\'' +
        "contrato='" + this.getContrato() + '\'' +
        "indnorcor='" + this.getIndnorcor() + '\'' +
        "tipofac='" + this.getTipoFac() + '\'' +
        "desctipofac='" + this.getTipoFac().getDescription() + '\'' +
        "fectrx='" + this.getFecTrn() + '\'' +
        "hortrn='" + this.getHorTrn() + '\'' +
        "impfac='" + this.getImpfac() + '\'' +
        "origenope='" + this.getOrigenope() + '\'' +
        "clamondiv='" + this.getClamonDiv() + '\'' +
        "impautcon='" + this.getImpAutCon() + '\'' +
        "impdiv='" + this.getImpDiv() + '\'' +
        "cmbapli='" + this.getCmbApli() + '\'' +
        '}';
    }
    else{
      return "";
    }
  }
}
