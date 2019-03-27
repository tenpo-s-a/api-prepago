package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.helpers.tenpo.ApiCall;
import cl.multicaja.prepaid.helpers.tenpo.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class TestUserById {

  @Test
  public void testSuccess() throws TimeoutException, BaseException {
    ApiCall apiCall = new ApiCall();
    apiCall.setApiUrl("https://tenpo.free.beeceptor.com");
    UUID id = UUID.fromString("956c767f-7a8c-4ddb-ada5-f01886ffa451");
    User user = apiCall.getUserById(id);
    Assert.assertNotNull("Objeto no puede ser null",user);
    Assert.assertEquals("Id igual que enviado", id, user.getId());
  }

  @Test
  public void testNotFound() throws TimeoutException, BaseException {
    ApiCall apiCall = new ApiCall();
    apiCall.setApiUrl("https://tenpo.free.beeceptor.com");
    UUID id = UUID.fromString("956c767f-7a8c-4ddb-ada5-f01886ffa451");
    User user = apiCall.getUserById(id);
    Assert.assertNull("Objeto tiene que ser null",user);
  }
}
