package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidTransaction10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.Assert;
import org.junit.Test;


import java.math.BigDecimal;
import java.util.List;

public class Test_getTransaction_v10 extends TestBaseUnitApi {

  private HttpResponse getTransactions(Long userIdMc, String fecha_desde, String fecha_hasta) {

    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/transactions?from=%s&to=%s", userIdMc,fecha_desde,fecha_hasta),DEFAULT_HTTP_HEADERS2);
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

  @Test
  public void getTransactionsOk() throws Exception{
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);
      prepaidUser10 = createPrepaidUser10(prepaidUser10);

      AltaClienteDTO altaClienteDTO = registerInTecnocom(user);
      Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);
      prepaidCard10 = createPrepaidCard10(prepaidCard10);
      int amount = numberUtils.random(3000, 10000);
      BigDecimal impfac = BigDecimal.valueOf(amount);
      InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);
      Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

      HttpResponse respHttp =getTransactions(user.getId(),"","");
      Assert.assertEquals("status 200", 200, respHttp.getStatus());
      ObjectMapper mapper = new ObjectMapper();
      mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCaseStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
      List<PrepaidTransaction10> prepaidTransaction10List = mapper.readValue(respHttp.getResp(), new TypeReference<List<PrepaidTransaction10>>(){});
      Assert.assertNotNull("Response Not Null",prepaidTransaction10List);
      Assert.assertEquals("ArrayList.size() = 1",1,prepaidTransaction10List.size());
      Assert.assertEquals("Debe  coincidir el monto ",amount,prepaidTransaction10List.get(0).getAmountPrimary().getValue().intValue());

    }
  }
}
