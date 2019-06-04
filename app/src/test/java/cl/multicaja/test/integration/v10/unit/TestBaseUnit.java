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
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.*;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.prepaid.async.v10.*;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.ejb.v11.PrepaidMovementEJBBean11;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.EncryptHelper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.*;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
  private static PrepaidEJBBean10 prepaidEJBBean10;
  private static PrepaidMovementEJBBean10 prepaidMovementEJBBean10;
  private static PrepaidMovementEJBBean11 prepaidMovementEJBBean11;
  private static MailPrepaidEJBBean10 mailPrepaidEJBBean10;
  private static FilesEJBBean10 filesEJBBean10;
  private static ReprocesQueueDelegate10 reprocesQueueDelegate10;
  private static ProductChangeDelegate10 productChangeDelegate10;
  private static PrepaidAccountingEJBBean10 prepaidAccountingEJBBean10;
  private static TecnocomReconciliationEJBBean10 tecnocomReconciliationEJBBean10;
  private static McRedReconciliationEJBBean10 mcRedReconciliationEJBBean10;
  private static ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10;
  private static MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10;
  private static PrepaidAccountingFileEJBBean10 prepaidAccountingFileEJB10;
  private static PrepaidClearingEJBBean10 prepaidClearingEJBBean10;
  private static BackofficeEJBBean10 backofficeEJBBEan10;
  private static MailDelegate10 mailDelegate;
  private static PrepaidInvoiceDelegate10 prepaidInvoiceDelegate10;
  private static KafkaEventDelegate10 kafkaEventDelegate10;
  private static AccountEJBBean10 accountEJBBean10;
  private static PrepaidCardEJBBean11 prepaidCardEJBBean11;
  private static IpmEJBBean10 ipmEJBBean10;

  protected static CalculationsHelper calculationsHelper = CalculationsHelper.getInstance();
  {
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

  public static CalculationsHelper getCalculationsHelper(){
    return  CalculationsHelper.getInstance();
  }

  /**
   *Get Schema Prepaid 
   * @return
   */
  public static String getSchema() {
    return getPrepaidCardEJBBean11().getSchema();
  }

  public static String getSchemaAccounting() {
    return getPrepaidCardEJBBean11().getSchemaAccounting();
  }

  /**
   *
   * @return
   */
  public static DBUtils getDbUtils() {
    return getPrepaidCardEJBBean11().getDbUtils();
  }
  public CalculatorParameter10 getPercentage(){
   return getCalculationsHelper().getCalculatorParameter10();
  }

  public static MailDelegate10 getMailDelegate() {
    if(mailDelegate == null) {
      mailDelegate = new MailDelegate10();
    }
    return mailDelegate;
  }
  public static PrepaidInvoiceDelegate10 getInvoiceDelegate10(){
    if(prepaidInvoiceDelegate10 == null) {
      prepaidInvoiceDelegate10 = new PrepaidInvoiceDelegate10();
    }
    return prepaidInvoiceDelegate10;
  }

  public static KafkaEventDelegate10 getKafkaEventDelegate10() {
    if(kafkaEventDelegate10 == null) {
      kafkaEventDelegate10 = new KafkaEventDelegate10();
    }
    return kafkaEventDelegate10;
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
      prepaidMovementEJBBean10.setPrepaidCardEJB11(getPrepaidCardEJBBean11());
      prepaidMovementEJBBean10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      prepaidMovementEJBBean10.setPrepaidAccountingEJB10(getPrepaidAccountingEJBBean10());
      prepaidMovementEJBBean10.setMailDelegate(getMailDelegate());
      prepaidMovementEJBBean10.setPrepaidClearingEJB10(getPrepaidClearingEJBBean10());
      prepaidMovementEJBBean10.setMailPrepaidEJBBean10(getMailPrepaidEJBBean10());
      prepaidMovementEJBBean10.setTecnocomReconciliationEJBBean(getTecnocomReconciliationEJBBean10());
      prepaidMovementEJBBean10.setMcRedReconciliationEJBBean(getMcRedReconciliationEJBBean10());
      prepaidMovementEJBBean10.setReconciliationFilesEJBBean10(getReconciliationFilesEJBBean10());


    }
    return prepaidMovementEJBBean10;
  }

  public static PrepaidMovementEJBBean11 getPrepaidMovementEJBBean11(){
    if (prepaidMovementEJBBean11 == null) {
      prepaidMovementEJBBean11 = new PrepaidMovementEJBBean11();
      prepaidMovementEJBBean11.setKafkaEventDelegate10(getKafkaEventDelegate10());
      prepaidMovementEJBBean11.setDelegate(getPrepaidTopupDelegate10());
      prepaidMovementEJBBean11.setPrepaidUserEJB10(getPrepaidUserEJBBean10());
      prepaidMovementEJBBean11.setCdtEJB10(getCdtEJBBean10());
      prepaidMovementEJBBean11.setPrepaidCardEJB11(getPrepaidCardEJBBean11());
      prepaidMovementEJBBean11.setPrepaidEJBBean10(getPrepaidEJBBean10());
      prepaidMovementEJBBean11.setPrepaidAccountingEJB10(getPrepaidAccountingEJBBean10());
      prepaidMovementEJBBean11.setMailDelegate(getMailDelegate());
      prepaidMovementEJBBean11.setPrepaidClearingEJB10(getPrepaidClearingEJBBean10());
      prepaidMovementEJBBean11.setMailPrepaidEJBBean10(getMailPrepaidEJBBean10());
      prepaidMovementEJBBean11.setTecnocomReconciliationEJBBean(getTecnocomReconciliationEJBBean10());
      prepaidMovementEJBBean11.setMcRedReconciliationEJBBean(getMcRedReconciliationEJBBean10());
      prepaidMovementEJBBean11.setReconciliationFilesEJBBean10(getReconciliationFilesEJBBean10());
      prepaidMovementEJBBean11.setAccountEJBBean10(getAccountEJBBean10());
    }
    return prepaidMovementEJBBean11;
  }

  public static AccountEJBBean10 getAccountEJBBean10(){
    if (accountEJBBean10 == null) {
      accountEJBBean10 = new AccountEJBBean10();
      accountEJBBean10.setKafkaEventDelegate10(getKafkaEventDelegate10());
      accountEJBBean10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      accountEJBBean10.setPrepaidCardEJBBean11(getPrepaidCardEJBBean11());
    }
    return accountEJBBean10;
  }

  /**
   *
   * @return
   */
  public static PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    if (prepaidUserEJBBean10 == null) {
      prepaidUserEJBBean10 = new PrepaidUserEJBBean10();
      prepaidUserEJBBean10.setPrepaidCardEJB11(getPrepaidCardEJBBean11());
      prepaidUserEJBBean10.setPrepaidMovementEJB10(getPrepaidMovementEJBBean10());
      prepaidUserEJBBean10.setAccountEJBBean10(getAccountEJBBean10());
    }
    return prepaidUserEJBBean10;
  }

  public static PrepaidCardEJBBean11 getPrepaidCardEJBBean11() {
    if (prepaidCardEJBBean11 == null) {
      prepaidCardEJBBean11 = new PrepaidCardEJBBean11();
      prepaidCardEJBBean11.setKafkaEventDelegate10(getKafkaEventDelegate10());
      prepaidCardEJBBean11.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
    }
    return prepaidCardEJBBean11;
  }

  public static MailPrepaidEJBBean10 getMailPrepaidEJBBean10(){

    if (mailPrepaidEJBBean10 == null) {
      mailPrepaidEJBBean10 = new MailPrepaidEJBBean10();
      mailPrepaidEJBBean10.setPrepaidCardEJBBean11(getPrepaidCardEJBBean11());
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
      prepaidClearingEJBBean10.setPrepaidAccountingFileEJBBean10(getPrepaidAccountingFileEJBBean10());
      prepaidClearingEJBBean10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      prepaidClearingEJBBean10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
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
      prepaidEJBBean10.setPrepaidMovementEJB11(getPrepaidMovementEJBBean11());
      prepaidEJBBean10.setPrepaidUserEJB10(getPrepaidUserEJBBean10());
      prepaidEJBBean10.setPrepaidCardEJB11(getPrepaidCardEJBBean11());
      prepaidEJBBean10.setFilesEJBBean10(getFilesEJBBean10());
      prepaidEJBBean10.setDelegateReprocesQueue(getReprocesQueueDelegate10());
      prepaidEJBBean10.setProductChangeDelegate(getProductChangeDelegate10());
      prepaidEJBBean10.setMailDelegate(getMailDelegate());
      prepaidEJBBean10.setKafkaEventDelegate10(getKafkaEventDelegate10());
      prepaidEJBBean10.setAccountEJBBean10(getAccountEJBBean10());
    }
    return prepaidEJBBean10;
  }

  public static BackofficeEJBBean10 getBackofficeEJBBEan10() {
    if(backofficeEJBBEan10 == null) {
      backofficeEJBBEan10 = new BackofficeEJBBean10();
    }
    return backofficeEJBBEan10;
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
      prepaidAccountingEJBBean10.setPrepaidClearingEJBBean10(getPrepaidClearingEJBBean10());
      prepaidAccountingEJBBean10.setPrepaidAccountingFileEJBBean10(getPrepaidAccountingFileEJBBean10());
      prepaidAccountingEJBBean10.setPrepaidCardEJBBean11(getPrepaidCardEJBBean11());
      prepaidAccountingEJBBean10.setPrepaidMovementEJBBean11(getPrepaidMovementEJBBean11());
    }
    return prepaidAccountingEJBBean10;
  }

  public static TecnocomReconciliationEJBBean10 getTecnocomReconciliationEJBBean10() {
    if(tecnocomReconciliationEJBBean10 == null) {
      tecnocomReconciliationEJBBean10 = new TecnocomReconciliationEJBBean10();
      tecnocomReconciliationEJBBean10.setPrepaidCardEJBBean11(getPrepaidCardEJBBean11());
      tecnocomReconciliationEJBBean10.setPrepaidMovementEJBBean11(getPrepaidMovementEJBBean11());
      tecnocomReconciliationEJBBean10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      tecnocomReconciliationEJBBean10.setPrepaidAccountingEJBBean10(getPrepaidAccountingEJBBean10());
      tecnocomReconciliationEJBBean10.setPrepaidClearingEJBBean10(getPrepaidClearingEJBBean10());
      tecnocomReconciliationEJBBean10.setReconciliationFilesEJBBean10(getReconciliationFilesEJBBean10());
      tecnocomReconciliationEJBBean10.setPrepaidInvoiceDelegate10(getInvoiceDelegate10());
      tecnocomReconciliationEJBBean10.setAccountEJBBean10(getAccountEJBBean10());
      tecnocomReconciliationEJBBean10.setIpmEJBBean10(getIpmEJBBean10());
    }
    return tecnocomReconciliationEJBBean10;
  }

  public static McRedReconciliationEJBBean10 getMcRedReconciliationEJBBean10() {
    if(mcRedReconciliationEJBBean10 == null) {
      mcRedReconciliationEJBBean10 = new McRedReconciliationEJBBean10();
      mcRedReconciliationEJBBean10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      mcRedReconciliationEJBBean10.setReconciliationFilesEJBBean10(getReconciliationFilesEJBBean10());
      mcRedReconciliationEJBBean10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      mcRedReconciliationEJBBean10.setPrepaidInvoiceDelegate10(getInvoiceDelegate10());
      mcRedReconciliationEJBBean10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
    }
    return mcRedReconciliationEJBBean10;
  }

  public static ReconciliationFilesEJBBean10 getReconciliationFilesEJBBean10() {
    if(reconciliationFilesEJBBean10 == null) {
      reconciliationFilesEJBBean10 = new ReconciliationFilesEJBBean10();
    }
    return reconciliationFilesEJBBean10;
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

  public static IpmEJBBean10 getIpmEJBBean10() {
    if (ipmEJBBean10 == null) {
      ipmEJBBean10 = new IpmEJBBean10();
    }
    return ipmEJBBean10;
  }

  /**
   *
   * @return
   */
  @Deprecated
  public PrepaidUser10 buildPrepaidUser10() {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(getUniqueLong());
    prepaidUser.setRut(getUniqueRutNumber());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);
    return prepaidUser;
  }
  public PrepaidUser10 buildPrepaidUserv2() {
    return buildPrepaidUserv2(PrepaidUserLevel.LEVEL_1);
  }

  public PrepaidUser10 buildPrepaidUserv2(PrepaidUserLevel userLevel) {

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(getUniqueLong());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);
    prepaidUser.setDocumentNumber(getUniqueRutNumber().toString());
    prepaidUser.setRut(Integer.parseInt(prepaidUser.getDocumentNumber()));
    prepaidUser.setName(getRandomString(10));
    prepaidUser.setLastName(getRandomString(10));
    prepaidUser.setUuid(UUID.randomUUID().toString());
    prepaidUser.setDocumentType(DocumentType.DNI_CL);
    prepaidUser.setUserLevel(userLevel);
    prepaidUser.setUserPlan(UserPlanType.FREE);
    return prepaidUser;
  }

  public PrepaidMovementFee10 buildPrepaidMovementFee10(PrepaidMovement10 prepaidMovement10) {
    PrepaidMovementFee10 prepaidMovementFee = new PrepaidMovementFee10();
    prepaidMovementFee.setMovementId(prepaidMovement10.getId());
    prepaidMovementFee.setFeeType(PrepaidMovementFeeType.EXCHANGE_RATE_DIF);
    prepaidMovementFee.setAmount(new BigDecimal(numberUtils.random(3000L, 50000L)));
    prepaidMovementFee.setIva(prepaidMovementFee.getAmount().multiply(new BigDecimal(0.19)));
    return prepaidMovementFee;
  }

  public Account createRandomAccount(PrepaidUser10 prepaidUser) throws Exception {
    return accountEJBBean10.insertAccount(prepaidUser.getId(),getRandomNumericString(20));
  }

  /**
   *
   * @return
   */
  public PrepaidUser10 buildPrepaidUser11(){

    PrepaidUser10 user = new PrepaidUser10();

    Integer rutOrDocumentNumber = getUniqueRutNumber();

    user.setUserIdMc(Long.valueOf(getRandomNumericString(10)));
    user.setDocumentType(DocumentType.DNI_CL);
    user.setRut(rutOrDocumentNumber);
    user.setStatus(PrepaidUserStatus.ACTIVE);
    user.setName(getRandomString(10));
    user.setLastName(getRandomString(10));
    user.setDocumentNumber(rutOrDocumentNumber.toString());
    user.setUserLevel(PrepaidUserLevel.LEVEL_1);
    user.setUuid(UUID.randomUUID().toString());
    user.setUserPlan(UserPlanType.FREE);
    return user;
  }

  /**
   *
   * @param prepaidUser
   * @return
   * @throws Exception
   */
   @Deprecated
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


  @Deprecated
  public PrepaidCard10 buildPrepaidCard10(PrepaidUser10 prepaidUser,Long accountId) throws Exception {
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
    prepaidCard.setAccountId(accountId);
    return prepaidCard;
  }

  public PrepaidCard10 buildPrepaidCard11(PrepaidUser10 prepaidUser,Long accountId) throws Exception {
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
    prepaidCard.setAccountId(accountId);
    prepaidCard.setUuid(UUID.randomUUID().toString());
    prepaidCard.setHashedPan("");
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
  @Deprecated
  public PrepaidCard10 buildPrepaidCard10() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = getPrepaidUserEJBBean10().createPrepaidUser(null, prepaidUser);
    return buildPrepaidCard10(prepaidUser);
  }

  public Account buildAccountFromTecnocom(PrepaidUser10 prepaidUser) throws Exception {

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(prepaidUser.getName(), prepaidUser.getLastName(), "", prepaidUser.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);

    Account account = new Account();
    account.setUserId(prepaidUser.getId());
    account.setAccountNumber(altaClienteDTO.getContrato());
    account.setProcessor(AccountProcessor.TECNOCOM_CL.name());
    account.setUpdatedAt(LocalDateTime.now());
    account.setUpdatedAt(LocalDateTime.now());
    account.setUuid(UUID.randomUUID().toString());
    account.setStatus(AccountStatus.ACTIVE);
    account.setExpireBalance(0l);
    account.setBalanceInfo("");

    return account;
  }

  /**
   *
   * @param user
   * @param account
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCardWithTecnocomData(PrepaidUser10 user, Account account) throws Exception {
    DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(account.getAccountNumber());
    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(user.getId());
    prepaidCard.setProcessorUserId("");
    prepaidCard.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
    prepaidCard.setEncryptedPan(EncryptHelper.getInstance().encryptPan(datosTarjetaDTO.getPan()));
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setNameOnCard(user.getName() + " " + user.getLastName());
    prepaidCard.setProducto(datosTarjetaDTO.getProducto());
    prepaidCard.setNumeroUnico(datosTarjetaDTO.getIdentclitar());
    prepaidCard.setUuid(UUID.randomUUID().toString());
    prepaidCard.setAccountId(account.getId());
    prepaidCard.setHashedPan(getRandomString(20));
    return prepaidCard;
  }

  public PrepaidTopup10 buildPrepaidTopup10() {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getRandomNumericString(15);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(3238));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidTopup.setAmount(newAmountAndCurrency);
    prepaidTopup.setTotal(newAmountAndCurrency);
    prepaidTopup.setMerchantCategory(1);
    prepaidTopup.setMerchantName(getRandomString(6));


    return prepaidTopup;
  }


  public NewPrepaidTopup10 buildNewPrepaidTopup10() {

    String merchantCode = numberUtils.random() ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getRandomNumericString(15);

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(3238));

    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidTopup.setAmount(newAmountAndCurrency);

    prepaidTopup.setMerchantCategory(1);
    prepaidTopup.setMerchantName(getRandomString(6));

    return prepaidTopup;
  }

  public NewPrepaidWithdraw10 buildNewPrepaidWithdrawV2() throws Exception {
    return buildNewPrepaidWithdrawV2(getRandomNumericString(15));
  }

  public NewPrepaidWithdraw10 buildNewPrepaidWithdrawV2(String merchantCode) throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.setTransactionId(getUniqueInteger().toString());
    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(RandomUtils.nextLong(2000,9000)));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(newAmountAndCurrency);
    prepaidWithdraw.setMerchantCategory(1);
    prepaidWithdraw.setMerchantName(getRandomString(6));
    if(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE.equals(merchantCode)){
      UserAccount userAccount = randomBankAccount();
      prepaidWithdraw.setBankId(userAccount.getBankId());
      prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
      prepaidWithdraw.setAccountType(userAccount.getAccountType());
      prepaidWithdraw.setAccountRut(userAccount.getRut());
    }
    return prepaidWithdraw;
  }


  public PrepaidWithdraw10 buildPrepaidWithdrawV2(){

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();
    PrepaidWithdraw10 prepaidWithdraw = new PrepaidWithdraw10();
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


  public CdtTransaction10 buildCdtTransaction10(PrepaidUser10 user, PrepaidTopup10 prepaidTopup) throws BaseException {
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(prepaidTopup.getAmount().getValue());
    cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + user.getDocumentNumber());
    cdtTransaction.setGloss(prepaidTopup.getCdtTransactionType().getName()+" "+prepaidTopup.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(prepaidTopup.getTransactionId());
    cdtTransaction.setIndSimulacion(false);
    return cdtTransaction;
  }

  /**
   *
   * @param user
   * @param prepaidWithdraw
   * @return
   * @throws BaseException
   */
  public CdtTransaction10 buildCdtTransaction10(PrepaidUser10 user, PrepaidWithdraw10 prepaidWithdraw) throws BaseException {
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(prepaidWithdraw.getAmount().getValue());
    cdtTransaction.setTransactionType(prepaidWithdraw.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + user.getDocumentNumber());
    cdtTransaction.setGloss(prepaidWithdraw.getCdtTransactionType().getName()+" "+prepaidWithdraw.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(prepaidWithdraw.getTransactionId());
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
  @Deprecated
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
   * @param user
   * @throws BaseException
   */
  public PrepaidUser10 updatePrepaidUser(PrepaidUser10 user) throws Exception{
    if(user == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "TenpoUser"));
    }

    return getPrepaidUserEJBBean10().updatePrepaidUser(null, user);
  }

  /**
   *
   * @param uiid
   * @return
   * @throws Exception
   */
  public PrepaidUser10 findPrepaidUserByExtId(String uiid) throws Exception{

    return getPrepaidUserEJBBean10().findByExtId(null,uiid);
  }

  /**
   *
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  public PrepaidUser10 createPrepaidUserV2(PrepaidUser10 prepaidUser) throws Exception {

    prepaidUser = getPrepaidUserEJBBean10().createUser(null, prepaidUser);

    Assert.assertNotNull("debe retornar un usuario", prepaidUser);
    Assert.assertEquals("debe tener id", true, prepaidUser.getId() > 0);
    Assert.assertNotNull("debe tener status", prepaidUser.getStatus());
    Assert.assertNotNull("debe tener uuid", prepaidUser.getUuid());

    return prepaidUser;
  }

  public Account createAccount(Long userId, String accountNum) throws Exception {

    Account account = getAccountEJBBean10().insertAccount(userId,accountNum);

    Assert.assertNotNull("debe retornar un account", account);
    Assert.assertNotEquals("debe tener id", 0L, account.getId().longValue());

    return account;
  }

    /**
     *
     * @param prepaidCard
     * @return
     * @throws Exception
     */
    @Deprecated
  public PrepaidCard10 createPrepaidCard10(PrepaidCard10 prepaidCard) throws Exception {

    prepaidCard = getPrepaidCardEJBBean11().createPrepaidCard(null, prepaidCard);

    Assert.assertNotNull("debe retornar un usuario", prepaidCard);
    Assert.assertEquals("debe tener id", true, prepaidCard.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, prepaidCard.getIdUser() > 0);
    Assert.assertNotNull("debe tener status", prepaidCard.getStatus());

    return prepaidCard;
  }


  public PrepaidCard10 createPrepaidCardV2(PrepaidCard10 prepaidCard) throws Exception {

    prepaidCard = getPrepaidCardEJBBean11().createPrepaidCard(null, prepaidCard);

    Assert.assertNotNull("debe retornar una tarjeta", prepaidCard);
    Assert.assertEquals("debe tener id", true, prepaidCard.getId() > 0);
    Assert.assertNotNull("debe tener status", prepaidCard.getStatus());

    return prepaidCard;
  }

  public PrepaidMovementFee10 createPrepaidMovementFee10(PrepaidMovementFee10 fee) throws Exception {
    fee = getPrepaidMovementEJBBean11().addPrepaidMovementFee(fee);

    Assert.assertNotNull("debe retornar una comision", fee);
    Assert.assertEquals("debe tener id", true, fee.getId() > 0);
    return fee;
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
  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup,PrepaidCard10 prepaidCard10) throws Exception {
    return buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard10, null, PrepaidMovementType.TOPUP,false);
  }
  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup) throws Exception {
    return buildPrepaidMovement11(prepaidUser, prepaidTopup, null, null, PrepaidMovementType.TOPUP,false);
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

  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup, CdtTransaction10 cdtTransaction ) throws Exception {
    return buildPrepaidMovement11(prepaidUser, prepaidTopup, null, cdtTransaction, PrepaidMovementType.TOPUP,false);
  }


  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param prepaidCard
   * @param cdtTransaction
   * @return
   */
  @Deprecated
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
  }

  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction,PrepaidMovementStatus status) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP,status);
  }

  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction,PrepaidMovementStatus status) throws Exception {
    return buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP,status);
  }

  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction,PrepaidCard10 prepaidCard10) throws Exception {
    return buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP,false);
  }

  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction) throws Exception {
    return buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP,false);
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
  @Deprecated
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidWithdraw10 prepaidWithdraw) {
    return buildPrepaidMovement10(prepaidUser, prepaidWithdraw, null, null, PrepaidMovementType.WITHDRAW);
  }

  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, PrepaidWithdraw10 prepaidWithdraw) throws Exception {
    return buildPrepaidMovement11(prepaidUser, prepaidWithdraw, null, null, PrepaidMovementType.WITHDRAW,false);
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
  @Deprecated
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidWithdraw10 prepaidWithdraw, CdtTransaction10 cdtTransaction) {
    return buildPrepaidMovement10(prepaidUser, prepaidWithdraw, null, cdtTransaction, PrepaidMovementType.WITHDRAW);
  }


  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovementType type, Boolean isReverse) throws Exception {

    String codent = null;
    try {
      codent = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }
    TipoFactura tipoFactura;
    if(isReverse == null || !isReverse) {
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
    }else {
      if(PrepaidMovementType.TOPUP.equals(type)){
        tipoFactura = TipoFactura.ANULA_CARGA_TRANSFERENCIA;
      } else {
        tipoFactura = TipoFactura.ANULA_RETIRO_TRANSFERENCIA;
      }

      if (prepaidTopup != null) {
        if (TransactionOriginType.POS.equals(prepaidTopup.getTransactionOriginType())) {
          if(PrepaidMovementType.TOPUP.equals(type)){
            tipoFactura = TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA;
          } else {
            tipoFactura = TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA;
          }
        }
      }
    }

    Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());
    String centalta = "";
    String cuenta = "";
    if(account != null && !StringUtils.isBlank(account.getAccountNumber())) {
      String accountNumber = account.getAccountNumber();
      centalta = accountNumber.substring(4, 8);
      cuenta = accountNumber.substring(12);
    }

    if(prepaidCard == null) {
      System.out.println("llena tarjeta");
      prepaidCard = getPrepaidCardEJBBean11().getPrepaidCardByAccountId(account.getId());
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(cdtTransaction != null ? cdtTransaction.getTransactionReference() : getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());

    prepaidMovement.setIdTxExterno(prepaidTopup != null ? prepaidTopup.getTransactionId(): getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(type);
    prepaidMovement.setMonto(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(centalta); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(cuenta); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFactura.getCorrector())); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(java.util.Date.from(ZonedDateTime.now(ZoneId.of("UTC")).toInstant()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
    prepaidMovement.setImpliq(BigDecimal.ZERO); // se debe actualizar despues
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
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setNomcomred(prepaidTopup != null ? prepaidTopup.getMerchantName() != null ? prepaidTopup.getMerchantName() : getRandomString(10) : getRandomString(10));
    prepaidMovement.setCardId(prepaidCard != null ? prepaidCard.getId() : 0);
    return prepaidMovement;
  }

  @Deprecated
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
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
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
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
    prepaidMovement.setImpliq(BigDecimal.ZERO); // se debe actualizar despues
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
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setNomcomred(prepaidTopup != null ? prepaidTopup.getMerchantName() != null ? prepaidTopup.getMerchantName() : getRandomString(10) : getRandomString(10));

    return prepaidMovement;
  }

  @Deprecated
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
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
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
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
    prepaidMovement.setImpliq(BigDecimal.ZERO); // se debe actualizar despues
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
    prepaidMovement.setNomcomred(prepaidTopup != null ? prepaidTopup.getMerchantName() != null ? prepaidTopup.getMerchantName() : getRandomString(10) : getRandomString(10));
    return prepaidMovement;
  }

  public PrepaidMovement10 buildPrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovementType type,PrepaidMovementStatus status) throws Exception {

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
    Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());
    String centalta = "";
    String cuenta = "";
    if(account != null && !StringUtils.isBlank(account.getAccountNumber())) {
      String accountNumber = account.getAccountNumber();
      centalta = accountNumber.substring(4, 8);
      cuenta = accountNumber.substring(12);
    }
    if(prepaidCard == null){
      System.out.println("llena tarjeta");
      prepaidCard = getPrepaidCardEJBBean11().getPrepaidCardByAccountId(account.getId());
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(cdtTransaction != null ? cdtTransaction.getTransactionReference() : getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());

    prepaidMovement.setIdTxExterno(cdtTransaction != null ? cdtTransaction.getExternalTransactionId() : getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(type);
    prepaidMovement.setMonto(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(status);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
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
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    //prepaidMovement.setNumaut("");  //Este campo se calcula automaticamente al hacer insert a menos que se quiera uno especifico.
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
    prepaidMovement.setImpliq(BigDecimal.ZERO); // se debe actualizar despues
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
    prepaidMovement.setNomcomred(prepaidTopup != null ? prepaidTopup.getMerchantName() != null ? prepaidTopup.getMerchantName() : getRandomString(10) : getRandomString(10));
    prepaidMovement.setCardId(prepaidCard != null ? prepaidCard.getId() : 0);
    return prepaidMovement;
  }
  /*
    REVERSAS
   */

  public PrepaidMovement10 buildReversePrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidTopup10 reverseRequest) {
    return buildReversePrepaidMovement10(prepaidUser, reverseRequest, null, PrepaidMovementType.TOPUP);
  }
  public PrepaidMovement10 buildReversePrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidTopup10 reverseRequest,PrepaidCard10 prepaidCard10) throws Exception {
    return buildReversePrepaidMovement11(prepaidUser, reverseRequest, prepaidCard10, PrepaidMovementType.TOPUP);
  }
  public PrepaidMovement10 buildReversePrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidTopup10 reverseRequest) throws Exception {
    return buildReversePrepaidMovement11(prepaidUser, reverseRequest, null, PrepaidMovementType.TOPUP);
  }
  public PrepaidMovement10 buildReversePrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidWithdraw10 reverseRequest) {
    return buildReversePrepaidMovement10(prepaidUser, reverseRequest, null, PrepaidMovementType.WITHDRAW);
  }
  public PrepaidMovement10 buildReversePrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidWithdraw10 reverseRequest) throws Exception {
    return buildReversePrepaidMovement11(prepaidUser, reverseRequest, null, PrepaidMovementType.WITHDRAW);
  }
  @Deprecated
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
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
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
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(reverseRequest != null ? reverseRequest.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(getRandomNumericString(6)); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(reverseRequest != null ? reverseRequest.getMerchantCode() : null);
    prepaidMovement.setCodact(reverseRequest != null ? reverseRequest.getMerchantCategory() : null);
    prepaidMovement.setImpliq(BigDecimal.ZERO); // se debe actualizar despues
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
    prepaidMovement.setNomcomred(reverseRequest != null ? reverseRequest.getMerchantName() != null ? reverseRequest.getMerchantName() : getRandomString(10) : getRandomString(10));

    return prepaidMovement;
  }
  public PrepaidMovement10 buildReversePrepaidMovement11(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 reverseRequest, PrepaidCard10 prepaidCard, PrepaidMovementType type) throws Exception {
    return buildPrepaidMovement11(prepaidUser,reverseRequest,prepaidCard,null,type,true);
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

  public PrepaidMovement10 createPrepaidMovement11(PrepaidMovement10 prepaidMovement10) throws Exception {

    prepaidMovement10 = getPrepaidMovementEJBBean11().addPrepaidMovement(null, prepaidMovement10);

    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId() > 0);

    return prepaidMovement10;
  }

  public AltaClienteDTO registerInTecnocomV2(PrepaidUser10 user) throws BaseException {

    if (user == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user"));
    }

    if (user.getName() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.name"));
    }

    if (user.getLastName() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.lastname_1"));
    }

    if (user.getRut() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.rut"));
    }

    if (user.getDocumentNumber() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.rut.value"));
    }
    TipoAlta tipoAlta =  user.getUserLevel() == PrepaidUserLevel.LEVEL_1 ? TipoAlta.NIVEL1: TipoAlta.NIVEL2;
    return getTecnocomService().altaClientes(user.getName(), user.getLastName(), "", user.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);
  }

  /**
   *
   * @param prepaidCard10
   * @return
   */
  @Deprecated
  public InclusionMovimientosDTO topupInTecnocom(PrepaidCard10 prepaidCard10, BigDecimal impfac) throws BaseException {

    if (prepaidCard10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10"));
    }

    if (impfac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
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


  public InclusionMovimientosDTO topupInTecnocom(String accountNumber, PrepaidCard10 prepaidCard10, BigDecimal impfac) throws BaseException {

    if (prepaidCard10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10"));
    }

    if (impfac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(accountNumber == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountNumber"));
    }
    if (StringUtils.isBlank(prepaidCard10.getPan())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10.pan"));
    }

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

    InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(accountNumber, pan, clamon, indnorcor, tipofac,
      numreffac, impfac, numaut, codcom,
      nomcomred, codact, clamondiv,impfac);

    return inclusionMovimientosDTO;
  }

  @Deprecated
  public InclusionMovimientosDTO inclusionMovimientosTecnocom(PrepaidCard10 prepaidCard10, PrepaidMovement10 movement10) throws BaseException {

    if (prepaidCard10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10"));
    }

    if (movement10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
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

  public InclusionMovimientosDTO inclusionMovimientosTecnocom(Account account, PrepaidCard10 prepaidCard10, PrepaidMovement10 movement10) throws BaseException {

    if (prepaidCard10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10"));
    }

    if (movement10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }

    if (StringUtils.isBlank(prepaidCard10.getPan())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10.pan"));
    }

    String numaut = TecnocomServiceHelper.getNumautFromIdMov(movement10.getId().toString());

    InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(account.getAccountNumber(), EncryptUtil.getInstance().decrypt(prepaidCard10.getEncryptedPan()),
      movement10.getClamon(),movement10.getIndnorcor(), movement10.getTipofac(), movement10.getNumreffac(), movement10.getImpfac(), numaut, movement10.getCodcom(),
      movement10.getCodcom(), movement10.getCodact(), CodigoMoneda.fromValue(movement10.getClamondiv()),movement10.getImpfac());
    return inclusionMovimientosDTO;
  }

  /**
   * Espera por 10 intentos cada 1 segundo la existencia de una tarjeta del cliente prepago
   *
   * @param accountId
   * @param status
   * @return
   * @throws Exception
   */
  protected PrepaidCard10 waitForLastPrepaidCardInStatus(Long accountId, PrepaidCardStatus status) throws Exception {

    PrepaidCard10 prepaidCard10 = null;

    //Espera por que la tarjeta se encuentre activa
    for(int j = 0; j < 10; j++) {

      Thread.sleep(1000);

      prepaidCard10 = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountId(null, accountId);

      if (prepaidCard10 != null && status.equals(prepaidCard10.getStatus())) {
        break;
      }
    }

    return prepaidCard10;
  }

  protected PrepaidCard10 waitForLastPrepaidCardInStatusV11(Long userId, PrepaidCardStatus status) throws Exception {

    PrepaidCard10 prepaidCard10 = null;
    //Espera por que la tarjeta se encuentre activa
    for(int j = 0; j < 10; j++) {
      Thread.sleep(2000);
      prepaidCard10 = getPrepaidCardEJBBean11().getByUserIdAndStatus(null, userId,PrepaidCardStatus.ACTIVE);
      if(prepaidCard10 != null){
        break;
      }
    }
    return prepaidCard10;
  }
  protected UserAccount randomBankAccount(){
    UserAccount userAccount = new UserAccount();
    userAccount.setBankId(getUniqueRutNumber().longValue());
    userAccount.setAccountNumber(getUniqueRutNumber().longValue());
    userAccount.setAccountType("Vista");
    userAccount.setRut(getUniqueRutNumber().toString());
    return userAccount;
  }

  public Map<String,Object> getDefaultHeaders(){
    Map<String,Object> header = new HashMap<>();
    header.put(cl.multicaja.core.utils.Constants.HEADER_USER_LOCALE, cl.multicaja.core.utils.Constants.DEFAULT_LOCALE.toString());
    header.put(Constants.HEADER_USER_TIMEZONE,"America/Santiago");
    return header;
  }

  protected AccountingData10 buildRandomAccouting(AccountingTxType accountingTxType){
    AccountingData10 accounting10 = new AccountingData10();
    accounting10.setTransactionDate(new Timestamp((new Date()).getTime()));
    accounting10.setOrigin(AccountingOriginType.IPM);
    accounting10.setType(accountingTxType);
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
    accounting10.setAccountingStatus(AccountingStatusType.OK);
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
  protected AccountingData10 buildRandomAccouting(){
    AccountingData10 accounting10 = new AccountingData10();
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
    accounting10.setAccountingStatus(AccountingStatusType.OK);
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
  protected List<AccountingData10> generateRandomAccountingList(Integer iPositionNull, Integer count){
    List<AccountingData10> accounting10s = new ArrayList<>();
    for(int i = 0;i<count;i++){
       AccountingData10 accounting10 = new AccountingData10();
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
         accounting10.setAccountingStatus(AccountingStatusType.OK);
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
  protected static EntityManager createEntityManager() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(ConfigUtils.getEnv());
    EntityManager em = emf.createEntityManager();
    return em;
  }

  public boolean isRecentLocalDateTime(LocalDateTime localDateTime, Integer minutesInterval) {
    LocalDateTime nowTime = LocalDateTime.now(ZoneId.of("UTC"));
    return nowTime.minusMinutes(minutesInterval).isBefore(localDateTime) && nowTime.plusMinutes(minutesInterval).isAfter(localDateTime);
  }

  public IpmMovement10 buildIpmMovement10() {
    IpmMovement10 ipmMovement10 = new IpmMovement10();
    ipmMovement10.setFileId(RandomUtils.nextLong());
    ipmMovement10.setMessageType(RandomUtils.nextInt(0, 15));
    ipmMovement10.setFunctionCode(RandomUtils.nextInt(0, 15));
    ipmMovement10.setMessageReason(RandomUtils.nextInt(0, 15));
    ipmMovement10.setMessageNumber(RandomUtils.nextInt(0, 15));
    ipmMovement10.setPan(getRandomString(12));
    ipmMovement10.setTransactionAmount(new BigDecimal(RandomUtils.nextInt(3000, 100000)));
    ipmMovement10.setReconciliationAmount(new BigDecimal(RandomUtils.nextInt(3000, 100000)));
    ipmMovement10.setCardholderBillingAmount(new BigDecimal(RandomUtils.nextInt(3000, 100000)));
    ipmMovement10.setReconciliationConversionRate(new BigDecimal(RandomUtils.nextInt(3000, 100000)));
    ipmMovement10.setCardholderBillingConversionRate(new BigDecimal(RandomUtils.nextInt(3000, 100000)));
    ipmMovement10.setTransactionLocalDate(LocalDateTime.now(ZoneId.of("UTC")));
    ipmMovement10.setApprovalCode(getRandomNumericString(6));
    ipmMovement10.setTransactionCurrencyCode(RandomUtils.nextInt(0, 100));
    ipmMovement10.setReconciliationCurrencyCode(RandomUtils.nextInt(0, 100));
    ipmMovement10.setCardholderBillingCurrencyCode(RandomUtils.nextInt(0, 100));
    ipmMovement10.setMerchantCode(getRandomNumericString(15));
    ipmMovement10.setMerchantName(getRandomString(12));
    ipmMovement10.setMerchantState(getRandomString(5));
    ipmMovement10.setMerchantCountry(getRandomString(2));
    ipmMovement10.setTransactionLifeCycleId(getRandomNumericString(5));
    ipmMovement10.setReconciled(false);
    Timestamps timestamps = new Timestamps();
    timestamps.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
    timestamps.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));
    ipmMovement10.setTimestamps(timestamps);
    return ipmMovement10;
  }

  public IpmMovement10 createIpmMovement(IpmMovement10 movement) throws Exception {
    String insertMovementSql = String.format(
      "INSERT INTO %s.ipm_file_data (" +
        "  file_id, " +
        "  message_type, " +
        "  function_code, " +
        "  message_reason, " +
        "  message_number, " +
        "  pan, " +
        "  transaction_amount, " +
        "  reconciliation_amount, " +
        "  cardholder_billing_amount, " +
        "  reconciliation_conversion_rate, " +
        "  cardholder_billing_conversion_rate, " +
        "  transaction_local_date, " +
        "  approval_code, " +
        "  transaction_currency_code, " +
        "  reconciliation_currency_code, " +
        "  cardholder_billing_currency_code, " +
        "  merchant_code, " +
        "  merchant_name, " +
        "  merchant_state, " +
        "  merchant_country, " +
        "  transaction_life_cycle_id, " +
        "  reconciled, " +
        "  created_at, " +
        "  updated_at " +
        ") VALUES (" +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ?, " +
        "  ? " +
        ")", getSchemaAccounting());

    KeyHolder keyHolder = new GeneratedKeyHolder();

    getDbUtils().getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection.prepareStatement(insertMovementSql, new String[] {"id"});
      ps.setLong(1, movement.getFileId());
      ps.setLong(2, movement.getMessageType());
      ps.setInt(3, movement.getFunctionCode());
      ps.setInt(4, movement.getMessageReason());
      ps.setInt(5, movement.getMessageNumber());
      ps.setString(6, movement.getPan());
      ps.setBigDecimal(7, movement.getTransactionAmount());
      ps.setBigDecimal(8, movement.getReconciliationAmount());
      ps.setBigDecimal(9, movement.getCardholderBillingAmount());
      ps.setBigDecimal(10, movement.getReconciliationConversionRate());
      ps.setBigDecimal(11, movement.getCardholderBillingConversionRate());
      ps.setTimestamp(12, Timestamp.valueOf(movement.getTransactionLocalDate()));
      ps.setString(13, movement.getApprovalCode());
      ps.setInt(14, movement.getTransactionCurrencyCode());
      ps.setInt(15, movement.getReconciliationCurrencyCode());
      ps.setInt(16, movement.getCardholderBillingCurrencyCode());
      ps.setString(17, movement.getMerchantCode());
      ps.setString(18, movement.getMerchantName());
      ps.setString(19, movement.getMerchantState());
      ps.setString(20, movement.getMerchantCountry());
      ps.setString(21, movement.getTransactionLifeCycleId());
      ps.setBoolean(22, movement.getReconciled());
      ps.setTimestamp(23, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setTimestamp(24, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));

      return ps;
    }, keyHolder);

    return getIpmMovementById((long) keyHolder.getKey());
  }

  public IpmMovement10 getIpmMovementById(Long id) {
    String findMovementByIdSql = String.format("SELECT * FROM %s.ipm_file_data WHERE id = ?", getSchemaAccounting());

    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(findMovementByIdSql, getIpmEJBBean10().getIpmMovementMapper(), id);
    } catch (EmptyResultDataAccessException ex) {
      return null;
    }
  }
}
