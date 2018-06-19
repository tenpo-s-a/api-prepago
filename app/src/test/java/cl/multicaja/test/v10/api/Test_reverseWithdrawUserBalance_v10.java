package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.NewPrepaidWithdraw10;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_reverseWithdrawUserBalance_v10 extends TestBaseUnitApi {

  /**
   *
   * @param newPrepaidWithdraw10
   * @return
   */
  private HttpResponse reverseWithdrawUserBalance(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup/reverse", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void reverseWithdrawUserBalance_ok(){
    HttpResponse resp = reverseWithdrawUserBalance(new NewPrepaidWithdraw10());
    Assert.assertEquals("status 201", 201, resp.getStatus());
  }
}
