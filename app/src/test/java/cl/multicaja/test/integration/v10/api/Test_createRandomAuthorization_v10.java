package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class Test_createRandomAuthorization_v10 extends TestBaseUnitApi {

  private HttpResponse createRandomAuthorization(Long userIdMc) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid_testhelpers/%s/randomAuthorization", userIdMc), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse getAuthorizations(Long userIdMc) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid_testhelpers/%s/authorizations", userIdMc), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }
/*
  @Test
  public void shouldCreateRandomAuthorization() throws  Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    // Preparar una carga inicial de 5000
    BigDecimal topupValue = new BigDecimal(5000);
    topupUserBalance(prepaidUser.getUuid(), topupValue);
    PrepaidCard10 card = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tenener una tarjeta activa", card);

    System.out.println("prepaidUser.getRut(): "+prepaidUser.getRut());

    HttpResponse resp = createRandomAuthorization(prepaidUser.getUserIdMc());
    Assert.assertEquals("status 200", 200, resp.getStatus());
    BigDecimal randomAmountIn = resp.toObject(BigDecimal.class);
    System.out.println("Valor Guardado de autorización para el usuario : " +prepaidUser.getUserIdMc()+" por "+randomAmountIn);

    HttpResponse respRead = getAuthorizations(prepaidUser.getUserIdMc());
    Assert.assertEquals("status 200", 200, resp.getStatus());
    BigDecimal randomAmountOut = respRead.toObject(BigDecimal.class);
    System.out.println("Valor Encontrado de autorización para el usuario : "+prepaidUser.getUserIdMc()+" por "+randomAmountOut);

    Assert.assertEquals("El valor de "+randomAmountIn+" coincide ",randomAmountIn,randomAmountOut);

  }
 */

}
