package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_TIENE_PREPAGO;
import static cl.multicaja.core.model.Errors.SALDO_NO_DISPONIBLE_$VALUE;

public class Test_getAccountBalance extends TestBaseUnitApi {

  @Before
  public void  before1() {
    getTecnocomService().setAutomaticError(Boolean.FALSE);
    getTecnocomService().setRetorno(null);
  }

  private HttpResponse getPrepaidUserBalance(String userUuid, String accountUuid, boolean forceRefreshBalance) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/account/%s/balance", userUuid, accountUuid), new HttpHeader("forceRefreshBalance", String.valueOf(forceRefreshBalance)));
    return respHttp;
  }

  @Test
  public void getBalance_ok() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    // se hace una carga
    BigDecimal impfac = BigDecimal.valueOf(3000);
    topupUserBalance(prepaidUser10.getUuid(), impfac);

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull(account);

    {
      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      HttpResponse respHttp = getPrepaidUserBalance(prepaidUser10.getUuid(), account.getUuid(), true);

      Assert.assertEquals("status 200", 200, respHttp.getStatus());

      PrepaidBalance10 prepaidBalance10 = respHttp.toObject(PrepaidBalance10.class);

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }
  }

  @Test
  public void getBalance_not_ok_userNull() throws Exception {

    try {

      HttpResponse respHttp = getPrepaidUserBalance(UUID.randomUUID().toString(),UUID.randomUUID().toString(), false);
      Assert.assertEquals("status 404", 404, respHttp.getStatus());
      NotFoundException nex = respHttp.toObject(NotFoundException.class);

      if (nex != null) {
        throw nex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(NotFoundException nex) {
      Assert.assertEquals("debe ser error cliente no tiene prepago", CLIENTE_NO_TIENE_PREPAGO.getValue(), nex.getCode());
    }
  }

  @Test
  public void getBalance_not_ok_accountNull() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    try {

      HttpResponse respHttp = getPrepaidUserBalance(prepaidUser10.getUuid(),UUID.randomUUID().toString(), false);
      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      ValidationException nex = respHttp.toObject(ValidationException.class);

      if (nex != null) {
        throw nex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException nex) {
      Assert.assertEquals("debe ser error cliente no tiene prepago", SALDO_NO_DISPONIBLE_$VALUE.getValue(), nex.getCode());
    }
  }

  @Test
  public void getBalance_not_ok_accountMismatch() throws Exception {

    PrepaidUser10 prepaidUser1 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser1 = createPrepaidUserV2(prepaidUser1);

    PrepaidUser10 prepaidUser2 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser2 = createPrepaidUserV2(prepaidUser2);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser1.getId(), getRandomNumericString(15));

    Assert.assertNotNull(account);

    try {

      HttpResponse respHttp = getPrepaidUserBalance(prepaidUser2.getUuid(), account.getUuid(), false);
      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      ValidationException nex = respHttp.toObject(ValidationException.class);

      if (nex != null) {
        throw nex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException nex) {
      Assert.assertEquals("debe ser error cliente no tiene prepago", SALDO_NO_DISPONIBLE_$VALUE.getValue(), nex.getCode());
    }
  }
}
