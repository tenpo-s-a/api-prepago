package cl.multicaja.test.integration.v10.api;

import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.helpers.tenpo.TenpoApiCall;
import cl.multicaja.prepaid.helpers.tenpo.model.TenpoUser;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

public class TestUserById {

  private static final String URL = "http://40.70.68.63:8080/v1/user-management";
  private PrepaidEJBBean10 prepaidEJBBean10 = new PrepaidEJBBean10();

  public PrepaidEJBBean10 getPrepaidEJBBean10() {
    return prepaidEJBBean10;
  }

  @Test
  public void testSuccess() throws Exception {
    TenpoApiCall tenpoApiCall = TenpoApiCall.getInstance();
    tenpoApiCall.setApiUrl(URL);
    UUID id = UUID.fromString("4a70b069-545a-4bc1-af16-a109028aad02");
    TenpoUser tenpoUser = tenpoApiCall.getUserById(id);
    Assert.assertNotNull("Objeto no puede ser null", tenpoUser);
    Assert.assertEquals("Id igual que enviado", id, tenpoUser.getId());
  }

  @Test
  public void testNotFound() throws Exception {
    TenpoApiCall tenpoApiCall = TenpoApiCall.getInstance();
    tenpoApiCall.setApiUrl(URL);
    UUID id = UUID.fromString("956c767f-7a8c-4ddb-ada5-f01886ffa451");
    TenpoUser tenpoUser = tenpoApiCall.getUserById(id);
    Assert.assertNull("Objeto tiene que ser null", tenpoUser);
  }

  //On Demand
  @Ignore
  @Test
  public void validateTenpoUser() throws Exception {
    String uuid = "894d0db8-3da2-4ddd-b3bf-3c64cd2247de";
    UUID id = UUID.fromString(uuid);
    TenpoApiCall tenpoApiCall = TenpoApiCall.getInstance();
    tenpoApiCall.setApiUrl(URL);

    TenpoUser tenpoUser = tenpoApiCall.getUserById(id);
    PrepaidUser10 prepaidUser10 = getPrepaidEJBBean10().validateTempoUser(uuid);

    Assert.assertEquals("Los uuid son iguales",id,tenpoUser.getId());
    Assert.assertEquals("Los uuid son iguales",id,UUID.fromString(prepaidUser10.getUuid()));

  }

}
