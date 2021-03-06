package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.accounting.ejb.v10.PrepaidAccountingEJBBean10;
import cl.multicaja.accounting.ejb.v10.PrepaidClearingEJBBean10;
import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.PdfUtils;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.ejb.v11.PrepaidMovementEJBBean11;
import cl.multicaja.prepaid.helpers.EncryptHelper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;
import org.apache.camel.CamelContext;

import javax.ejb.EJB;

/**
 * @autor vutreras
 */
public abstract class BaseRoute10 extends CamelRouteBuilder {

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private PrepaidMovementEJBBean11 prepaidMovementEJBBean11;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @EJB
  private PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10;

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJBBean10;

  @EJB
  private TecnocomReconciliationEJBBean10 tecnocomReconciliationEJBBean10;

  @EJB
  private McRedReconciliationEJBBean10 mcRedReconciliationEJBBean10;

  @EJB
  private ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10;

  @EJB
  private MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10;

  @EJB
  private BackofficeEJBBean10 backofficeEJBBEan10;

  @EJB
  private AccountEJBBean10 accountEJBBean10;


  private ParametersUtil parametersUtil;
  private ConfigUtils configUtils;
  private EncryptHelper encryptHelper;
  private PdfUtils pdfUtils;
  private NumberUtils numberUtils;

  public BaseRoute10() {
    super();
  }

  /**
   *
   * @param context
   */
  public BaseRoute10(CamelContext context) {
    super(context);
  }

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  public EncryptHelper getEncryptHelper() {
    if(encryptHelper == null){
      encryptHelper = EncryptHelper.getInstance();
    }
    return encryptHelper;
  }



  public NumberUtils getNumberUtils() {
    if (this.numberUtils == null) {
      this.numberUtils = NumberUtils.getInstance();
    }
    return this.numberUtils;
  }

  public ParametersUtil getParametersUtil() {
    if (parametersUtil == null) {
      parametersUtil = ParametersUtil.getInstance();
    }
    return parametersUtil;
  }

  public PdfUtils getPdfUtils(){
    if(pdfUtils == null){
      pdfUtils = PdfUtils.getInstance();
    }
    return pdfUtils;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  public PrepaidCardEJBBean11 getPrepaidCardEJBBean11() {
    return prepaidCardEJBBean11;
  }

  public void setPrepaidCardEJBBean11(PrepaidCardEJBBean11 prepaidCardEJBBean11) {
    this.prepaidCardEJBBean11 = prepaidCardEJBBean11;
  }

  public PrepaidEJBBean10 getPrepaidEJBBean10() {
    return prepaidEJBBean10;
  }

  public void setPrepaidEJBBean10(PrepaidEJBBean10 prepaidEJBBean10) {
    this.prepaidEJBBean10 = prepaidEJBBean10;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  public PrepaidMovementEJBBean11 getPrepaidMovementEJBBean11() {
    return prepaidMovementEJBBean11;
  }

  public void setPrepaidMovementEJBBean11(PrepaidMovementEJBBean11 prepaidMovementEJBBean11) {
    this.prepaidMovementEJBBean11 = prepaidMovementEJBBean11;
  }

  public CdtEJBBean10 getCdtEJBBean10() {
    return cdtEJBBean10;
  }

  public void setCdtEJBBean10(CdtEJBBean10 cdtEJBBean10) {
    this.cdtEJBBean10 = cdtEJBBean10;
  }

  public TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }

  public TecnocomServiceHelper getTecnocomServiceHelper() {
    return TecnocomServiceHelper.getInstance();
  }

  public MailPrepaidEJBBean10 getMailPrepaidEJBBean10() {
    return mailPrepaidEJBBean10;
  }

  public void setMailPrepaidEJBBean10(MailPrepaidEJBBean10 mailPrepaidEJBBean10) {
    this.mailPrepaidEJBBean10 = mailPrepaidEJBBean10;
  }

  public PrepaidAccountingEJBBean10 getPrepaidAccountingEJBBean10() {
    return prepaidAccountingEJBBean10;
  }

  public void setPrepaidAccountingEJBBean10(PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10) {
    this.prepaidAccountingEJBBean10 = prepaidAccountingEJBBean10;
  }

  public PrepaidClearingEJBBean10 getPrepaidClearingEJBBean10() {
    return prepaidClearingEJBBean10;
  }

  public void setPrepaidClearingEJBBean10(PrepaidClearingEJBBean10 prepaidClearingEJBBean10) {
    this.prepaidClearingEJBBean10 = prepaidClearingEJBBean10;
  }

  public TecnocomReconciliationEJBBean10 getTecnocomReconciliationEJBBean10() {
    return tecnocomReconciliationEJBBean10;
  }

  public void setTecnocomReconciliationEJBBean10(TecnocomReconciliationEJBBean10 tecnocomReconciliationEJBBean10) {
    this.tecnocomReconciliationEJBBean10 = tecnocomReconciliationEJBBean10;
  }

  public McRedReconciliationEJBBean10 getMcRedReconciliationEJBBean10() {
    return mcRedReconciliationEJBBean10;
  }

  public void setMcRedReconciliationEJBBean10(McRedReconciliationEJBBean10 mcRedReconciliationEJBBean10) {
    this.mcRedReconciliationEJBBean10 = mcRedReconciliationEJBBean10;
  }

  public ReconciliationFilesEJBBean10 getReconciliationFilesEJBBean10() { return reconciliationFilesEJBBean10; }

  public void setReconciliationFilesEJBBean10(ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10) {
    this.reconciliationFilesEJBBean10 = reconciliationFilesEJBBean10;
  }

  public MastercardCurrencyUpdateEJBBean10 getMastercardCurrencyUpdateEJBBean10() {
    return mastercardCurrencyUpdateEJBBean10;
  }

  public void setMastercardCurrencyUpdateEJBBean10(MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10) {
    this.mastercardCurrencyUpdateEJBBean10 = mastercardCurrencyUpdateEJBBean10;
  }

  public BackofficeEJBBean10 getBackofficeEJBBEan10() {
    return backofficeEJBBEan10;
  }

  public void setBackofficeEJBBEan10(BackofficeEJBBean10 backofficeEJBBEan10) {
    this.backofficeEJBBEan10 = backofficeEJBBEan10;
  }

  public AccountEJBBean10 getAccountEJBBean10() {
    return accountEJBBean10;
  }

  public void setAccountEJBBean10(AccountEJBBean10 accountEJBBean10) {
    this.accountEJBBean10 = accountEJBBean10;
  }
}
