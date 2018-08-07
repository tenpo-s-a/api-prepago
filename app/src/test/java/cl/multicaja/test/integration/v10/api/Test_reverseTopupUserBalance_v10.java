package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.NewPrepaidTopup10;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_reverseTopupUserBalance_v10 extends TestBaseUnitApi {

  /**
   *
   * @param newPrepaidTopup10
   * @return
   */
  private HttpResponse reverseTopupUserBalance(NewPrepaidTopup10 newPrepaidTopup10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup/reverse", toJson(newPrepaidTopup10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void reverseTopupUserBalance_ok(){
    HttpResponse resp = reverseTopupUserBalance(new NewPrepaidTopup10());
    Assert.assertEquals("status 201", 201, resp.getStatus());
  }
}
