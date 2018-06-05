package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_initSignup_v10 extends TestBaseUnitApi {

  @Test
  public void initSignup(){
    HttpResponse resp = apiPOST("/1.0/prepaid/signup", "{}");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 200", 200, resp.getStatus());
  }
}
