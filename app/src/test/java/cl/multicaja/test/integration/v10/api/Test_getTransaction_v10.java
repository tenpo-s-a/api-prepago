package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidTransaction10;
import cl.multicaja.prepaid.model.v10.PrepaidTransactionExtend10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class Test_getTransaction_v10 extends TestBaseUnitApi {

  private HttpResponse getTransactions(Long userIdMc, String fecha_desde, String fecha_hasta) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/transactions?from=%s&to=%s", userIdMc,fecha_desde,fecha_hasta),DEFAULT_HTTP_HEADERS2);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse createRandomAuthorization(Long userIdMc) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid_testhelpers/%s/randomAuthorization", userIdMc), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void getTransactionsErrorParam() {
    {
      HttpResponse respHttp = getTransactions(null, null,null);
      System.out.println("HTTPSTATUS: "+respHttp.getStatus());
      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }
  }

  //TODO: Esto no se utilizara
  @Ignore
  @Test
  public void getTransactionsOk() throws Exception{
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      int amount = numberUtils.random(3000, 10000);
      BigDecimal impfac = BigDecimal.valueOf(amount);

      InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(account.getAccountNumber(), prepaidCard10, impfac);
      Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

      HttpResponse resp = createRandomAuthorization(prepaidUser.getId());
      Assert.assertEquals("status 200", 200, resp.getStatus());
      BigDecimal randomAmount = resp.toObject(BigDecimal.class);
      System.out.println("Se hizo una autorización de: " + randomAmount);

      HttpResponse respHttp = getTransactions(prepaidUser.getId(),"","");
      Assert.assertEquals("status 200", 200, respHttp.getStatus());

      ObjectMapper mapper = new ObjectMapper();
      mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCaseStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

      PrepaidTransactionExtend10 prepaidTransactionExtend10 = mapper.readValue(respHttp.getResp(),
        new TypeReference<PrepaidTransactionExtend10>(){});

      Assert.assertEquals("Transacciones sin errores: ",true,prepaidTransactionExtend10.getSuccess());

      List<PrepaidTransaction10> prepaidTransaction10List = prepaidTransactionExtend10.getData();
      Assert.assertNotNull("Response Not Null",prepaidTransaction10List);
      Assert.assertEquals("ArrayList.size() = 2",2,prepaidTransaction10List.size());
      Assert.assertEquals("Debe  coincidir el monto ",amount,
        prepaidTransaction10List.get(0).getAmountPrimary().getValue().intValue());

    }
  }


}
