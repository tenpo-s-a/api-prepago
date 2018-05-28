package cl.multicaja.test.api.unit;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.SignUp;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class TestBaseUnit extends TestApiBase {

  static {
    System.setProperty("project.artifactId", "api-prepaid");
  }

  protected RutUtils rutUtils = RutUtils.getInstance();

  protected ParametersUtil parametersUtil = ParametersUtil.getInstance();

  private PrepaidTopupDelegate10 prepaidTopupDelegate10;
  private CdtEJBBean10 cdtEJBBean10;
  private UsersEJBBean10 usersEJBBean10;
  private PrepaidEJBBean10 prepaidEJBBean10;
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  protected PrepaidTopupDelegate10 getPrepaidTopupDelegate10() {
    if (prepaidTopupDelegate10 == null) {
      prepaidTopupDelegate10 = new PrepaidTopupDelegate10();
    }
    return prepaidTopupDelegate10;
  }

  protected CdtEJBBean10 getCdtEJBBean10() {
    if (cdtEJBBean10 == null) {
      cdtEJBBean10 = new CdtEJBBean10();
    }
    return cdtEJBBean10;
  }

  protected UsersEJBBean10 getUsersEJBBean10() {
    if (usersEJBBean10 == null) {
      usersEJBBean10 = new UsersEJBBean10();
    }
    return usersEJBBean10;
  }

  protected PrepaidEJBBean10 getPrepaidEJBBean10() {
    if (prepaidEJBBean10 == null) {
      prepaidEJBBean10 = new PrepaidEJBBean10();
      prepaidEJBBean10.setDelegate(getPrepaidTopupDelegate10());
      prepaidEJBBean10.setUsersEJB10(getUsersEJBBean10());
      prepaidEJBBean10.setCdtEJB10(this.getCdtEJBBean10());
    }
    return prepaidEJBBean10;
  }

  protected PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10(){
    if (prepaidMovementEJBBean10 == null) {
      prepaidMovementEJBBean10 = new PrepaidMovementEJBBean10();
    }
    return prepaidMovementEJBBean10;
  }

  /**
   *
   * @return
   * @throws Exception
   */
  public User registerUser() throws Exception {
    Integer rut = getUniqueRutNumber();
    String email = String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(20));
    SignUp singUP = getUsersEJBBean10().signUpUser(null, rut, email);
    return getUsersEJBBean10().getUserById(null, singUP.getUserId());
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
    prepaidCard.setEncryptedPan(RandomStringUtils.randomAlphabetic(50));
    prepaidCard.setExpiration(expiryDate);
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setProcessorUserId(RandomStringUtils.randomAlphabetic(20));
    prepaidCard.setNameOnCard("Tarjeta de: " + RandomStringUtils.randomAlphabetic(5));
    return prepaidCard;
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
    newAmountAndCurrency.setCurrencyCode(CurrencyCodes.CHILE_CLP);

    prepaidTopup.setAmount(newAmountAndCurrency);
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
    Assert.assertNotNull("debe tener pan", prepaidCard.getPan());
    Assert.assertNotNull("debe tener encryptedPan", prepaidCard.getEncryptedPan());
    Assert.assertNotNull("debe tener expiration", prepaidCard.getExpiration());
    Assert.assertNotNull("debe tener status", prepaidCard.getStatus());
    Assert.assertNotNull("debe tener processorUserId", prepaidCard.getProcessorUserId());
    Assert.assertNotNull("debe tener nameOnCard", prepaidCard.getNameOnCard());

    return prepaidCard;
  }
}
