package cl.multicaja.test.api;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor abarazarte
 */
public class Test_sendCardEmail_v10 extends TestApiBase {

  @Test
  public void sendCardEmail(){
    HttpResponse resp = apiPOST("/1.0/prepaid/1/mail", "{}");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 200", 200, resp.getStatus());
  }
}
