package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_getUserTopups_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userIdMc
   * @return
   */
  private HttpResponse getUserTopups(Long userIdMc) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/topup", userIdMc));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void getUserTopups_ok(){
    HttpResponse resp = getUserTopups(1L);
    Assert.assertEquals("status 200", 200, resp.getStatus());
  }
}
