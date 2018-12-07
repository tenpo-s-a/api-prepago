package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor vutreras
 */
public class Test_getPrepaidUserBalance_v10 extends TestBaseUnitApi {

  @BeforeClass
  public static void  beforeClass() {
    getTecnocomService().setAutomaticError(Boolean.FALSE);
    getTecnocomService().setRetorno(null);
  }

  /**
   *
   * @param userIdMc
   * @param forceRefreshBalance
   * @return
   */
  private HttpResponse getPrepaidUserBalance(Long userIdMc, boolean forceRefreshBalance) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/balance", userIdMc), new HttpHeader("forceRefreshBalance", String.valueOf(forceRefreshBalance)));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void getPrepaidUserBalance_ok() throws Exception {



    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    // se hace una carga
    BigDecimal impfac = BigDecimal.valueOf(3000);
    topupUserBalance(user, impfac);

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    {
      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      HttpResponse respHttp = getPrepaidUserBalance(user.getId(), true);

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
      HttpResponse respHttp = getPrepaidUserBalance(null, false);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }

    //no debe existir el usuario
    {
      try {

        HttpResponse respHttp = getPrepaidUserBalance(user.getId() + 1, false);

        Assert.assertEquals("status 404", 404, respHttp.getStatus());

        NotFoundException nex = respHttp.toObject(NotFoundException.class);

        if (nex != null) {
          throw nex;
        }

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(NotFoundException nex) {
        Assert.assertEquals("debe ser error cliente no tiene prepago", CLIENTE_NO_EXISTE.getValue(), nex.getCode());
      }
    }

    //aun no tiene prepago
    {
      try {

        HttpResponse respHttp = getPrepaidUserBalance(user.getId(), false);

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

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //ahora tiene prepago pero aun no se ha creado tarjeta, debe dar error de tarjeta primera carga pendiente
    try {

      HttpResponse respHttp = getPrepaidUserBalance(user.getId(), false);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());

      ValidationException vex = respHttp.toObject(ValidationException.class);

      if (vex != null) {
        throw vex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_PRIMERA_CARGA_PENDIENTE.getValue(), vex.getCode());
    }

    //ahora se crea la tarjeta para que pase la validacion anterior
    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //dado que no se dio de alta el cliente, al intentar buscar el saldo en tecnocom debe dar error
    try {

      HttpResponse respHttp = getPrepaidUserBalance(user.getId(), false);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());

      ValidationException vex = respHttp.toObject(ValidationException.class);

      if (vex != null) {
        throw vex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", SALDO_NO_DISPONIBLE_$VALUE.getValue(), vex.getCode());
    }
  }
}
