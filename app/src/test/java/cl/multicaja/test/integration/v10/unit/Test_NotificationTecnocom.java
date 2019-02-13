package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.NotificationTecnocom;
import cl.multicaja.prepaid.model.v10.NotificationTecnocomBody;
import cl.multicaja.prepaid.model.v10.NotificationTecnocomHeader;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.PARAMETRO_NO_CUMPLE_FORMATO_$VALUE;

public class Test_NotificationTecnocom extends TestBaseUnit {

  private NotificationTecnocom notificationTecnocom;
  private NotificationTecnocomHeader notificationTecnocomHeader;
  private NotificationTecnocomBody notificationTecnocomBody;


  @Test
  public void testCallNotificationAllParamsWithSuccessProcess() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue("1000.00");
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue("1000.00");
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue("1000.00");
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

    NotificationTecnocom notificationTecnocomResponse = getPrepaidEJBBean10().setNotificationCallback(null, notificationTecnocom);

    System.out.println(notificationTecnocomResponse);
    //messageResponse = notificationTecnocomResponse.
    //System.out.println(messageResponse);
    //Assert.assertEquals(messageResponse,"202",notifResult.getErrorCode());

  }


  @Test
  public void testCallNotificationWithoutBase64ParamOrNullValue() throws Exception{

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue("1000.00");
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue("1000.00");
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue("1000.00");
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

    NotificationTecnocom notifResponse = getPrepaidEJBBean10().setNotificationCallback(null, notificationTecnocom);

    //String assertMessage = notifResponse.getErrorMessage();
    //System.out.println(notifResponse.getErrorCode());
    //Assert.assertEquals(assertMessage,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResponse.getErrorCode());
  }

  @Test
  public void testCallNotificationWithBase64NotValidParamValue() throws Exception{

    String messageResponse;
    String base64String = "This is a String Text";

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue("1000.00");
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue("1000.00");
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue("1000.00");
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

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getErrorMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_NO_CUMPLE_FORMATO_$VALUE.getValue().toString(),notifResult.getErrorCode());

  }

  @Test
  public void testCallNotificationMoreThanOneParamsOnNull() throws Exception{

    String messageResponse;
    String base64String = "This is a String Text";

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue("1000.00");
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue("1000.00");
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue("1000.00");
    notificationTecnocomBody.setTipoTx(100);
    notificationTecnocomBody.setIdMensaje(1200);
    notificationTecnocomBody.setMerchantCode("0008902131");
    notificationTecnocomBody.setMerchantName("AMAZON UK");
    notificationTecnocomBody.setCountryIso3266Code(152);
    notificationTecnocomBody.setCountryDescription("Republica de Chile");
    notificationTecnocomBody.setPlaceName("Santiago");
    notificationTecnocomBody.setResolucionTx(null);

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(null);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getErrorMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getErrorCode());

  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndEmptyBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");

    notificationTecnocomHeader = new NotificationTecnocomHeader();

    notificationTecnocomBody = new NotificationTecnocomBody();

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getErrorMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getErrorCode());

  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndCompleteBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");

    notificationTecnocomHeader = new NotificationTecnocomHeader();

    NotificationTecnocomBody notificationTecnocomBody = new NotificationTecnocomBody();
    notificationTecnocomBody.setSdCurrencyCode(152);
    notificationTecnocomBody.setSdValue("1000.00");
    notificationTecnocomBody.setIlCurrencyCode(152);
    notificationTecnocomBody.setIlValue("1000.00");
    notificationTecnocomBody.setIdCurrencyCode(152);
    notificationTecnocomBody.setIdValue("1000.00");
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

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getErrorMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getErrorCode());

  }

  @Test
  public void testCallNotificationCompleteHeaderDataAndEmptyBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");

    notificationTecnocomHeader = new NotificationTecnocomHeader();
    notificationTecnocomHeader.setEntidad("9603");
    notificationTecnocomHeader.setCentroAlta("001");
    notificationTecnocomHeader.setCuenta("000000012345");
    notificationTecnocomHeader.setPan("411111******1111");

    NotificationTecnocomBody notificationTecnocomBody = new NotificationTecnocomBody();

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setHeader(notificationTecnocomHeader);
    notificationTecnocom.setBody(notificationTecnocomBody);
    notificationTecnocom.setBase64Data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getErrorMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getErrorCode());

  }

}
