package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.NotificationCallback;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Test_TecnocomNotification extends TestBaseUnit {

  private NotificationCallback notificationCallback;

  //@Test
  //public void testCallNotification() throws Exception{
  //}

  @Test
  public void testCallNotificationSucessParams() throws Exception{

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    notificationCallback = new NotificationCallback();
    notificationCallback.setSd_currency_code(new Long(152));
    notificationCallback.setSd_value("1000.00");
    notificationCallback.setIl_currency_code(new Long(152));
    notificationCallback.setIl_value("1000.00");
    notificationCallback.setId_currency_code(new Long(152));
    notificationCallback.setId_value("1000.00");
    notificationCallback.setTipo_tx(new Long(100));
    notificationCallback.setId_mensaje(new Long(1200));
    notificationCallback.setMerchant_code("0008902131");
    notificationCallback.setMerchant_name("AMAZON UK");
    notificationCallback.setCountry_iso_3266_code(new Long(152));
    notificationCallback.setCountry_description("Republica de Chile");
    notificationCallback.setPlace_name("Santiago");
    notificationCallback.setResolucion_tx(new Long(100));
    notificationCallback.setBase64_data(base64String);

    NotificationCallback notifResult = getPrepaidEJBBean10().setNotificationCallback(null,notificationCallback);

    System.out.println(notifResult);

  }

  @Test
  public void testCallNotificationWithoutBase64() throws Exception{

    notificationCallback = new NotificationCallback();
    notificationCallback.setSd_currency_code(new Long(152));
    notificationCallback.setSd_value("1000.00");
    notificationCallback.setIl_currency_code(new Long(152));
    notificationCallback.setIl_value("1000.00");
    notificationCallback.setId_currency_code(new Long(152));
    notificationCallback.setId_value("1000.00");
    notificationCallback.setTipo_tx(new Long(100));
    notificationCallback.setId_mensaje(new Long(1200));
    notificationCallback.setMerchant_code("0008902131");
    notificationCallback.setMerchant_name("AMAZON UK");
    notificationCallback.setCountry_iso_3266_code(new Long(152));
    notificationCallback.setCountry_description("Republica de Chile");
    notificationCallback.setPlace_name("Santiago");
    notificationCallback.setResolucion_tx(new Long(100));

    NotificationCallback notifResult = getPrepaidEJBBean10().setNotificationCallback(null,notificationCallback);
    String assertMessage = "Base 64 is Empty";
    System.out.println(assertMessage);
    Assert.assertEquals(assertMessage,"101007",notifResult.getResponse_code());


  }

  @Test
  public void testCallNotificationBase64NotValid() throws Exception{

    String base64String = "This is a String Text";

    notificationCallback = new NotificationCallback();
    notificationCallback.setSd_currency_code(new Long(152));
    notificationCallback.setSd_value("1000.00");
    notificationCallback.setIl_currency_code(new Long(152));
    notificationCallback.setIl_value("1000.00");
    notificationCallback.setId_currency_code(new Long(152));
    notificationCallback.setId_value("1000.00");
    notificationCallback.setTipo_tx(new Long(100));
    notificationCallback.setId_mensaje(new Long(1200));
    notificationCallback.setMerchant_code("0008902131");
    notificationCallback.setMerchant_name("AMAZON UK");
    notificationCallback.setCountry_iso_3266_code(new Long(152));
    notificationCallback.setCountry_description("Republica de Chile");
    notificationCallback.setPlace_name("Santiago");
    notificationCallback.setResolucion_tx(new Long(100));
    notificationCallback.setBase64_data(base64String);

    NotificationCallback notifResult = getPrepaidEJBBean10().setNotificationCallback(null,notificationCallback);
    String assertMessage = "Base64 is invalid";
    System.out.println(assertMessage);
    Assert.assertEquals(assertMessage,"101007",notifResult.getResponse_code());

  }

  @Test
  public void testCallNotificationTwoParamsOnNull() throws Exception{

    String base64String = "This is a String Text";

    notificationCallback = new NotificationCallback();
    notificationCallback.setSd_currency_code(new Long(152));
    notificationCallback.setSd_value("1000.00");
    notificationCallback.setIl_currency_code(new Long(152));
    notificationCallback.setIl_value("1000.00");
    notificationCallback.setId_currency_code(new Long(152));
    notificationCallback.setId_value("1000.00");
    notificationCallback.setTipo_tx(new Long(100));
    notificationCallback.setId_mensaje(new Long(1200));
    notificationCallback.setMerchant_code("0008902131");
    notificationCallback.setMerchant_name("AMAZON UK");
    notificationCallback.setCountry_iso_3266_code(new Long(152));
    notificationCallback.setCountry_description("Republica de Chile");
    notificationCallback.setPlace_name("Santiago");
    //notificationCallback.setResolucion_tx(new Long(100));
    //notificationCallback.setBase64_data(base64String);

    NotificationCallback notifResult = getPrepaidEJBBean10().setNotificationCallback(null,notificationCallback);
    String assertMessage = "Exist more than one parameter on null ";
    System.out.println(assertMessage+"these are: "+notifResult.getResponse_message());
    Assert.assertEquals(assertMessage,"101004",notifResult.getResponse_code());

  }

}
