package cl.multicaja.test.api.unit;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.*;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @autor vutreras
 */
public class TestBaseUnit extends TestApiBase {

  static {
    System.setProperty("project.artifactId", "api-prepaid");
  }

  public static RutUtils rutUtils = RutUtils.getInstance();

  public static ParametersUtil parametersUtil = ParametersUtil.getInstance();

  public static EncryptUtil encryptUtil = EncryptUtil.getInstance();

  private static PrepaidTopupDelegate10 prepaidTopupDelegate10;
  private static CdtEJBBean10 cdtEJBBean10;
  private static UsersEJBBean10 usersEJBBean10;
  private static PrepaidEJBBean10 prepaidEJBBean10;
  private static PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

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
  public static PrepaidEJBBean10 getPrepaidEJBBean10() {
    if (prepaidEJBBean10 == null) {
      prepaidEJBBean10 = new PrepaidEJBBean10();
      prepaidEJBBean10.setDelegate(getPrepaidTopupDelegate10());
      prepaidEJBBean10.setUsersEJB10(getUsersEJBBean10());
      prepaidEJBBean10.setCdtEJB10(getCdtEJBBean10());
      prepaidEJBBean10.setPrepaidMovementEJB10(getPrepaidMovementEJBBean10());
    }
    return prepaidEJBBean10;
  }

  /**
   * pre-registra a un usuario (solo rut e email)
   * @return
   * @throws Exception
   */
  public User preRegisterUser() throws Exception {
    Integer rut = getUniqueRutNumber();
    String email = String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(20));
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
  public PrepaidUser10 buildPrepaidUser(User user) {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setIdUserMc(user != null ? user.getId() : null);
    prepaidUser.setRut(user != null ? user.getRut().getValue() : null);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    return prepaidUser;
  }

  /**
   *
   * @return
   */
  public PrepaidUser10 buildPrepaidUser() {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setIdUserMc(getUniqueLong());
    prepaidUser.setRut(getUniqueRutNumber());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    return prepaidUser;
  }

  /**
   *
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCard(PrepaidUser10 prepaidUser) throws Exception {
    int expiryYear = numberUtils.random(1000, 9999);
    int expiryMonth = numberUtils.random(1, 99);
    int expiryDate = numberUtils.toInt(expiryYear + "" + StringUtils.leftPad(String.valueOf(expiryMonth), 2, "0"));
    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser != null ? prepaidUser.getId() : null);
    prepaidCard.setPan(RandomStringUtils.randomNumeric(16));
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));
    prepaidCard.setExpiration(expiryDate);
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setProcessorUserId(RandomStringUtils.randomNumeric(20));
    prepaidCard.setNameOnCard("Tarjeta de: " + RandomStringUtils.randomAlphabetic(5));
    return prepaidCard;
  }


  /**
   * CREA TARJETA ESTADO PENDIENTE
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCardPending(PrepaidUser10 prepaidUser) throws Exception {

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser != null ? prepaidUser.getId() : null);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard.setProcessorUserId(RandomStringUtils.randomNumeric(20));

    return prepaidCard;
  }
  /**
   *CREA TARJETA ESTADO PENDIENTE
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCardPending() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUser();
    prepaidUser = getPrepaidEJBBean10().createPrepaidUser(null, prepaidUser);
    return buildPrepaidCardPending(prepaidUser);
  }

  /**
   *
   * @return
   * @throws Exception
   */
  public PrepaidCard10 buildPrepaidCard() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUser();
    prepaidUser = getPrepaidEJBBean10().createPrepaidUser(null, prepaidUser);
    return buildPrepaidCard(prepaidUser);
  }

  /**
   *
   * @param user
   * @return
   */
  public PrepaidTopup10 buildPrepaidTopup(User user) {
    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();
    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setRut(user != null ? user.getRut().getValue() : null);
    prepaidTopup.setId(getUniqueLong());
    prepaidTopup.setUserId(user != null ? user.getId() : null);
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(numberUtils.random(1000, 10000)));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidTopup.setAmount(newAmountAndCurrency);

    prepaidTopup.setMerchantCategory(1);
    prepaidTopup.setMerchantName(RandomStringUtils.randomAlphabetic(6));

    return prepaidTopup;
  }

  /**
   *
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  public PrepaidUser10 createPrepaidUser(PrepaidUser10 prepaidUser) throws Exception {

    prepaidUser = getPrepaidEJBBean10().createPrepaidUser(null, prepaidUser);

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
  public PrepaidCard10 createPrepaidCard(PrepaidCard10 prepaidCard) throws Exception {

    prepaidCard = getPrepaidEJBBean10().createPrepaidCard(null, prepaidCard);

    Assert.assertNotNull("debe retornar un usuario", prepaidCard);
    Assert.assertEquals("debe tener id", true, prepaidCard.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, prepaidCard.getIdUser() > 0);
    Assert.assertNotNull("debe tener status", prepaidCard.getStatus());

    return prepaidCard;
  }

  /**
   *
   * @param prepaidUser
   * @return
   */
  protected PrepaidMovement10 buildPrepaidMovement(PrepaidUser10 prepaidUser) {
    return buildPrepaidMovement(prepaidUser, null);
  }

  /**
   *
   * @param prepaidUser
   * @return
   */
  protected PrepaidMovement10 buildPrepaidMovement(PrepaidUser10 prepaidUser, PrepaidTopup10 prepaidTopup) {

    String codent = ConfigUtils.getInstance().getProperty("tecnocom.codEntity");

    TipoFactura tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;

    if (prepaidTopup != null) {
      if (TopupType.WEB.equals(prepaidTopup.getType())) {
        tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
      } else {
        tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
      }
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta("1234");
    prepaidMovement.setCuenta(getUniqueInteger().toString());
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac("");
    prepaidMovement.setPan(RandomStringUtils.randomNumeric(16));
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(BigDecimal.valueOf(1000));
    prepaidMovement.setCmbapli(0);
    prepaidMovement.setNumaut("");
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom(getUniqueInteger().toString());
    prepaidMovement.setCodact(numberUtils.random(1111,9999));
    prepaidMovement.setImpliq(getUniqueLong());
    prepaidMovement.setClamonliq(0);
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setNompob(RandomStringUtils.randomAlphabetic(6));
    prepaidMovement.setNumextcta(0);
    prepaidMovement.setNummovext(0);
    prepaidMovement.setClamone(CodigoMoneda.CHILE_CLP.getValue());
    prepaidMovement.setTipolin("1234");
    prepaidMovement.setLinref(1);
    prepaidMovement.setNumbencta(1);
    prepaidMovement.setNumplastico(numberUtils.toLong(RandomStringUtils.randomNumeric(12)));
    return prepaidMovement;
  }

  /**
   *
   * @param prepaidMovement10
   * @return
   * @throws Exception
   */
  public PrepaidMovement10 createPrepaidMovement(PrepaidMovement10 prepaidMovement10) throws Exception {

    prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);

    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId() > 0);

    return prepaidMovement10;
  }
}
