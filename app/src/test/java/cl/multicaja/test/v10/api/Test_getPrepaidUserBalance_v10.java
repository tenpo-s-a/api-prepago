package cl.multicaja.test.v10.api;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidBalance10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class Test_getPrepaidUserBalance_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userId
   * @return
   */
  private HttpResponse getPrepaidUserBalance(Long userId) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/balance", userId));
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
      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(BigDecimal.valueOf(0L), CodigoMoneda.CHILE_CLP);
      NewAmountAndCurrency10 pcaMain = CalculationsHelper.calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = CalculationsHelper.calculatePcaSecondary(balance);

      HttpResponse respHttp = getPrepaidUserBalance(prepaidUser10.getId());

      Assert.assertEquals("status 200", 200, respHttp.getStatus());

      PrepaidBalance10 prepaidBalance10 = respHttp.toObject(PrepaidBalance10.class);

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertFalse("No debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    Thread.sleep(PrepaidUserEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS + 1000);

    {
      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac, CodigoMoneda.CHILE_CLP);
      NewAmountAndCurrency10 pcaMain = CalculationsHelper.calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = CalculationsHelper.calculatePcaSecondary(balance);

      HttpResponse respHttp = getPrepaidUserBalance(prepaidUser10.getId());

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

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    {
      HttpResponse respHttp = getPrepaidUserBalance(null);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }

    {
      try {

        HttpResponse respHttp = getPrepaidUserBalance(prepaidUser10.getId() + 1);

        Assert.assertEquals("status 404", 404, respHttp.getStatus());

        NotFoundException nex = respHttp.toObject(NotFoundException.class);

        if (nex != null) {
          throw nex;
        }

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(NotFoundException vex) {
        Assert.assertEquals("debe ser error cliente no tiene prepago", Integer.valueOf(102003), vex.getCode());
      }
    }

  }
}
