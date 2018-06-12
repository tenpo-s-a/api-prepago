package cl.multicaja.test.v10.api;

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
public class Test_balance extends TestBaseUnitApi {

  @Test
  public void balance_v10() throws Exception {

    PrepaidUserEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS = 5000;

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    {
      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(BigDecimal.valueOf(0L), CodigoMoneda.CHILE_CLP);
      NewAmountAndCurrency10 pcaMain = CalculationsHelper.calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = CalculationsHelper.calculatePcaSecondary(balance);

      HttpResponse resp = apiGET(String.format("/1.0/prepaid/%s/balance", prepaidUser10.getId()));
      Assert.assertEquals("status 200", 200, resp.getStatus());

      PrepaidBalance10 prepaidBalance10 = resp.toObject(PrepaidBalance10.class);

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

      HttpResponse resp = apiGET(String.format("/1.0/prepaid/%s/balance", prepaidUser10.getId()));
      Assert.assertEquals("status 200", 200, resp.getStatus());

      PrepaidBalance10 prepaidBalance10 = resp.toObject(PrepaidBalance10.class);

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }
  }
}
