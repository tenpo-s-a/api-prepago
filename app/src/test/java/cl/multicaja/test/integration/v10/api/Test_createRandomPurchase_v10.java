package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.ejb.EJB;
import java.math.BigDecimal;

public class Test_createRandomPurchase_v10 extends TestBaseUnitApi  {

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  private HttpResponse createRandomPurchase(Long mcUserId) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/randomPurchase", mcUserId.toString()), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse getPrepaidUserBalance(Long userIdMc, boolean forceRefreshBalance) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/balance", userIdMc), new HttpHeader("forceRefreshBalance", String.valueOf(forceRefreshBalance)));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn200_createRandomPurchase() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // Preparar una carga inicial de 5000
    BigDecimal topupValue = new BigDecimal(5000);
    topupUserBalance(user, topupValue);
    PrepaidCard10 card = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tenener una tarjeta activa", card);

    // Crear la compra aleatoria
    HttpResponse resp = createRandomPurchase(prepaidUser.getUserIdMc());
    Assert.assertEquals("status 201", 201, resp.getStatus());
    BigDecimal randomAmount = resp.toObject(BigDecimal.class);

    // Obtener el nuevo saldo
    HttpResponse respHttp = getPrepaidUserBalance(user.getId(), true);
    Assert.assertEquals("status 200", 200, respHttp.getStatus());
    PrepaidBalance10 prepaidBalance10 = respHttp.toObject(PrepaidBalance10.class);

    // Revisar resultado
    BigDecimal fee = randomAmount.multiply(new BigDecimal(0.025));
    Assert.assertEquals("Debe ser igual", topupValue.subtract(randomAmount).subtract(fee), prepaidBalance10.getBalance().getValue());
    Assert.assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
  }

}
