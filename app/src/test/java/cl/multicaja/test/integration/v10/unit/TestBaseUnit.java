package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.ejb.v10.PrepaidAccountingEJBBean10;
import cl.multicaja.accounting.ejb.v10.PrepaidAccountingFileEJBBean10;
import cl.multicaja.accounting.ejb.v10.PrepaidClearingEJBBean10;
import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.*;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.async.v10.ProductChangeDelegate10;
import cl.multicaja.prepaid.async.v10.ReprocesQueueDelegate10;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.*;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.UserAccountNew;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.security.common.FileRealmHelper;
import org.junit.Assert;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static cl.multicaja.core.model.Errors.LIMITES_ERROR_GENERICO_$VALUE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @autor vutreras
 */
public class TestBaseUnit extends TestApiBase {

  static {
    System.setProperty("project.artifactId", "api-prepaid");
  }

  public ParametersUtil parametersUtil = ParametersUtil.getInstance();

  public final static String APP_NAME = "prepaid.appname";

  private static ConfigUtils configUtils;
  private static PrepaidTopupDelegate10 prepaidTopupDelegate10;
  private static CdtEJBBean10 cdtEJBBean10;
  private static PrepaidUserEJBBean10 prepaidUserEJBBean10;
  private static PrepaidCardEJBBean10 prepaidCardEJBBean10;
  private static PrepaidEJBBean10 prepaidEJBBean10;
  private static PrepaidMovementEJBBean10 prepaidMovementEJBBean10;
  private static MailPrepaidEJBBean10 mailPrepaidEJBBean10;
  private static FilesEJBBean10 filesEJBBean10;
  private static ReprocesQueueDelegate10 reprocesQueueDelegate10;
  private static UserClient userClient;
  private static ProductChangeDelegate10 productChangeDelegate10;
  private static PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10;
  private static TecnocomReconciliationEJBBean10 tecnocomReconciliationEJBBean10;
  private static McRedReconciliationEJBBean10 mcRedReconciliationEJBBean10;
  private static MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10;
  private static PrepaidAccountingFileEJBBean10 prepaidAccountingFileEJB10;
  private static PrepaidClearingEJBBean10 prepaidClearingEJBBean10;
  protected static CalculationsHelper calculationsHelper = CalculationsHelper.getInstance();
  {
    System.out.println("Exist: " + getPrepaidCardEJBBean10());
    calculationsHelper.setMastercardCurrencyUpdateEJBBean10(getMastercardCurrencyUpdateEJBBean10());
  }

  protected final static HttpHeader[] DEFAULT_HTTP_HEADERS2 = {
    new HttpHeader("Content-Type", "application/json"),
    new HttpHeader(Constants.HEADER_USER_LOCALE, Constants.DEFAULT_LOCALE.toString()),
    new HttpHeader(Constants.HEADER_USER_TIMEZONE,"America/Santiago")
  };

  /**
   *
   * @return
   */
  public static ConfigUtils getConfigUtils() {
    if (configUtils == null) {
      configUtils = new ConfigUtils("api-prepaid");
    }
    return configUtils;
  }

  public static UserClient getUserClient() {
    if (userClient == null) {
      userClient = new UserClient();
    }
    return userClient;
  }

  public static CalculationsHelper getCalculationsHelper(){
    return  CalculationsHelper.getInstance();
  }

  /**
   *
   * @return
   */
  public static String getSchema() {
    return getPrepaidCardEJBBean10().getSchema();
  }

  public static String getSchemaAccounting() {
    return getPrepaidCardEJBBean10().getSchemaAccounting();
  }

  /**
   *
   * @return
   */
  public static DBUtils getDbUtils() {
    return getPrepaidCardEJBBean10().getDbUtils();
  }
  public CalculatorParameter10 getPercentage(){
   return getCalculationsHelper().getCalculatorParameter10();
  }
  /**
   *
   * @return
   */
  public static PrepaidTopupDelegate10 getPrepaidTopupDelegate10() {
    if (prepaidTopupDelegate10 == null) {
      prepaidTopupDelegate10 = new PrepaidTopupDelegate10();
    }
    return prepaidTopupDelegate10;
  }
  public static ReprocesQueueDelegate10 getReprocesQueueDelegate10(){
    if (reprocesQueueDelegate10 == null) {
      reprocesQueueDelegate10 = new ReprocesQueueDelegate10();
    }
    return reprocesQueueDelegate10;
  }
  public static ProductChangeDelegate10 getProductChangeDelegate10(){
    if (productChangeDelegate10 == null) {
      productChangeDelegate10 = new ProductChangeDelegate10();
    }
    return productChangeDelegate10;
  }
  /**
   *
   * @return
   */
  public static CdtEJBBean10 getCdtEJBBean10() {
    if (cdtEJBBean10 == null) {
      cdtEJBBean10 = new CdtEJBBean10();
    }
    return cdtEJBBean10;
  }

  /**
   *
   * @return
   */
  public static PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10(){
    if (prepaidMovementEJBBean10 == null) {
      prepaidMovementEJBBean10 = new PrepaidMovementEJBBean10();
      prepaidMovementEJBBean10.setDelegate(getPrepaidTopupDelegate10());
      prepaidMovementEJBBean10.setPrepaidUserEJB10(getPrepaidUserEJBBean10());
      prepaidMovementEJBBean10.setCdtEJB10(getCdtEJBBean10());
      prepaidMovementEJBBean10.setPrepaidCardEJB10(getPrepaidCardEJBBean10());
      prepaidMovementEJBBean10.setUserClient(getUserClient());
      prepaidMovementEJBBean10.setPrepaidEJBBean10(getPrepaidEJBBean10());
    }
    return prepaidMovementEJBBean10;
  }

