package cl.multicaja.test.v10.unit;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;
import cl.multicaja.users.model.v10.*;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

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
  private static UsersEJBBean10 usersEJBBean10;
  private static PrepaidUserEJBBean10 prepaidUserEJBBean10;
  private static PrepaidCardEJBBean10 prepaidCardEJBBean10;
  private static PrepaidEJBBean10 prepaidEJBBean10;
  private static PrepaidMovementEJBBean10 prepaidMovementEJBBean10;
  private static MailEJBBean10 mailEJBBean10;

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

  /**
   *
   * @return
   */
  public static String getSchema() {
    return getPrepaidCardEJBBean10().getSchema();
  }

  /**
   *
   * @return
   */
  public static DBUtils getDbUtils() {
    return getPrepaidCardEJBBean10().getDbUtils();
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
  public static UsersEJBBean10 getUsersEJBBean10() {
    if (usersEJBBean10 == null) {
      usersEJBBean10 = new UsersEJBBean10();
    }
    return usersEJBBean10;
  }

  /**
   *
   * @return
   */
  public static PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10(){
    if (prepaidMovementEJBBean10 == null) {
      prepaidMovementEJBBean10 = new PrepaidMovementEJBBean10();
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
      prepaidUserEJBBean10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
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

  /**
   *
   * @return
   */
  public static MailEJBBean10 getMailEJBBean10() {
    if (mailEJBBean10 == null) {
      mailEJBBean10 = new MailEJBBean10();
    }
    return mailEJBBean10;
  }

  /**
   *
   * @return
   */
  public static PrepaidEJBBean10 getPrepaidEJBBean10() {
    if (prepaidEJBBean10 == null) {
      prepaidEJBBean10 = new PrepaidEJBBean10();
      prepaidEJBBean10.setDelegate(getPrepaidTopupDelegate10());
      prepaidEJBBean10.setUsersEJB10(getUsersEJBBean10());
      prepaidEJBBean10.setCdtEJB10(getCdtEJBBean10());
      prepaidEJBBean10.setPrepaidMovementEJB10(getPrepaidMovementEJBBean10());
      prepaidEJBBean10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      prepaidEJBBean10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
    }
    return prepaidEJBBean10;
  }

  /**
   *
   * @return
   */
  public static TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }

  /**
   * pre-registra a un usuario (solo rut e email)
   * @return
   * @throws Exception
   */
  public User preRegisterUser() throws Exception {
    Integer rut = getUniqueRutNumber();
    String email = getUniqueEmail();
    SignUp singUP = getUsersEJBBean10().signUpUser(null, rut, email);
    return getUsersEJBBean10().getUserById(null, singUP.getUserId());
  }

  /**
   *
   * @param u
   * @return
   * @throws Exception
   */
  public User updateUser(User u) throws Exception {
    return getUsersEJBBean10().updateUser(u, u.getRut(), u.getEmail(), u.getCellphone(), u.getNameStatus(), u.getGlobalStatus(), u.getBirthday(), u.getPassword(), u.getCompanyData());
  }

  /**
   * pre-registra un usuario y además lo deja habilitado completamente
   * @return
   * @throws Exception
   */
  public User registerUser() throws Exception {
    User user = preRegisterUser();
    user = getUsersEJBBean10().fillUser(user);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.getEmail().setStatus(EmailStatus.VERIFIED);
    user.setNameStatus(NameStatus.VERIFIED);
    user.setPassword(String.valueOf(numberUtils.random(1111,9999)));
    user = updateUser(user);
    return user;
  }

  /**
   *
   * @return
   */
  public PrepaidUser10 buildPrepaidUser10(User user) {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setIdUserMc(user != null ? user.getId() : null);
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
    prepaidUser.setIdUserMc(getUniqueLong());
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
    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser != null ? prepaidUser.getId() : null);
    prepaidCard.setPan(getRandomNumericString(16));
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));
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
   * CREA TARJETA ESTADO PENDIENTE
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
   * CREA TARJETA ESTADO PENDIENTE
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
  public PrepaidCard10 buildPrepaidCard10FromTecnocom(User user, PrepaidUser10 prepaidUser) {

    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);

    DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(altaClienteDTO.getContrato());

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser.getId());
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard.setPan(datosTarjetaDTO.getPan());
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
    newAmountAndCurrency.setValue(new BigDecimal(3000));
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
  public NewPrepaidTopup10 buildNewPrepaidTopup10(User user) {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setRut(user != null ? user.getRut().getValue() : null);
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(3000));
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
  public NewPrepaidWithdraw10 buildNewPrepaidWithdraw10(User user) {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(user != null ? user.getRut().getValue() : null);
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(5000));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(newAmountAndCurrency);

    prepaidWithdraw.setMerchantCategory(1);
    prepaidWithdraw.setMerchantName(getRandomString(6));

    return prepaidWithdraw;
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
    newAmountAndCurrency.setValue(new BigDecimal(3000));
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
    if(!cdtTransaction.getNumError().equals("0")){
      long lNumError = numberUtils.toLong(cdtTransaction.getNumError(),-1L);
      if(lNumError != -1 && lNumError > 10000) {
        throw new ValidationException(107000).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(101006).setData(new KeyValue("value", cdtTransaction.getMsjError()));
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
    Assert.assertEquals("debe tener idUserMc", true, prepaidUser.getIdUserMc() > 0);
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

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param prepaidCard
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup, PrepaidCard10 prepaidCard) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, null);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param cdtTransaction
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup, CdtTransaction10 cdtTransaction) {
    return buildPrepaidMovement10(prepaidUser, prepaidTopup, null, cdtTransaction);
  }

  /**
   *
   * @param prepaidUser
   * @param prepaidTopup
   * @param prepaidCard
   * @param cdtTransaction
   * @return
   */
  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction) {

    String codent = null;
    try {
      codent = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;

    if (prepaidTopup != null) {
      if (TransactionOriginType.WEB.equals(prepaidTopup.getTransactionOriginType())) {
        tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
      } else {
        tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
      }
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(cdtTransaction != null ? cdtTransaction.getTransactionReference() : getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(cdtTransaction != null ? cdtTransaction.getExternalTransactionId() : getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
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
    prepaidMovement.setImpfac(prepaidTopup.getAmount().getValue());
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup.getMerchantCode());
    prepaidMovement.setCodact(prepaidTopup.getMerchantCategory());
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
      throw new ValidationException(101004).setData(new KeyValue("value", "user"));
    }

    if (user.getName() == null) {
      throw new ValidationException(101004).setData(new KeyValue("value", "user.name"));
    }

    if (user.getLastname_1() == null) {
      throw new ValidationException(101004).setData(new KeyValue("value", "user.lastname_1"));
    }

    if (user.getLastname_2() == null) {
      throw new ValidationException(101004).setData(new KeyValue("value", "user.lastname_2"));
    }

    if (user.getRut() == null) {
      throw new ValidationException(101004).setData(new KeyValue("value", "user.rut"));
    }

    if (user.getRut().getValue() == null) {
      throw new ValidationException(101004).setData(new KeyValue("value", "user.rut.value"));
    }

    return getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);
  }

  /**
   *
   * @param prepaidCard10
   * @return
   */
  public InclusionMovimientosDTO topupInTecnocom(PrepaidCard10 prepaidCard10, BigDecimal impfac) throws BaseException {

    if (prepaidCard10 == null) {
      throw new ValidationException(101004).setData(new KeyValue("value", "prepaidCard10"));
    }

    if (impfac == null) {
      throw new ValidationException(101004).setData(new KeyValue("value", "amount"));
    }

    if (StringUtils.isBlank(prepaidCard10.getProcessorUserId())) {
      throw new ValidationException(101004).setData(new KeyValue("value", "prepaidCard10.processorUserId"));
    }

    if (StringUtils.isBlank(prepaidCard10.getPan())) {
      throw new ValidationException(101004).setData(new KeyValue("value", "prepaidCard10.pan"));
    }

    String contrato = prepaidCard10.getProcessorUserId();
    String pan = prepaidCard10.getPan();
    CodigoMoneda clamon = CodigoMoneda.CHILE_CLP;
    IndicadorNormalCorrector indnorcor = IndicadorNormalCorrector.NORMAL;
    TipoFactura tipofac = TipoFactura.CARGA_TRANSFERENCIA;
    String codcom = "1";
    Integer codact = 1;
    CodigoPais codpais = CodigoPais.CHILE;
    String nomcomred = "prueba";
    String numreffac = getUniqueLong().toString();
    String numaut = numreffac;

    //solamente los 6 primeros digitos de numreffac
    if (numaut.length() > 6) {
      numaut = numaut.substring(numaut.length()-6);
    }

    System.out.println("Monto a cargar: " + impfac);

    InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
      numreffac, impfac, numaut, codcom,
      nomcomred, codact, codpais);

    return inclusionMovimientosDTO;
  }
}
