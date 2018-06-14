package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_getSignupStatus_v10 extends TestBaseUnitApi {

  @Test
  public void getSignup(){
    HttpResponse resp = apiGET("/1.0/prepaid/signup/1");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 201", 201, resp.getStatus());
  }
}
