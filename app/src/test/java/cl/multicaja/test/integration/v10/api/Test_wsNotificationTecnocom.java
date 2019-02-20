package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.NotificationTecnocom;
import cl.multicaja.prepaid.model.v10.NotificationTecnocomBody;
import cl.multicaja.prepaid.model.v10.NotificationTecnocomHeader;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.PARAMETRO_NO_CUMPLE_FORMATO_$VALUE;

public class Test_wsNotificationTecnocom extends TestBaseUnitApi {


  private NotificationTecnocom notificationTecnocom;
  private NotificationTecnocomHeader notificationTecnocomHeader;
  private NotificationTecnocomBody notificationTecnocomBody;


  private HttpResponse callNotification(NotificationTecnocom notificationTecnocom, HttpHeader[] headers){
    HttpResponse httpResponse = apiPOST(String.format("/1.0/prepaid/processor/notification"), toJson(notificationTecnocom),headers);
    return httpResponse;
  }


  @Test
  public void testCallNotificationAllParamsWithSuccessProcess() {

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setTipoTx(100);
    notificationTecnocomBody.setIdMensaje(1200);
    notificationTecnocomBody.setMerchantCode("0008902131");
    notificationTecnocomBody.setMerchantName("AMAZON UK");
    notificationTecnocomBody.setCountryIso3266Code(152);
    notificationTecnocomBody.setCountryDescription("Republica de Chile");
    notificationTecnocomBody.setPlaceName("Santiago");
    notificationTecnocomBody.setResolucionTx(100);

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    messageResponse = httpResponse.getResp();
    Assert.assertEquals(messageResponse,202,httpResponse.getStatus());

  }

  @Test
  public void testCallNotificationWithoutBase64ParamOrNullValue() {

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setTipoTx(100);
    notificationTecnocomBody.setIdMensaje(1200);
    notificationTecnocomBody.setMerchantCode("0008902131");
    notificationTecnocomBody.setMerchantName("AMAZON UK");
    notificationTecnocomBody.setCountryIso3266Code(152);
    notificationTecnocomBody.setCountryDescription("Republica de Chile");
    notificationTecnocomBody.setPlaceName("Santiago");
    notificationTecnocomBody.setResolucionTx(100);

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(null);
    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    Map<String, Object> errorObj = httpResponse.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Status 400",400,errorObj.get("status"));
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void testCallNotificationWithBase64NotValidParamValue() {

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setTipoTx(100);
    notificationTecnocomBody.setIdMensaje(1200);
    notificationTecnocomBody.setMerchantCode("0008902131");
    notificationTecnocomBody.setMerchantName("AMAZON UK");
    notificationTecnocomBody.setCountryIso3266Code(152);
    notificationTecnocomBody.setCountryDescription("Republica de Chile");
    notificationTecnocomBody.setPlaceName("Santiago");
    notificationTecnocomBody.setResolucionTx(100);

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data("This is a Test");
    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    Map<String, Object> errorObj = httpResponse.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Status 422",422,errorObj.get("status"));
    Assert.assertEquals("Deberia tener error code = 101007", PARAMETRO_NO_CUMPLE_FORMATO_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void testCallNotificationMoreThanOneParamsOnNull() {

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setTipoTx(100);
    notificationTecnocomBody.setIdMensaje(1200);
    notificationTecnocomBody.setMerchantCode("0008902131");
    notificationTecnocomBody.setMerchantName("AMAZON UK");
    notificationTecnocomBody.setCountryIso3266Code(152);
    notificationTecnocomBody.setCountryDescription("Republica de Chile");
    notificationTecnocomBody.setPlaceName(null);
    notificationTecnocomBody.setResolucionTx(null);

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);
    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    Map<String, Object> errorObj = httpResponse.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Status 400",400,errorObj.get("status"));
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndEmptyBodyData() {

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();

    notificationTecnocomBody = new NotificationTecnocomBody();

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    Map<String, Object> errorObj = httpResponse.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Status 400",400,errorObj.get("status"));
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndCompleteBodyData() {

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue(BigDecimal.valueOf(1000.00));
    notificationTecnocomBody.setTipoTx(100);
    notificationTecnocomBody.setIdMensaje(1200);
    notificationTecnocomBody.setMerchantCode("0008902131");
    notificationTecnocomBody.setMerchantName("AMAZON UK");
    notificationTecnocomBody.setCountryIso3266Code(152);
    notificationTecnocomBody.setCountryDescription("Republica de Chile");
    notificationTecnocomBody.setPlaceName("Santiago");
    notificationTecnocomBody.setResolucionTx(100);

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    Map<String, Object> errorObj = httpResponse.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Status 400",400,errorObj.get("status"));
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void testcallNotificationHeaderDataAndEmptyBodyData() {

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    Map<String, Object> errorObj = httpResponse.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Status 400",400,errorObj.get("status"));
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void testCallNotificationMakeError500(){
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    notificationTecnocomHeader = new NotificationTecnocomHeader();

    notificationTecnocomBody = new NotificationTecnocomBody();

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(null);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);

    HttpResponse httpResponse = callNotification(notificationTecnocom,headers);

    Map<String, Object> errorObj = httpResponse.toMap();

    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Status 500",500,errorObj.get("status"));
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));

  }

}
