package cl.multicaja.test.api;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_getPrepaidCard_v10 extends TestApiBase {

  @Test
  public void getPrepaidCard(){
    HttpResponse resp = apiGET("/1.0/prepaid/1/card");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 200", 200, resp.getStatus());
  }
}