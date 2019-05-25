package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

//TODO: Revisar si se seguira utilizando
@Ignore
public class Test_conciliaciones_v10 extends TestBaseUnitApi {


  private HttpResponse topupUserBalance(PrepaidUser10 user10,NewPrepaidTopup10 newPrepaidTopup10) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/cash_in",user10.getUuid()), toJson(newPrepaidTopup10));
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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    HttpResponse resp = topupUserBalance(prepaidUser,prepaidTopup);
    Assert.assertEquals("status 201", 201, resp.getStatus());
    return resp.toObject(PrepaidTopup10.class);
  }

  private void createTopupReverse() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
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

  //TODO: Corregir y descomentar
  /*private PrepaidWithdraw10 posWithdraw() throws Exception {

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
    prepaidWithdraw.setMerchantCode(getRandomNumericString(15));
    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);
    Assert.assertEquals("status 201", 201, resp.getStatus());
    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);
    return withdraw;
  }*/


   private void reverseWithdraw() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(getRandomNumericString(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

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

  //Ignorar hasta corregir
  @Ignore
  // CREA 10 REVERSAS DE CARGAS
  @Test
  public void creaReversaCargas() throws Exception {
    for(int i=0;i<10;i++) {
      createTopupReverse();
      Thread.sleep(1000);
    }
  }

  //Ignorar hasta corregir
  @Ignore
  @Test
  public void creaRetiros() throws Exception {
    for(int i=0;i<5;i++) {
      //PrepaidWithdraw10 withdraw =  posWithdraw();
      //Assert.assertNotNull("Debe existir withdraw",withdraw);
      Thread.sleep(1000);
    }
  }

  //Ignorar hasta corregir
  @Ignore
  @Test
  public void creaReversaRetiros() throws Exception {
    for(int i=0;i<5;i++) {
      reverseWithdraw();
      Thread.sleep(1000);
    }
  }
}
