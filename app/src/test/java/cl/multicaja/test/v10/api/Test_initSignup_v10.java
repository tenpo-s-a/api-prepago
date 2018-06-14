package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_initSignup_v10 extends TestBaseUnitApi {

  @Test
  public void signup(){
    HttpResponse resp = apiPOST("/1.0/prepaid/signup", "{}");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 201", 201, resp.getStatus());
  }
}
