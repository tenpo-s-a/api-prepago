package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_reverseTopup_v10 extends TestBaseUnitApi {

  @Test
  public void reverseTopupUserBalance(){
    HttpResponse resp = apiPOST("/1.0/prepaid/topup/reverse", "{}");
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 200", 200, resp.getStatus());
  }
}
