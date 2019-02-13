package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.NotificationTecnocom;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Test_sendNotificationTecnocom extends TestBaseUnitApi {
  

  private HttpResponse callNotification(NotificationTecnocom notificationTecnocom, HttpHeader[] headers) throws Exception{
    String jsontToSend = null;
    jsontToSend = new ObjectMapper().writeValueAsString(notificationTecnocom);
    //jsontToSend = toJson(notificationTecnocom);
    System.out.println("callNotification Params: "+toJson(notificationTecnocom));
    HttpResponse httpResponse = apiPOST(String.format("/1.0/prepaid_testhelpers/processor/notification"), jsontToSend,headers);
    System.out.println("RESP HTTP: " + httpResponse);
    return httpResponse;
  }

  @Test
  public void testCallNotificationAllParamsWithSuccessProcess() throws Exception {

    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    NotificationTecnocom notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setSdCurrencyCode(new Long(152));
    notificationTecnocom.setSdValue("1000.00");
    notificationTecnocom.setIlCurrencyCode(new Long(152));
    notificationTecnocom.setIlValue("1000.00");
    notificationTecnocom.setIdCurrencyCode(new Long(152));
    notificationTecnocom.setIdValue("1000.00");
    notificationTecnocom.setTipoTx(new Long(100));
    notificationTecnocom.setIdMensaje(new Long(1200));
    notificationTecnocom.setMerchantCode("0008902131");
    notificationTecnocom.setMerchantName("AMAZON UK");
    notificationTecnocom.setCountryIso3266Code(new Long(152));
    notificationTecnocom.setCountryDescription("Republica de chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));
    notificationTecnocom.setBase64Data(base64String);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    messageResponse = httpResponse.getResp();
    Assert.assertEquals(messageResponse,202,httpResponse.getStatus());

  }

  @Test
  public void testCallNotificationWithoutBase64ParamOrNullValue() throws Exception {
    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    NotificationTecnocom notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setSdCurrencyCode(new Long(152));
    notificationTecnocom.setSdValue("1000.00");
    notificationTecnocom.setIlCurrencyCode(new Long(152));
    notificationTecnocom.setIlValue("1000.00");
    notificationTecnocom.setIdCurrencyCode(new Long(152));
    notificationTecnocom.setIdValue("1000.00");
    notificationTecnocom.setTipoTx(new Long(100));
    notificationTecnocom.setIdMensaje(new Long(1200));
    notificationTecnocom.setMerchantCode("0008902131");
    notificationTecnocom.setMerchantName("AMAZON UK");
    notificationTecnocom.setCountryIso3266Code(new Long(152));
    notificationTecnocom.setCountryDescription("Republica de chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    messageResponse = "Error:"+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testCallNotificationWithBase64NotValidParamValue() throws Exception {
    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse = null;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    NotificationTecnocom notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setSdCurrencyCode(new Long(152));
    notificationTecnocom.setSdValue("1000.00");
    notificationTecnocom.setIlCurrencyCode(new Long(152));
    notificationTecnocom.setIlValue("1000.00");
    notificationTecnocom.setIdCurrencyCode(new Long(152));
    notificationTecnocom.setIdValue("1000.00");
    notificationTecnocom.setTipoTx(new Long(100));
    notificationTecnocom.setIdMensaje(new Long(1200));
    notificationTecnocom.setMerchantCode("0008902131");
    notificationTecnocom.setMerchantName("AMAZON UK");
    notificationTecnocom.setCountryIso3266Code(new Long(152));
    notificationTecnocom.setCountryDescription("Republica de chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));
    notificationTecnocom.setBase64Data("This is a Test");

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    messageResponse = "Error: "+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,422,httpResponse.getStatus());
  }


  @Test
  public void testCallNotificationMoreThanOneParamsOnNull() throws Exception {
    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse = null;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    NotificationTecnocom notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setSdCurrencyCode(new Long(152));
    notificationTecnocom.setSdValue("1000.00");
    notificationTecnocom.setIlCurrencyCode(new Long(152));
    notificationTecnocom.setIlValue("1000.00");
    notificationTecnocom.setIdCurrencyCode(new Long(152));
    notificationTecnocom.setIdValue("1000.00");
    notificationTecnocom.setTipoTx(new Long(100));
    notificationTecnocom.setIdMensaje(new Long(1200));
    notificationTecnocom.setMerchantCode("0008902131");
    notificationTecnocom.setMerchantName("AMAZON UK");
    notificationTecnocom.setCountryIso3266Code(new Long(152));
    notificationTecnocom.setCountryDescription("Republica de chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(null);
    notificationTecnocom.setBase64Data(null);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    messageResponse = httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndEmptyBodyData() throws Exception {

    String messageResponse;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    NotificationTecnocom notificationTecnocom = new NotificationTecnocom();

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);
    messageResponse = "Error: "+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndCompleteBodyData() throws Exception {

    String messageResponse;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    NotificationTecnocom notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setSdCurrencyCode(new Long(152));
    notificationTecnocom.setSdValue("1000.00");
    notificationTecnocom.setIlCurrencyCode(new Long(152));
    notificationTecnocom.setIlValue("1000.00");
    notificationTecnocom.setIdCurrencyCode(new Long(152));
    notificationTecnocom.setIdValue("1000.00");
    notificationTecnocom.setTipoTx(new Long(100));
    notificationTecnocom.setIdMensaje(new Long(1200));
    notificationTecnocom.setMerchantCode("0008902131");
    notificationTecnocom.setMerchantName("AMAZON UK");
    notificationTecnocom.setCountryIso3266Code(new Long(152));
    notificationTecnocom.setCountryDescription("Republica de chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));
    notificationTecnocom.setBase64Data(base64String);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);
    messageResponse = "Error: "+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testcallNotificationHeaderDataAndEmptyBodyData() throws Exception {
    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    NotificationTecnocom notificationTecnocom = new NotificationTecnocom();


    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);
    messageResponse = "Error: "+httpResponse.getResp();
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

}
