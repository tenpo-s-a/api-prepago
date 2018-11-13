package cl.multicaja.prepaid.helpers.tecnocom.model;

import java.math.BigDecimal;

public class AccountantFileDetail {
  public static final String FEC_DATE_FORMAT = "yyyy-MM-dd";

  private String detail;
  private Boolean hasError;
  private String errorDetails;

  private final ReconciliationFileLayout CODENT = new ReconciliationFileLayout(0, 4, null);
  private final ReconciliationFileLayout CODGRU = new ReconciliationFileLayout(4, 2, null);
  private final ReconciliationFileLayout CODCCN = new ReconciliationFileLayout(6, 4, null);
  private final ReconciliationFileLayout IMPORTE = new ReconciliationFileLayout(10, 17, 2);
  private final ReconciliationFileLayout CLAMON = new ReconciliationFileLayout(27, 3, null);
  private final ReconciliationFileLayout FECALTA = new ReconciliationFileLayout(30, 10, null);
  private final ReconciliationFileLayout FECCONTA = new ReconciliationFileLayout(40, 10, null);
  private final ReconciliationFileLayout FECOPER = new ReconciliationFileLayout(50, 10, null);

  private final ReconciliationFileLayout CUENTADEBE = new ReconciliationFileLayout(463, 6, null);
  private final ReconciliationFileLayout CUENTAHABER = new ReconciliationFileLayout(469, 6, null);

  public AccountantFileDetail() { }

  public AccountantFileDetail(String detail) {
    this.detail = detail;
  }

  public String getCodent() {
    return this.detail.substring(this.CODENT.getStart(), this.CODENT.getEnd()).trim();
  }

  public String getCodGru() { return this.detail.substring(this.CODGRU.getStart(), this.CODGRU.getEnd()).trim(); }

  public String getCodCcn() { return this.detail.substring(this.CODCCN.getStart(), this.CODCCN.getEnd()).trim(); }

  public BigDecimal getImporte() {
    long importeLong = Long.valueOf(this.detail.substring(this.IMPORTE.getStart(), this.IMPORTE.getEnd()));
    return new BigDecimal(importeLong);
  }

  public String getClamon() { return this.detail.substring(this.CLAMON.getStart(), this.CLAMON.getEnd()).trim(); }

  public String getFecAlta() { return this.detail.substring(this.FECALTA.getStart(), this.FECALTA.getEnd()).trim(); }

  public String getFecConta() { return this.detail.substring(this.FECCONTA.getStart(), this.FECCONTA.getEnd()).trim(); }

  public String getFecOper() { return this.detail.substring(this.FECOPER.getStart(), this.FECOPER.getEnd()).trim(); }

  public BigDecimal getCuentaDebe() {
    long importeLong = Long.valueOf(this.detail.substring(this.CUENTADEBE.getStart(), this.CUENTADEBE.getEnd()));
    return new BigDecimal(importeLong);
  }

  public BigDecimal getCuentaHaber() {
    long importeLong = Long.valueOf(this.detail.substring(this.CUENTAHABER.getStart(), this.CUENTAHABER.getEnd()));
    return new BigDecimal(importeLong);
  }
}
