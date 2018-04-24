package cl.multicaja.prepago.test.kong;

import cl.multicaja.prepago.test.TestKongBase;
import cl.multicaja.prepago.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180420120200 extends TestKongBase {

  @Test
  public void check_usersfake_1_0() {

    HttpResponse resp = kongGET("/prepaid/ping", "api-prepaid-1.0");

    Assert.assertEquals("status 200", 200, resp.getStatus());
    Assert.assertEquals("servicio 1.0", "PrepaidResource10", resp.toMap().get("service"));
  }
}
