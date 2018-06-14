package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_getPrepaidCard_v10 extends TestBaseUnitApi {

  @Test
  public void getPrepaidCard_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = apiGET(String.format("/1.0/prepaid/%s/card", prepaidUser10.getId()));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidCard10 card1 = resp.toObject(PrepaidCard10.class);

    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getId(), card1.getId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getProcessorUserId(), card1.getProcessorUserId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getPan(), card1.getPan());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getExpiration(), card1.getExpiration());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getStatus(), card1.getStatus());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getNameOnCard(), card1.getNameOnCard());
  }

  @Test
  public void getPrepaidCard_not_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = apiGET(String.format("/1.0/prepaid/%s/card", prepaidUser10.getId() + 1));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 404", 404, resp.getStatus());
  }
}
