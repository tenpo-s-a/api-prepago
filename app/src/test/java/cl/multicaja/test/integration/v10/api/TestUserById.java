package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.helpers.tenpo.ApiCall;
import cl.multicaja.prepaid.helpers.tenpo.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class TestUserById {

  private static final String URL = "http://a9edead9fef6711e8af090ab676248e6-1044307436.us-east-1.elb.amazonaws.com:8080/v1/user-management";

  @Test
  public void testSuccess() throws TimeoutException, BaseException {
    ApiCall apiCall = new ApiCall();
    apiCall.setApiUrl(URL);
    UUID id = UUID.fromString("774284d0-b07c-4aab-9bef-727bb4283ee8");
    User user = apiCall.getUserById(id);
    Assert.assertNotNull("Objeto no puede ser null",user);
    Assert.assertEquals("Id igual que enviado", id, user.getId());
  }

  @Test
  public void testNotFound() throws TimeoutException, BaseException {
    ApiCall apiCall = new ApiCall();
    apiCall.setApiUrl(URL);
    UUID id = UUID.fromString("956c767f-7a8c-4ddb-ada5-f01886ffa451");
    User user = apiCall.getUserById(id);
    Assert.assertNull("Objeto tiene que ser null",user);
  }
}