  /**
   *
   * @return
   */
  public static PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    if (prepaidUserEJBBean10 == null) {
      prepaidUserEJBBean10 = new PrepaidUserEJBBean10();
      prepaidUserEJBBean10.setPrepaidCardEJB10(getPrepaidCardEJBBean10());
      prepaidUserEJBBean10.setPrepaidMovementEJB10(getPrepaidMovementEJBBean10());
    }
    return prepaidUserEJBBean10;
  }

  /**
   *
   * @return
   */
  public static PrepaidCardEJBBean10 getPrepaidCardEJBBean10() {
    if (prepaidCardEJBBean10 == null) {
      prepaidCardEJBBean10 = new PrepaidCardEJBBean10();
    }
    return prepaidCardEJBBean10;
  }
  public static MailPrepaidEJBBean10 getMailPrepaidEJBBean10(){

    if (mailPrepaidEJBBean10 == null) {
      mailPrepaidEJBBean10 = new MailPrepaidEJBBean10();
      mailPrepaidEJBBean10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      mailPrepaidEJBBean10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      mailPrepaidEJBBean10.setPrepaidTopupDelegate10(getPrepaidTopupDelegate10());
    }
    return mailPrepaidEJBBean10;
  }
  public static PrepaidAccountingFileEJBBean10 getPrepaidAccountingFileEJBBean10(){
    if(prepaidAccountingFileEJB10 == null){
      prepaidAccountingFileEJB10 = new PrepaidAccountingFileEJBBean10();
    }
    return prepaidAccountingFileEJB10;
  }
  public static PrepaidClearingEJBBean10 getPrepaidClearingEJBBean10(){
    if(prepaidClearingEJBBean10 == null){
      prepaidClearingEJBBean10 = new PrepaidClearingEJBBean10();
    }
    return prepaidClearingEJBBean10;
  }

  /**
   *
   * @return
   */
  public static PrepaidEJBBean10 getPrepaidEJBBean10() {
    if (prepaidEJBBean10 == null) {
      prepaidEJBBean10 = new PrepaidEJBBean10();
      prepaidEJBBean10.setDelegate(getPrepaidTopupDelegate10());
      prepaidEJBBean10.setCdtEJB10(getCdtEJBBean10());
      prepaidEJBBean10.setPrepaidMovementEJB10(getPrepaidMovementEJBBean10());
      prepaidEJBBean10.setPrepaidUserEJB10(getPrepaidUserEJBBean10());
      prepaidEJBBean10.setPrepaidCardEJB10(getPrepaidCardEJBBean10());
      prepaidEJBBean10.setFilesEJBBean10(getFilesEJBBean10());
      prepaidEJBBean10.setDelegateReprocesQueue(getReprocesQueueDelegate10());
      prepaidEJBBean10.setProductChangeDelegate(getProductChangeDelegate10());
    }
    return prepaidEJBBean10;
  }

  public static FilesEJBBean10 getFilesEJBBean10() {
    if(filesEJBBean10 == null) {
      filesEJBBean10 = new FilesEJBBean10();
    }
    return filesEJBBean10;
  }

  public static PrepaidAccountingEJBBean10 getPrepaidAccountingEJBBean10() {
    if (prepaidAccountingEJBBean10 == null) {
      prepaidAccountingEJBBean10 = new PrepaidAccountingEJBBean10();
      prepaidAccountingEJBBean10.setMailPrepaidEJBBean10(getMailPrepaidEJBBean10());
    }
    return prepaidAccountingEJBBean10;
  }

  public static TecnocomReconciliationEJBBean10 getTecnocomReconciliationEJBBean10() {
    if(tecnocomReconciliationEJBBean10 == null) {
      tecnocomReconciliationEJBBean10 = new TecnocomReconciliationEJBBean10();
      tecnocomReconciliationEJBBean10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      tecnocomReconciliationEJBBean10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
    }
    return tecnocomReconciliationEJBBean10;
  }

  public static McRedReconciliationEJBBean10 getMcRedReconciliationEJBBean10() {
    if(mcRedReconciliationEJBBean10 == null) {
      mcRedReconciliationEJBBean10 = new McRedReconciliationEJBBean10();
      mcRedReconciliationEJBBean10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
    }
    return mcRedReconciliationEJBBean10;
  }

  public static MastercardCurrencyUpdateEJBBean10 getMastercardCurrencyUpdateEJBBean10() {
    if(mastercardCurrencyUpdateEJBBean10 == null) {
      mastercardCurrencyUpdateEJBBean10 = new MastercardCurrencyUpdateEJBBean10();
    }
    return mastercardCurrencyUpdateEJBBean10;
  }

  /**
   *
   * @return
   */
  public static TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }

  /**
   * realiza un signUp con rut e email
   *
   * @param rut
   * @param email
   * @return
   * @throws Exception
   */
  public SignUp signupUser(Integer rut, String email) throws Exception {
    return getUserClient().signUp(null, new SignUPNew(email,rut));
  }

  /**
   * realiza un signUp con datos aleatorios
   *
   * @return
   * @throws Exception
   */
  public SignUp signupUser() throws Exception {
    Integer rut = getUniqueRutNumber();
    String email = getUniqueEmail();
    return this.signupUser(rut, email);
  }

  /**
   * realiza un signUp con datos aleatorios y retorna el usuario creado
   *
   * @return
   * @throws Exception
   */
  public User signupUserAndGetUser() throws Exception {
    SignUp singUP = signupUser();
    return getUserClient().getUserById(null, singUP.getUserId());
  }

  /**
   * actualiza un usuario
   *
   * @param u
   * @return
   * @throws Exception
   */
  public User updateUser(User u) throws Exception {
    return getUserClient().updateUser(null,u.getId(),u);
  }

  /**
   * registra un usuario y además lo deja habilitado completamente
   *
   * @return
   * @throws Exception
   */
  public User registerUser() throws Exception {
    return registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED);
  }

  /**
   * registra un usuario y además lo deja en algun estado
   *
   * @return
   * @throws Exception
   */
  public User registerUser(UserStatus status) throws Exception {
    return registerUser(String.valueOf(numberUtils.random(1111,9999)),status);
  }

  /**
   * registra un usuario y además lo deja en algun estado de identidad
   *
   * @return
   * @throws Exception
   */
  public User registerUser(UserIdentityStatus status) throws Exception {
    return registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, status);
  }

  /**
   * registra un usuario con clave
   *
   * @param password
   * @return
   * @throws Exception
   */
  public User registerUser(String password) throws Exception {
    return registerUser(password ,UserStatus.ENABLED);
  }

  /**
   * registra un usuario con clave y estado especifico
   *
   * @param password
   * @param status
   * @return
   * @throws Exception
   */
  public User registerUser(String password, UserStatus status) throws Exception {
    return registerUser(password, status, UserIdentityStatus.NORMAL);
  }

  /**
   * registra un usuario con clave y estado especifico
   *
   * @param password
   * @param status
   * @return
   * @throws Exception
   */
  public User registerUser(String password, UserStatus status, UserIdentityStatus identityStatus) throws Exception {
    User user = signupUserAndGetUser();
    user.setName(null);
    user.setLastname_1(null);
    user.setLastname_2(null);
    user = getUserClient().fillUser(null,user);
    user.setGlobalStatus(status);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.getEmail().setStatus(EmailStatus.VERIFIED);
    user.setNameStatus(NameStatus.VERIFIED);
    user.setIdentityStatus(identityStatus);
    user.setPassword(password);
    user = updateUser(user);
    return user;
  }

  /**
   * actualiza la clave de un usuario
   *
   * @param user
   * @param newPassword
   * @return
   * @throws Exception
   */
  public User updateUserPassword(User user, String newPassword) throws Exception {
    UserPasswordNew pass = new UserPasswordNew();
    pass.setValue(newPassword);
    user = getUserClient().updateUserPassword(null, user.getId(), pass);
    return user;
  }

  /**
   *
   * @return
   * @throws Exception
   */
  public User registerUserFirstTopup() throws Exception {
    return registerUserFirstTopup(String.valueOf(numberUtils.random(1111,9999)),UserStatus.ENABLED);
  }

  /**
   *
   * @param password
   * @param status
   * @return
   * @throws Exception
   */
  public User registerUserFirstTopup(String password, UserStatus status) throws Exception {
    User user = signupUserAndGetUser();
    user.setName(null);
    user.setLastname_1(null);
    user.setLastname_2(null);
    user = getUserClient().fillUser(null,user);
    user.setGlobalStatus(status);
    user.getRut().setStatus(RutStatus.UNVERIFIED);
    user.getEmail().setStatus(EmailStatus.UNVERIFIED);
    user.setNameStatus(NameStatus.VERIFIED);
    user.setPassword(password);
    user = updateUser(user);
    return user;
  }

  public User registerUserBlackListed() throws Exception {
    return registerUserBlackListed(String.valueOf(numberUtils.random(1111,9999)),UserStatus.ENABLED);
  }
  public User registerUserBlackListed(String password, UserStatus status) throws Exception {
    User user = signupUserAndGetUser();
    user.setName(null);
    user.setLastname_1(null);
    user.setLastname_2(null);
    user = getUserClient().fillUser(null,user);
    user.setGlobalStatus(status);
    user.getRut().setStatus(RutStatus.UNVERIFIED);
    user.getEmail().setStatus(EmailStatus.UNVERIFIED);
    user.setNameStatus(NameStatus.VERIFIED);
    user.setPassword(password);
    user.setIdentityStatus(UserIdentityStatus.TERRORIST);
    user = updateUser(user);
    return user;
  }
  /**
   *
   * @return
   */
  public PrepaidUser10 buildPrepaidUser10(User user) {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(user != null ? user.getId() : null);
    prepaidUser.setRut(user != null ? user.getRut().getValue() : null);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);
    return prepaidUser;
  }

  /**
   *
   * @return
   */
  public PrepaidUser10 buildPrepaidUser10() {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(getUniqueLong());
    prepaidUser.setRut(getUniqueRutNumber());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);
    return prepaidUser;
  }

  /**
   *
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCard10(PrepaidUser10 prepaidUser) throws Exception {
    int expiryYear = numberUtils.random(1000, 9999);
    int expiryMonth = numberUtils.random(1, 99);
    int expiryDate = numberUtils.toInt(expiryYear + "" + StringUtils.leftPad(String.valueOf(expiryMonth), 2, "0"));
    String pan = getRandomNumericString(16);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser != null ? prepaidUser.getId() : null);
    prepaidCard.setPan(Utils.replacePan(pan));
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(pan));
    prepaidCard.setExpiration(expiryDate);
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setProcessorUserId(getRandomNumericString(20));
    prepaidCard.setNameOnCard("Tarjeta de: " + getRandomString(5));
    prepaidCard.setProducto(getRandomNumericString(2));
    prepaidCard.setNumeroUnico(getRandomNumericString(8));
    return prepaidCard;
  }

  /**
   *
   * @param prepaidUser
   * @param altaClienteDTO
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCard10(PrepaidUser10 prepaidUser, AltaClienteDTO altaClienteDTO) throws Exception {
    int expiryYear = numberUtils.random(1000, 9999);
    int expiryMonth = numberUtils.random(1, 99);
    int expiryDate = numberUtils.toInt(expiryYear + "" + StringUtils.leftPad(String.valueOf(expiryMonth), 2, "0"));
    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser != null ? prepaidUser.getId() : null);
    prepaidCard.setPan(getRandomNumericString(16));
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));
    prepaidCard.setExpiration(expiryDate);
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard.setNameOnCard("Tarjeta de: " + getRandomString(5));
    prepaidCard.setProducto(getRandomNumericString(2));
    prepaidCard.setNumeroUnico(getRandomNumericString(8));
    return prepaidCard;
  }

  /**
   * CREA TARJETA ESTADO PENDING
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCard10Pending(PrepaidUser10 prepaidUser) throws Exception {
    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser != null ? prepaidUser.getId() : null);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard.setProcessorUserId(getRandomNumericString(20));
    return prepaidCard;
  }

  /**
   * CREA TARJETA ESTADO PENDING
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCard10Pending() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = getPrepaidUserEJBBean10().createPrepaidUser(null, prepaidUser);
    return buildPrepaidCard10Pending(prepaidUser);
  }

  /**
   *
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCard10() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = getPrepaidUserEJBBean10().createPrepaidUser(null, prepaidUser);
    return buildPrepaidCard10(prepaidUser);
  }

  /**
   * construye una tarjeta desde tecnocom
   * @param user
   * @param prepaidUser
   * @return
   */
  public PrepaidCard10 buildPrepaidCard10FromTecnocom(User user, PrepaidUser10 prepaidUser) throws Exception {

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);

    DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(altaClienteDTO.getContrato());

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser.getId());
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
    prepaidCard.setEncryptedPan(encryptUtil.encrypt(datosTarjetaDTO.getPan()));
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setNameOnCard(user.getName() + " " + user.getLastname_1());
    prepaidCard.setProducto(datosTarjetaDTO.getProducto());
    prepaidCard.setNumeroUnico(datosTarjetaDTO.getIdentclitar());

    return prepaidCard;
  }

  /**
   *
   * @param user
   * @return
   */
  public PrepaidTopup10 buildPrepaidTopup10(User user) {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setRut(user != null ? user.getRut().getValue() : null);
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(3119));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidTopup.setAmount(newAmountAndCurrency);
    prepaidTopup.setTotal(newAmountAndCurrency);
    prepaidTopup.setMerchantCategory(1);
    prepaidTopup.setMerchantName(getRandomString(6));

    return prepaidTopup;
  }

  /**
   *
   * @param user
   * @return
   */
  public NewPrepaidTopup10 buildNewPrepaidTopup10(User user) {

    String merchantCode = numberUtils.random() ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setRut(user != null ? user.getRut().getValue() : null);
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(3119));

    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidTopup.setAmount(newAmountAndCurrency);

    prepaidTopup.setMerchantCategory(1);
    prepaidTopup.setMerchantName(getRandomString(6));

    return prepaidTopup;
  }

  /**
   *
   * @param user
   * @return
   */
  public NewPrepaidWithdraw10 buildNewPrepaidWithdraw10(User user) throws Exception {
    return buildNewPrepaidWithdraw10(user, String.valueOf(numberUtils.random(1111,9999)));
  }

  public NewPrepaidWithdraw10 buildNewPrepaidWithdraw10(User user, String password) throws Exception {
    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE : getUniqueLong().toString();
    return buildNewPrepaidWithdraw10(user, password, merchantCode);
  }

  public NewPrepaidWithdraw10 buildNewPrepaidWithdraw10(User user, String password, String merchantCode) throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(user != null ? user.getRut().getValue() : null);
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(RandomUtils.nextDouble(2000,9000)));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(newAmountAndCurrency);

    prepaidWithdraw.setMerchantCategory(1);
    prepaidWithdraw.setMerchantName(getRandomString(6));

    prepaidWithdraw.setPassword(password);

    // Los retiros web requieren que el usuario tenga una cuenta asociada
    if (NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE.equals(merchantCode)) {
      UserAccount bankAccount = createBankAccount(user);
      prepaidWithdraw.setBankAccountId(bankAccount.getId());
    }

    return prepaidWithdraw;
  }

  public void buildAccountForPrepaid(User user, NewPrepaidWithdraw10 prepaidWithdraw10) {

  }

  /**
   *
   * @param user
   * @return
   */
  public PrepaidWithdraw10 buildPrepaidWithdraw10(User user) {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    PrepaidWithdraw10 prepaidWithdraw = new PrepaidWithdraw10();

    prepaidWithdraw.setRut(user != null ? user.getRut().getValue() : null);
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(3119));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(newAmountAndCurrency);

    prepaidWithdraw.setMerchantCategory(1);
    prepaidWithdraw.setMerchantName(getRandomString(6));

    NewAmountAndCurrency10 fee = new NewAmountAndCurrency10();
    fee.setValue(new BigDecimal(100));
    fee.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setFee(fee);

    prepaidWithdraw.setMcVoucherType("A");
    prepaidWithdraw.setMcVoucherData(new ArrayList<>());

    return prepaidWithdraw;
  }



  /**
   *
   * @param user
   * @param prepaidTopup
   * @return
   * @throws BaseException
   */
  public CdtTransaction10 buildCdtTransaction10(User user, PrepaidTopup10 prepaidTopup) throws BaseException {
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(prepaidTopup.getAmount().getValue());
    cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + user.getRut().getValue());
    cdtTransaction.setGloss(prepaidTopup.getCdtTransactionType().getName()+" "+prepaidTopup.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(prepaidTopup.getTransactionId());
    cdtTransaction.setIndSimulacion(false);
    return cdtTransaction;
  }

  /**
   *
   * @param cdtTransaction
   * @return
   * @throws Exception
   */
  public CdtTransaction10 createCdtTransaction10(CdtTransaction10 cdtTransaction) throws Exception {

    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    // Si no cumple con los limites
    if(!cdtTransaction.isNumErrorOk()){
      int lNumError = cdtTransaction.getNumErrorInt();
      if(lNumError != -1 && lNumError > 10000) {
        throw new ValidationException(LIMITES_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(LIMITES_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    return cdtTransaction;
  }

  /**
   *
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  public PrepaidUser10 createPrepaidUser10(PrepaidUser10 prepaidUser) throws Exception {

    prepaidUser = getPrepaidUserEJBBean10().createPrepaidUser(null, prepaidUser);

    Assert.assertNotNull("debe retornar un usuario", prepaidUser);
    Assert.assertEquals("debe tener id", true, prepaidUser.getId() > 0);
    Assert.assertEquals("debe tener idUserMc", true, prepaidUser.getUserIdMc() > 0);
    Assert.assertEquals("debe tener rut", true, prepaidUser.getRut() > 0);
    Assert.assertNotNull("debe tener status", prepaidUser.getStatus());

    return prepaidUser;
  }

  /**
   *
   * @param prepaidCard
   * @return
   * @throws Exception
   */
  public PrepaidCard10 createPrepaidCard10(PrepaidCard10 prepaidCard) throws Exception {

    prepaidCard = getPrepaidCardEJBBean10().createPrepaidCard(null, prepaidCard);

    Assert.assertNotNull("debe retornar un usuario", prepaidCard);
    Assert.assertEquals("debe tener id", true, prepaidCard.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, prepaidCard.getIdUser() > 0);
    Assert.assertNotNull("debe tener status", prepaidCard.getStatus());

    return prepaidCard;
  }

  /*
    TOPUP
   */

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, PrepaidMovementType.TOPUP);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param prepaidCard
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup, PrepaidCard10 prepaidCard) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, null, PrepaidMovementType.TOPUP);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param cdtTransaction
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup, CdtTransaction10 cdtTransaction) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, null, cdtTransaction, PrepaidMovementType.TOPUP);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param prepaidCard
   * @param cdtTransaction
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
  }

  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction,PrepaidMovementStatus status) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP,status);
  }
  /*
    WITHDRAW
   */
  /**
   *
   * @param prepaidUser
   * @param prepaidWithdraw
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidWithdraw10 prepaidWithdraw) {
    return buildPrepaidMovement10(prepaidUser, prepaidWithdraw, null, null, PrepaidMovementType.WITHDRAW);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidWithdraw
   * @param prepaidCard
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidWithdraw10 prepaidWithdraw, PrepaidCard10 prepaidCard) {
    return buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard, null, PrepaidMovementType.WITHDRAW);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidWithdraw
   * @param cdtTransaction
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidWithdraw10 prepaidWithdraw, CdtTransaction10 cdtTransaction) {
    return buildPrepaidMovement10(prepaidUser, prepaidWithdraw, null, cdtTransaction, PrepaidMovementType.WITHDRAW);
  }


  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param prepaidCard
   * @param cdtTransaction
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovementType type) {

    String codent = null;
    try {
      codent = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura;
    if(PrepaidMovementType.TOPUP.equals(type)) {
      tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
    } else {
      tipoFactura = TipoFactura.RETIRO_TRANSFERENCIA;
    }

    if (prepaidTopup != null) {
      if (TransactionOriginType.POS.equals(prepaidTopup.getTransactionOriginType())) {
        if (PrepaidMovementType.TOPUP.equals(type)) {
          tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
        } else {
          tipoFactura = TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA;
        }
      }
    }

    String centalta = "";
    String cuenta = "";
    if(prepaidCard != null && !StringUtils.isBlank(prepaidCard.getProcessorUserId())) {
      centalta = prepaidCard.getProcessorUserId().substring(4, 8);
      cuenta = prepaidCard.getProcessorUserId().substring(12);
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(cdtTransaction != null ? cdtTransaction.getTransactionReference() : getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(cdtTransaction != null ? cdtTransaction.getExternalTransactionId() : getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(type);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.OK);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(centalta); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(cuenta); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
    prepaidMovement.setImpliq(0L); // se debe actualizar despues
    prepaidMovement.setClamonliq(0); // se debe actualizar despues
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setNompob(""); // se debe actualizar despues
    prepaidMovement.setNumextcta(0); // se debe actualizar despues
    prepaidMovement.setNummovext(0); // se debe actualizar despues
    prepaidMovement.setClamone(0); // se debe actualizar despues
    prepaidMovement.setTipolin(""); // se debe actualizar despues
    prepaidMovement.setLinref(0); // se debe actualizar despues
    prepaidMovement.setNumbencta(1); // se debe actualizar despues
    prepaidMovement.setNumplastico(0L); // se debe actualizar despues
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement.setOriginType(MovementOriginType.API);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.OK);

    return prepaidMovement;
  }
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovementType type,PrepaidMovementStatus status) {

    String codent = null;
    try {
      codent = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura;
    if(PrepaidMovementType.TOPUP.equals(type)) {
      tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
    } else {
      tipoFactura = TipoFactura.RETIRO_TRANSFERENCIA;
    }

    if (prepaidTopup != null) {
      if (TransactionOriginType.POS.equals(prepaidTopup.getTransactionOriginType())) {
        if (PrepaidMovementType.TOPUP.equals(type)) {
          tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
        } else {
          tipoFactura = TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA;
        }
      }
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(cdtTransaction != null ? cdtTransaction.getTransactionReference() : getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(cdtTransaction != null ? cdtTransaction.getExternalTransactionId() : getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(type);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(status);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.OK);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(""); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(""); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
    prepaidMovement.setImpliq(0L); // se debe actualizar despues
    prepaidMovement.setClamonliq(0); // se debe actualizar despues
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setNompob(""); // se debe actualizar despues
    prepaidMovement.setNumextcta(0); // se debe actualizar despues
    prepaidMovement.setNummovext(0); // se debe actualizar despues
    prepaidMovement.setClamone(0); // se debe actualizar despues
    prepaidMovement.setTipolin(""); // se debe actualizar despues
    prepaidMovement.setLinref(0); // se debe actualizar despues
    prepaidMovement.setNumbencta(1); // se debe actualizar despues
    prepaidMovement.setNumplastico(0L); // se debe actualizar despues
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement.setOriginType(MovementOriginType.API);
    return prepaidMovement;
  }
  /*
    REVERSAS
   */

  public PrepaidMovement10 buildReversePrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidTopup10 reverseRequest) {
    return buildReversePrepaidMovement10(prepaidUser, reverseRequest, null, PrepaidMovementType.TOPUP);
  }

  public PrepaidMovement10 buildReversePrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidWithdraw10 reverseRequest) {
    return buildReversePrepaidMovement10(prepaidUser, reverseRequest, null, PrepaidMovementType.WITHDRAW);
  }

  public PrepaidMovement10 buildReversePrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 reverseRequest, PrepaidCard10 prepaidCard, PrepaidMovementType type) {

    String codent = null;
    try {
      codent = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura;
    if(PrepaidMovementType.TOPUP.equals(type)){
      tipoFactura = TipoFactura.ANULA_CARGA_TRANSFERENCIA;
    } else {
      tipoFactura = TipoFactura.ANULA_RETIRO_TRANSFERENCIA;
    }

    if (reverseRequest != null) {
      if (TransactionOriginType.POS.equals(reverseRequest.getTransactionOriginType())) {
        if(PrepaidMovementType.TOPUP.equals(type)){
          tipoFactura = TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA;
        } else {
          tipoFactura = TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA;
        }
      }
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(reverseRequest.getTransactionId());
    prepaidMovement.setTipoMovimiento(type);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.OK);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(""); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(""); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.CORRECTORA); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(reverseRequest != null ? reverseRequest.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(getRandomNumericString(6)); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(reverseRequest != null ? reverseRequest.getMerchantCode() : null);
    prepaidMovement.setCodact(reverseRequest != null ? reverseRequest.getMerchantCategory() : null);
    prepaidMovement.setImpliq(0L); // se debe actualizar despues
    prepaidMovement.setClamonliq(0); // se debe actualizar despues
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setNompob(""); // se debe actualizar despues
    prepaidMovement.setNumextcta(0); // se debe actualizar despues
    prepaidMovement.setNummovext(0); // se debe actualizar despues
    prepaidMovement.setClamone(0); // se debe actualizar despues
    prepaidMovement.setTipolin(""); // se debe actualizar despues
    prepaidMovement.setLinref(0); // se debe actualizar despues
    prepaidMovement.setNumbencta(1); // se debe actualizar despues
    prepaidMovement.setNumplastico(0L); // se debe actualizar despues
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement.setOriginType(MovementOriginType.API);

    return prepaidMovement;
  }

  /**
   *
   * @param prepaidMovement10
   * @return
   * @throws Exception
   */
  public PrepaidMovement10 createPrepaidMovement10(PrepaidMovement10 prepaidMovement10) throws Exception {

    prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);

    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId() > 0);

    return prepaidMovement10;
  }

  /**
   *
   * @param user
   * @return
   */
  public AltaClienteDTO registerInTecnocom(User user) throws BaseException {

    if (user == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user"));
    }

    if (user.getName() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.name"));
    }

    if (user.getLastname_1() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.lastname_1"));
    }

    if (user.getLastname_2() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.lastname_2"));
    }

    if (user.getRut() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.rut"));
    }

    if (user.getRut().getValue() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.rut.value"));
    }

    return getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, TipoAlta.NIVEL2);
  }

  /**
   *
   * @param prepaidCard10
   * @return
   */
  public InclusionMovimientosDTO topupInTecnocom(PrepaidCard10 prepaidCard10, BigDecimal impfac) throws BaseException {

    if (prepaidCard10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10"));
    }

    if (impfac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }

    if (StringUtils.isBlank(prepaidCard10.getProcessorUserId())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10.processorUserId"));
    }

    if (StringUtils.isBlank(prepaidCard10.getPan())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10.pan"));
    }

    String contrato = prepaidCard10.getProcessorUserId();
    String pan = prepaidCard10.getPan();
    CodigoMoneda clamon = CodigoMoneda.CHILE_CLP;
    IndicadorNormalCorrector indnorcor = IndicadorNormalCorrector.NORMAL;
    TipoFactura tipofac = TipoFactura.CARGA_TRANSFERENCIA;
    String codcom = "1";
    Integer codact = 1;
    CodigoMoneda clamondiv = CodigoMoneda.NONE;
    String nomcomred = "prueba";
    String numreffac = getUniqueLong().toString();
    String numaut = TecnocomServiceHelper.getNumautFromIdMov(numreffac);

    InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
      numreffac, impfac, numaut, codcom,
      nomcomred, codact, clamondiv,impfac);

    return inclusionMovimientosDTO;
  }

  public InclusionMovimientosDTO inclusionMovimientosTecnocom(PrepaidCard10 prepaidCard10, PrepaidMovement10 movement10) throws BaseException {

    if (prepaidCard10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10"));
    }

    if (movement10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }

    if (StringUtils.isBlank(prepaidCard10.getProcessorUserId())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10.processorUserId"));
    }

    if (StringUtils.isBlank(prepaidCard10.getPan())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10.pan"));
    }

    String numaut = TecnocomServiceHelper.getNumautFromIdMov(movement10.getId().toString());

    InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(prepaidCard10.getProcessorUserId(), EncryptUtil.getInstance().decrypt(prepaidCard10.getEncryptedPan()),
      movement10.getClamon(),movement10.getIndnorcor(), movement10.getTipofac(), movement10.getNumreffac(), movement10.getImpfac(), numaut, movement10.getCodcom(),
      movement10.getCodcom(), movement10.getCodact(), CodigoMoneda.fromValue(movement10.getClamondiv()),movement10.getImpfac());
    return inclusionMovimientosDTO;
  }
  /**
   * Espera por 10 intentos cada 1 segundo la existencia de una tarjeta del cliente prepago
   *
   * @param prepaidUser10
   * @param status
   * @return
   * @throws Exception
   */
  protected PrepaidCard10 waitForLastPrepaidCardInStatus(PrepaidUser10 prepaidUser10, PrepaidCardStatus status) throws Exception {

    PrepaidCard10 prepaidCard10 = null;

    //Espera por que la tarjeta se encuentre activa
    for(int j = 0; j < 10; j++) {

      Thread.sleep(1000);

      prepaidCard10 = getPrepaidCardEJBBean10().getLastPrepaidCardByUserId(null, prepaidUser10.getId());

      if (prepaidCard10 != null && status.equals(prepaidCard10.getStatus())) {
        break;
      }
    }

    return prepaidCard10;
  }

  protected UserAccount createBankAccount(User user) throws Exception {
    return createBankAccount(user, 1L, "Cuenta corriente", "mi cuenta de test", getRandomNumericString(8), user.getRut().getValue());
  }

  protected UserAccount createBankAccount(User user, Long bankId, String type, String name, String number, Integer rut) throws Exception {
    UserAccountNew newAccountRequest = new UserAccountNew();
    newAccountRequest.setBankId(bankId);
    newAccountRequest.setAccountType(type);
    newAccountRequest.setAccountAlias(name);
    newAccountRequest.setAccountNumber(number);
    newAccountRequest.setRut(rut);
    UserAccount newAccount = getUserClient().createUserBankAccount(null, user.getId(), newAccountRequest);
    return newAccount;
  }

  public Map<String,Object> getDefaultHeaders(){
    Map<String,Object> header = new HashMap<>();
    header.put(cl.multicaja.core.utils.Constants.HEADER_USER_LOCALE, cl.multicaja.core.utils.Constants.DEFAULT_LOCALE.toString());
    header.put(Constants.HEADER_USER_TIMEZONE,"America/Santiago");
    return header;
  }
  protected Accounting10 buildRandomAccouting(){
    Accounting10 accounting10 = new Accounting10();
    accounting10.setTransactionDate(new Timestamp((new Date()).getTime()));
    accounting10.setOrigin(AccountingOriginType.IPM);
    accounting10.setType(AccountingTxType.COMPRA_SUSCRIPCION);
    accounting10.setIdTransaction(getUniqueLong());
    accounting10.setFeeIva(new BigDecimal(getUniqueInteger()));
    accounting10.setFee(new BigDecimal(getUniqueInteger()));
    accounting10.setExchangeRateDif(new BigDecimal(getUniqueInteger()));

    accounting10.setConciliationDate(new Timestamp((new Date()).getTime()));
    accounting10.setAccountingMovementType(AccountingMovementType.COMPRA_MONEDA);
    accounting10.setCollectorFee(new BigDecimal(getUniqueInteger()));
    accounting10.setCollectorFeeIva(new BigDecimal(getUniqueInteger()));

    NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
    amountBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amountBalance.setValue(new BigDecimal(getUniqueInteger()));
    accounting10.setAmountBalance(amountBalance);
    accounting10.setStatus(AccountingStatusType.OK);
    accounting10.setFileId(0L);

    NewAmountAndCurrency10 amountMcar = new NewAmountAndCurrency10();
    amountMcar.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amountMcar.setValue(new BigDecimal(getUniqueInteger()));
    accounting10.setAmountMastercard(amountMcar);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal(getUniqueInteger()));
    accounting10.setAmount(amount);

    NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
    amountUsd.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amountUsd.setValue(new BigDecimal(getUniqueInteger()));
    accounting10.setAmountUsd(amountUsd);
    return accounting10;
  }
  protected List<Accounting10> generateRandomAccountingList(Integer iPositionNull, Integer count){
    List<Accounting10> accounting10s = new ArrayList<>();
    for(int i = 0;i<count;i++){
       Accounting10 accounting10 = new Accounting10();
       if(iPositionNull == null){
         accounting10.setTransactionDate(new Timestamp((new Date()).getTime()));
         accounting10.setOrigin(AccountingOriginType.IPM);
         accounting10.setType(AccountingTxType.COMPRA_SUSCRIPCION);
         accounting10.setIdTransaction(getUniqueLong());
         accounting10.setFeeIva(new BigDecimal(getUniqueInteger()));
         accounting10.setFee(new BigDecimal(getUniqueInteger()));
         accounting10.setExchangeRateDif(new BigDecimal(getUniqueInteger()));

         accounting10.setConciliationDate(new Timestamp((new Date()).getTime()));
         accounting10.setAccountingMovementType(AccountingMovementType.COMPRA_MONEDA);
         accounting10.setCollectorFee(new BigDecimal(getUniqueInteger()));
         accounting10.setCollectorFeeIva(new BigDecimal(getUniqueInteger()));

         NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
         amountBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);
         amountBalance.setValue(new BigDecimal(getUniqueInteger()));
         accounting10.setAmountBalance(amountBalance);
         accounting10.setStatus(AccountingStatusType.OK);
         accounting10.setFileId(0L);

         NewAmountAndCurrency10 amountMcar = new NewAmountAndCurrency10();
         amountMcar.setCurrencyCode(CodigoMoneda.CHILE_CLP);
         amountMcar.setValue(new BigDecimal(getUniqueInteger()));
         accounting10.setAmountMastercard(amountMcar);

         NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
         amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
         amount.setValue(new BigDecimal(getUniqueInteger()));
         accounting10.setAmount(amount);

         NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
         amountUsd.setCurrencyCode(CodigoMoneda.CHILE_CLP);
         amountUsd.setValue(new BigDecimal(getUniqueInteger()));
         accounting10.setAmountUsd(amountUsd);

       }
       else{

         if(iPositionNull == 1){

           accounting10.setIdTransaction(null);
           accounting10.setTransactionDate(new Timestamp((new Date()).getTime()));
           accounting10.setOrigin(AccountingOriginType.IPM);
           accounting10.setType(AccountingTxType.COMPRA_SUSCRIPCION);

           accounting10.setFeeIva(new BigDecimal(getUniqueInteger()));
           accounting10.setFee(new BigDecimal(getUniqueInteger()));
           accounting10.setExchangeRateDif(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amount);

           NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amountUsd);

           accounting10.setConciliationDate(new Timestamp((new Date()).getTime()));
           accounting10.setAccountingMovementType(AccountingMovementType.COMPRA_MONEDA);
           accounting10.setCollectorFee(new BigDecimal(getUniqueInteger()));
           accounting10.setCollectorFeeIva(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
           amountBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountBalance.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountBalance(amountBalance);
           accounting10.setStatus(AccountingStatusType.OK);
           accounting10.setFileId(0L);

           NewAmountAndCurrency10 amountMcar = new NewAmountAndCurrency10();
           amountMcar.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountMcar.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountMastercard(amountMcar);
         }
         else if(iPositionNull == 2){
           accounting10.setType(null);
           accounting10.setTransactionDate(new Timestamp((new Date()).getTime()));
           accounting10.setOrigin(AccountingOriginType.IPM);

           accounting10.setIdTransaction(getUniqueLong());
           accounting10.setFeeIva(new BigDecimal(getUniqueInteger()));
           accounting10.setFee(new BigDecimal(getUniqueInteger()));
           accounting10.setExchangeRateDif(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amount);

           NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amountUsd);

           accounting10.setConciliationDate(new Timestamp((new Date()).getTime()));
           accounting10.setAccountingMovementType(AccountingMovementType.COMPRA_MONEDA);
           accounting10.setCollectorFee(new BigDecimal(getUniqueInteger()));
           accounting10.setCollectorFeeIva(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
           amountBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountBalance.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountBalance(amountBalance);
           accounting10.setStatus(AccountingStatusType.OK);
           accounting10.setFileId(0L);

           NewAmountAndCurrency10 amountMcar = new NewAmountAndCurrency10();
           amountMcar.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountMcar.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountMastercard(amountMcar);
         }
         else if(iPositionNull == 3){
           accounting10.setOrigin(null);
           accounting10.setTransactionDate(new Timestamp((new Date()).getTime()));

           accounting10.setType(AccountingTxType.COMPRA_SUSCRIPCION);
           accounting10.setIdTransaction(getUniqueLong());
           accounting10.setFeeIva(new BigDecimal(getUniqueInteger()));
           accounting10.setFee(new BigDecimal(getUniqueInteger()));
           accounting10.setExchangeRateDif(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amount);

           NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amountUsd);

           accounting10.setConciliationDate(new Timestamp((new Date()).getTime()));
           accounting10.setAccountingMovementType(AccountingMovementType.COMPRA_MONEDA);
           accounting10.setCollectorFee(new BigDecimal(getUniqueInteger()));
           accounting10.setCollectorFeeIva(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
           amountBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountBalance.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountBalance(amountBalance);
           accounting10.setStatus(AccountingStatusType.OK);
           accounting10.setFileId(0L);

           NewAmountAndCurrency10 amountMcar = new NewAmountAndCurrency10();
           amountMcar.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountMcar.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountMastercard(amountMcar);
         }
         else {
           accounting10.setTransactionDate(null);
           accounting10.setOrigin(AccountingOriginType.IPM);
           accounting10.setType(AccountingTxType.COMPRA_SUSCRIPCION);
           accounting10.setIdTransaction(getUniqueLong());
           accounting10.setFeeIva(new BigDecimal(getUniqueInteger()));
           accounting10.setFee(new BigDecimal(getUniqueInteger()));
           accounting10.setExchangeRateDif(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amount);

           NewAmountAndCurrency10 amountUsd = new NewAmountAndCurrency10();
           amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amount.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmount(amountUsd);

           accounting10.setConciliationDate(new Timestamp((new Date()).getTime()));
           accounting10.setAccountingMovementType(AccountingMovementType.COMPRA_MONEDA);
           accounting10.setCollectorFee(new BigDecimal(getUniqueInteger()));
           accounting10.setCollectorFeeIva(new BigDecimal(getUniqueInteger()));

           NewAmountAndCurrency10 amountBalance = new NewAmountAndCurrency10();
           amountBalance.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountBalance.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountBalance(amountBalance);
           accounting10.setStatus(AccountingStatusType.OK);
           accounting10.setFileId(0L);

           NewAmountAndCurrency10 amountMcar = new NewAmountAndCurrency10();
           amountMcar.setCurrencyCode(CodigoMoneda.CHILE_CLP);
           amountMcar.setValue(new BigDecimal(getUniqueInteger()));
           accounting10.setAmountMastercard(amountMcar);
         }
       }
      accounting10s.add(accounting10);
    }
    return accounting10s;
  }
}
