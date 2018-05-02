package cl.multicaja.test.api;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_ping_v10 extends TestApiBase {

  @Test
  public void ping() {
    HttpResponse resp = apiGET("/1.0/ping");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 200", 200, resp.getStatus());
    Assert.assertEquals("service", "PrepaidResource10", resp.toMap().get("service"));
  }
}
