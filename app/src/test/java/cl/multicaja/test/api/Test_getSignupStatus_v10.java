package cl.multicaja.test.api;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_getSignupStatus_v10 extends TestApiBase {

  @Test
  public void getSignupStatus(){
    HttpResponse resp = apiGET("/1.0/prepaid/signup/1");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 200", 200, resp.getStatus());
  }
}