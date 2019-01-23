package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.accounting.ejb.v10.PrepaidAccountingEJBBean10;
import cl.multicaja.accounting.ejb.v10.PrepaidClearingEJBBean10;
import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.PdfUtils;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
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
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @EJB
  private PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10;

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJBBean10;

  @EJB
  private  TecnocomReconciliationEJBBean10 tecnocomReconciliationEJBBean10;

  @EJB
  private McRedReconciliationEJBBean10 mcRedReconciliationEJBBean10;

  @EJB
  private MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10;


  private ParametersUtil parametersUtil;
  private ConfigUtils configUtils;
  private EncryptUtil encryptUtil;
  private PdfUtils pdfUtils;
  private NumberUtils numberUtils;
  private UserClient userClient;

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

  public EncryptUtil getEncryptUtil(){
    if(this.encryptUtil == null){
      this.encryptUtil = EncryptUtil.getInstance();
    }
    return this.encryptUtil;
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

  public UserClient getUserClient(){
    if(userClient == null){
      userClient = new UserClient();
    }
    return userClient;
  }

  public void setUserClient(UserClient userClient) {
    this.userClient = userClient;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJBBean10() {
    return prepaidCardEJBBean10;
  }

  public void setPrepaidCardEJBBean10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
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

  public CdtEJBBean10 getCdtEJBBean10() {
    return cdtEJBBean10;
  }

  public void setCdtEJBBean10(CdtEJBBean10 cdtEJBBean10) {
    this.cdtEJBBean10 = cdtEJBBean10;
  }

  public TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
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

  public MastercardCurrencyUpdateEJBBean10 getMastercardCurrencyUpdateEJBBean10() {
    return mastercardCurrencyUpdateEJBBean10;
  }

  public void setMastercardCurrencyUpdateEJBBean10(MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10) {
    this.mastercardCurrencyUpdateEJBBean10 = mastercardCurrencyUpdateEJBBean10;
  }


}
