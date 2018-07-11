package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.PdfUtils;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.ejb.v10.MailEJBBean10;
import cl.multicaja.users.utils.ParametersUtil;
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
  private UsersEJBBean10 usersEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

  @EJB
  private MailEJBBean10 mailEJBBean10;

  private ParametersUtil parametersUtil;
  private ConfigUtils configUtils;
  private EncryptUtil encryptUtil;
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

  public EncryptUtil getEncryptUtil(){
    if(this.encryptUtil == null){
      this.encryptUtil = new EncryptUtil();
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

  public UsersEJBBean10 getUsersEJBBean10() {
    return usersEJBBean10;
  }

  public void setUsersEJBBean10(UsersEJBBean10 usersEJBBean10) {
    this.usersEJBBean10 = usersEJBBean10;
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

  public MailEJBBean10 getMailEJBBean10() {
    return mailEJBBean10;
  }

  public void setMailEJBBean10(MailEJBBean10 mailEJBBean10) {
    this.mailEJBBean10 = mailEJBBean10;
  }

  public TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }
}
