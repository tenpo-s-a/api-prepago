package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.NewPrepaidUserSignup10;
import cl.multicaja.prepaid.model.v10.PrepaidUserSignup10;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_initSignup_v10 extends TestBaseUnitApi {

  /**
   *
   * @param newPrepaidUserSignup10
   * @return
   */
  private HttpResponse initSignup(NewPrepaidUserSignup10 newPrepaidUserSignup10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/signup", toJson(newPrepaidUserSignup10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void initSignup_ok(){
    NewPrepaidUserSignup10 signup = new PrepaidUserSignup10();
    signup.setEmail(getUniqueEmail());
    signup.setRut(getUniqueRutNumber());

    HttpResponse resp = initSignup(signup);
    System.out.println("RESP:::" + resp.toMap());
    Assert.assertEquals("status 201", 201, resp.getStatus());
  }
}
