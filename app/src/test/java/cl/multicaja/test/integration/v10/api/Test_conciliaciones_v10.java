package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class Test_conciliaciones_v10 extends TestBaseUnitApi {

  private HttpResponse topupUserBalance(NewPrepaidTopup10 newPrepaidTopup10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup", toJson(newPrepaidTopup10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse reverseTopupUserBalance(NewPrepaidTopup10 newPrepaidTopup10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup/reverse", toJson(newPrepaidTopup10), headers);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }
  private HttpResponse withdrawUserBalance(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/withdrawal", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }
  private HttpResponse reverseWithdrawUserBalance(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST("/1.0/prepaid/withdrawal/reverse", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private PrepaidTopup10 createTopup() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
    HttpResponse resp = topupUserBalance(prepaidTopup);
    Assert.assertEquals("status 201", 201, resp.getStatus());
    return resp.toObject(PrepaidTopup10.class);
  }

  private void createTopupReverse() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
    prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(getUniqueInteger()));

    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup = createPrepaidMovement10(originalTopup);
    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);
    HttpResponse resp = reverseTopupUserBalance(prepaidTopup);
    Assert.assertEquals("status 201", 201, resp.getStatus());

  }

  private PrepaidWithdraw10 posWithdraw() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(user, BigDecimal.valueOf(RandomUtils.nextDouble(10000,99999)));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);
    Assert.assertEquals("status 201", 201, resp.getStatus());
    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);
    return withdraw;
  }


   private void reverseWithdraw() throws Exception {

      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
      originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
      originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
      originalWithdraw = createPrepaidMovement10(originalWithdraw);

      Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
      Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());
  }
  // CREA 10 CARGAS BUEENAS
  @Test
  public void creaCargas() throws Exception {
    for(int i=0;i<10;i++) {
      PrepaidTopup10 topup =  createTopup();
      Assert.assertNotNull("Debe existir TOPUP",topup);
      Thread.sleep(1000);
    }
  }
  // CREA 10 REVERSAS DE CARGAS
  @Test
  public void creaReversaCargas() throws Exception {
    for(int i=0;i<10;i++) {
      createTopupReverse();
      Thread.sleep(1000);
    }
  }
  @Test
  public void creaRetiros() throws Exception {
    for(int i=0;i<5;i++) {
      PrepaidWithdraw10 withdraw =  posWithdraw();
      Assert.assertNotNull("Debe existir withdraw",withdraw);
      Thread.sleep(1000);
    }
  }
  @Test
  public void creaReversaRetiros() throws Exception {
    for(int i=0;i<5;i++) {
      reverseWithdraw();
      Thread.sleep(1000);
    }
  }
}
