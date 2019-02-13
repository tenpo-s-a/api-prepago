package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.NotificationTecnocom;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.PARAMETRO_NO_CUMPLE_FORMATO_$VALUE;

public class Test_TecnocomNotification extends TestBaseUnit {

  private NotificationTecnocom notificationTecnocom;


  @Test
  public void testCallNotificationAllParamsWithSuccessProcess() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");
    headers.put("entidad","9603");
    headers.put("centro_alta","0001");
    headers.put("cuenta","000000012345");
    headers.put("pan","411111******1111");

    notificationTecnocom = new NotificationTecnocom();
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
    notificationTecnocom.setCountryDescription("Republica de Chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));
    notificationTecnocom.setBase64Data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,"002",notifResult.getCode());

  }

  @Test
  public void testCallNotificationWithoutBase64ParamOrNullValue() throws Exception{

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");
    headers.put("entidad","9603");
    headers.put("centro_alta","0001");
    headers.put("cuenta","000000012345");
    headers.put("pan","411111******1111");

    notificationTecnocom = new NotificationTecnocom();
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
    notificationTecnocom.setCountryDescription("Republica de Chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    String assertMessage = notifResult.getMessage();
    System.out.println(notifResult.getCode());
    Assert.assertEquals(assertMessage,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getCode());


  }

  @Test
  public void testCallNotificationWithBase64NotValidParamValue() throws Exception{

    String messageResponse;
    String base64String = "This is a String Text";

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");
    headers.put("entidad","9603");
    headers.put("centro_alta","0001");
    headers.put("cuenta","000000012345");
    headers.put("pan","411111******1111");

    notificationTecnocom = new NotificationTecnocom();
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
    notificationTecnocom.setCountryDescription("Republica de Chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));
    notificationTecnocom.setBase64Data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_NO_CUMPLE_FORMATO_$VALUE.getValue().toString(),notifResult.getCode());

  }

  @Test
  public void testCallNotificationMoreThanOneParamsOnNull() throws Exception{

    String messageResponse;
    String base64String = "This is a String Text";

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");
    headers.put("entidad","9603");
    headers.put("centro_alta","0001");
    headers.put("cuenta","000000012345");
    headers.put("pan","411111******1111");

    notificationTecnocom = new NotificationTecnocom();
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
    notificationTecnocom.setCountryDescription("Republica de Chile");
    notificationTecnocom.setPlaceName("Santiago");
    //notificationTecnocom.setResolucionTx(new Long(100));
    //notificationTecnocom.setBase64Data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getCode());

  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndEmptyBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    notificationTecnocom = new NotificationTecnocom();

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getCode());

  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndCompleteBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();

    notificationTecnocom = new NotificationTecnocom();
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
    notificationTecnocom.setCountryDescription("Republica de Chile");
    notificationTecnocom.setPlaceName("Santiago");
    notificationTecnocom.setResolucionTx(new Long(100));
    notificationTecnocom.setBase64Data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getCode());

  }

  @Test
  public void testCallNotificationCompleteHeaderDataAndEmptyBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    headers.put("Content-Type","application/json");
    headers.put("entidad","9603");
    headers.put("centro_alta","0001");
    headers.put("cuenta","000000012345");
    headers.put("pan","411111******1111");

    notificationTecnocom = new NotificationTecnocom();

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getMessage();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,PARAMETRO_FALTANTE_$VALUE.getValue().toString(),notifResult.getCode());

  }

}
