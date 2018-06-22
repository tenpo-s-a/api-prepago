package cl.multicaja.test.v10.api;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_EXISTE;
import static cl.multicaja.core.model.Errors.CLIENTE_NO_TIENE_PREPAGO;

/**
 * @autor vutreras
 */
public class Test_getPrepaidUserBalance_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userIdMc
   * @return
   */
  private HttpResponse getPrepaidUserBalance(Long userIdMc) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/balance", userIdMc));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void getPrepaidUserBalance_ok() throws Exception {

    PrepaidUserEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS = 5000;

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    {
      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(BigDecimal.valueOf(0L));
      NewAmountAndCurrency10 pcaMain = CalculationsHelper.calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = CalculationsHelper.calculatePcaSecondary(balance);

      HttpResponse respHttp = getPrepaidUserBalance(user.getId());

      Assert.assertEquals("status 200", 200, respHttp.getStatus());

      PrepaidBalance10 prepaidBalance10 = respHttp.toObject(PrepaidBalance10.class);

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertFalse("No debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    // se hace una carga
    BigDecimal impfac = BigDecimal.valueOf(3000);
    topupUserBalance(user, impfac);

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    Thread.sleep(PrepaidUserEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS + 1000);

    {
      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(BigDecimal.valueOf(2010));
      NewAmountAndCurrency10 pcaMain = CalculationsHelper.calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = CalculationsHelper.calculatePcaSecondary(balance);

      HttpResponse respHttp = getPrepaidUserBalance(user.getId());

      Assert.assertEquals("status 200", 200, respHttp.getStatus());

      PrepaidBalance10 prepaidBalance10 = respHttp.toObject(PrepaidBalance10.class);

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }
  }

  @Test
  public void getPrepaidUserBalance_not_ok() throws Exception {

    User user = registerUser();

    {
      HttpResponse respHttp = getPrepaidUserBalance(null);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }

    {
      try {

        HttpResponse respHttp = getPrepaidUserBalance(user.getId());

        Assert.assertEquals("status 404", 404, respHttp.getStatus());

        NotFoundException nex = respHttp.toObject(NotFoundException.class);

        if (nex != null) {
          throw nex;
        }

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(NotFoundException vex) {
        Assert.assertEquals("debe ser error cliente no tiene prepago", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
      }
    }

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    {
      try {

        HttpResponse respHttp = getPrepaidUserBalance(user.getId() + 1);

        Assert.assertEquals("status 404", 404, respHttp.getStatus());

        NotFoundException nex = respHttp.toObject(NotFoundException.class);

        if (nex != null) {
          throw nex;
        }

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(NotFoundException vex) {
        Assert.assertEquals("debe ser error cliente no tiene prepago", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
      }
    }

  }
}
