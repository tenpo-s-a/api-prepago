package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_getSignup_v10 extends TestBaseUnitApi {

  /**
   *
   * @param signupId
   * @return
   */
  private HttpResponse getSignup(Long signupId) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/signup/%s", signupId));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Ignore
  @Test
  public void getSignup_ok(){
    HttpResponse resp = getSignup(1L);
    Assert.assertEquals("status 201", 201, resp.getStatus());
  }
}
