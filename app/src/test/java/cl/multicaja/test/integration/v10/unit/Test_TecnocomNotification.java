package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.NotificationTecnocom;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

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
    notificationTecnocom.setSd_currency_code(new Long(152));
    notificationTecnocom.setSd_value("1000.00");
    notificationTecnocom.setIl_currency_code(new Long(152));
    notificationTecnocom.setIl_value("1000.00");
    notificationTecnocom.setId_currency_code(new Long(152));
    notificationTecnocom.setId_value("1000.00");
    notificationTecnocom.setTipo_tx(new Long(100));
    notificationTecnocom.setId_mensaje(new Long(1200));
    notificationTecnocom.setMerchant_code("0008902131");
    notificationTecnocom.setMerchant_name("AMAZON UK");
    notificationTecnocom.setCountry_iso_3266_code(new Long(152));
    notificationTecnocom.setCountry_description("Republica de Chile");
    notificationTecnocom.setPlace_name("Santiago");
    notificationTecnocom.setResolucion_tx(new Long(100));
    notificationTecnocom.setBase64_data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getResponse_message();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,"002",notifResult.getResponse_code());

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
    notificationTecnocom.setSd_currency_code(new Long(152));
    notificationTecnocom.setSd_value("1000.00");
    notificationTecnocom.setIl_currency_code(new Long(152));
    notificationTecnocom.setIl_value("1000.00");
    notificationTecnocom.setId_currency_code(new Long(152));
    notificationTecnocom.setId_value("1000.00");
    notificationTecnocom.setTipo_tx(new Long(100));
    notificationTecnocom.setId_mensaje(new Long(1200));
    notificationTecnocom.setMerchant_code("0008902131");
    notificationTecnocom.setMerchant_name("AMAZON UK");
    notificationTecnocom.setCountry_iso_3266_code(new Long(152));
    notificationTecnocom.setCountry_description("Republica de Chile");
    notificationTecnocom.setPlace_name("Santiago");
    notificationTecnocom.setResolucion_tx(new Long(100));

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    //String assertMessage = "Base 64 is Empty";
    String assertMessage = notifResult.getResponse_message();
    System.out.println(assertMessage);
    Assert.assertEquals(assertMessage,"101004",notifResult.getResponse_code());


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
    notificationTecnocom.setSd_currency_code(new Long(152));
    notificationTecnocom.setSd_value("1000.00");
    notificationTecnocom.setIl_currency_code(new Long(152));
    notificationTecnocom.setIl_value("1000.00");
    notificationTecnocom.setId_currency_code(new Long(152));
    notificationTecnocom.setId_value("1000.00");
    notificationTecnocom.setTipo_tx(new Long(100));
    notificationTecnocom.setId_mensaje(new Long(1200));
    notificationTecnocom.setMerchant_code("0008902131");
    notificationTecnocom.setMerchant_name("AMAZON UK");
    notificationTecnocom.setCountry_iso_3266_code(new Long(152));
    notificationTecnocom.setCountry_description("Republica de Chile");
    notificationTecnocom.setPlace_name("Santiago");
    notificationTecnocom.setResolucion_tx(new Long(100));
    notificationTecnocom.setBase64_data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getResponse_message();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,"101007",notifResult.getResponse_code());

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
    notificationTecnocom.setSd_currency_code(new Long(152));
    notificationTecnocom.setSd_value("1000.00");
    notificationTecnocom.setIl_currency_code(new Long(152));
    notificationTecnocom.setIl_value("1000.00");
    notificationTecnocom.setId_currency_code(new Long(152));
    notificationTecnocom.setId_value("1000.00");
    notificationTecnocom.setTipo_tx(new Long(100));
    notificationTecnocom.setId_mensaje(new Long(1200));
    notificationTecnocom.setMerchant_code("0008902131");
    notificationTecnocom.setMerchant_name("AMAZON UK");
    notificationTecnocom.setCountry_iso_3266_code(new Long(152));
    notificationTecnocom.setCountry_description("Republica de Chile");
    notificationTecnocom.setPlace_name("Santiago");
    //notificationTecnocom.setResolucion_tx(new Long(100));
    //notificationTecnocom.setBase64_data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getResponse_message();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,"101004",notifResult.getResponse_code());

  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndEmptyBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();
    notificationTecnocom = new NotificationTecnocom();

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getResponse_message();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,"101004",notifResult.getResponse_code());

  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndCompleteBodyData() throws Exception{

    String messageResponse;
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    HashMap<String,Object>headers = new HashMap<>();

    notificationTecnocom = new NotificationTecnocom();
    notificationTecnocom.setSd_currency_code(new Long(152));
    notificationTecnocom.setSd_value("1000.00");
    notificationTecnocom.setIl_currency_code(new Long(152));
    notificationTecnocom.setIl_value("1000.00");
    notificationTecnocom.setId_currency_code(new Long(152));
    notificationTecnocom.setId_value("1000.00");
    notificationTecnocom.setTipo_tx(new Long(100));
    notificationTecnocom.setId_mensaje(new Long(1200));
    notificationTecnocom.setMerchant_code("0008902131");
    notificationTecnocom.setMerchant_name("AMAZON UK");
    notificationTecnocom.setCountry_iso_3266_code(new Long(152));
    notificationTecnocom.setCountry_description("Republica de Chile");
    notificationTecnocom.setPlace_name("Santiago");
    notificationTecnocom.setResolucion_tx(new Long(100));
    notificationTecnocom.setBase64_data(base64String);

    NotificationTecnocom notifResult = getPrepaidEJBBean10().setNotificationCallback(headers, notificationTecnocom);
    messageResponse = notifResult.getResponse_message();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,"101004",notifResult.getResponse_code());

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
    messageResponse = notifResult.getResponse_message();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,"101004",notifResult.getResponse_code());

  }

}
