package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

public class Test_sendNotificationTecnocom extends TestBaseUnitApi {


  private HttpResponse callNotification() {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid_testhelpers/processor/notification"), null);
    System.out.println("RESP HTTP: " + respHttp);
    return respHttp;
  }


  @Test
  public void testCallNotification(){
    HttpResponse httpResponse = callNotification();
    Assert.assertEquals("Notificación exitosa",202, httpResponse.getStatus());
  }

  @Test
  public void testBadRequest402(){
    HttpResponse httpResponse = callNotification();
    Assert.assertEquals("Notificatión con Bad Request",402,httpResponse.getStatus());
  }





}
