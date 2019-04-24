package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoFactura;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class Test_createRandomPurchase_v10 extends TestBaseUnitApi  {

  private HttpResponse createRandomPurchase(Long userIdMc) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid_testhelpers/%s/randomPurchase", userIdMc), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse getPrepaidUserBalance(Long userIdMc, boolean forceRefreshBalance) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/balance", userIdMc), new HttpHeader("forceRefreshBalance", String.valueOf(forceRefreshBalance)));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse getPrepaidUserTransactions(Long userIdMc, String fecha_desde, String fecha_hasta) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/transactions?from=%s&to=%s", userIdMc, fecha_desde, fecha_hasta), DEFAULT_HTTP_HEADERS2);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  //TODO: Revisar y corregir.
  @Ignore
  @Test
  public void shouldCreateRandomPurchase() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    // Preparar una carga inicial de 5000
    BigDecimal topupValue = new BigDecimal(5000);
    topupUserBalance(prepaidUser.getUuid(), topupValue);
    PrepaidCard10 card = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tenener una tarjeta activa", card);

    // Crear la compra aleatoria
    HttpResponse resp = createRandomPurchase(prepaidUser.getUserIdMc());
    Assert.assertEquals("status 201", 201, resp.getStatus());
    BigDecimal randomAmount = resp.toObject(BigDecimal.class);
    System.out.println("Se hizo una compra de: " + randomAmount);

    // Obtener el nuevo saldo
    HttpResponse respHttp = getPrepaidUserBalance(prepaidUser.getId(), true);
    Assert.assertEquals("status 200", 200, respHttp.getStatus());
    PrepaidBalance10 prepaidBalance10 = respHttp.toObject(PrepaidBalance10.class);

    // Revisar resultado, fee de compras internacionales = 2.5%
    BigDecimal fee = randomAmount.multiply(new BigDecimal(0.025)).setScale(0, BigDecimal.ROUND_HALF_UP);
    BigDecimal cardFee = BigDecimal.valueOf(0);
    topupValue = topupValue.subtract(randomAmount).subtract(fee).subtract(cardFee); // Calcular nuevo valor
    Assert.assertEquals("Debe ser igual", topupValue, prepaidBalance10.getBalance().getValue());
    Assert.assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());


    int internationalPurchaseCounter = 0;

    HttpResponse transRespHttp = getPrepaidUserTransactions(prepaidUser.getUserIdMc(), "", "");
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCaseStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

    PrepaidTransactionExtend10 prepaidTransactionExtend10 = mapper.readValue(transRespHttp.getResp(),
      new TypeReference<PrepaidTransactionExtend10>(){});
    Assert.assertEquals("Transacciones sin errores: ",true,prepaidTransactionExtend10.getSuccess());

    List<PrepaidTransaction10> prepaidTransaction10List = prepaidTransactionExtend10.getData();
    for (PrepaidTransaction10 prepaidTransaction : prepaidTransaction10List) {
      System.out.println("Se encontro transaccion: " + prepaidTransaction.getInvoiceType().getCode());
      if (TipoFactura.COMPRA_INTERNACIONAL.getCode() == prepaidTransaction.getInvoiceType().getCode()) {
        internationalPurchaseCounter++;
      }
    }
    Assert.assertEquals("Debe haber 1 compra internacional", 1, internationalPurchaseCounter);
  }
}
