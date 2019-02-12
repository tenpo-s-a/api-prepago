package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Test_sendNotificationTecnocom extends TestBaseUnitApi {
  

  private HttpResponse callNotification(JsonObject body,HttpHeader[] headers) {
    HttpResponse httpResponse = apiPOST(String.format("/1.0/prepaid_testhelpers/processor/notification"), body.toString(),headers);
    System.out.println("RESP HTTP: " + httpResponse);
    return httpResponse;
  }

  @Test
  public void testCallNotificationAllParamsWithSuccessProcess() {

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
    JsonObject jsonBody = Json.createObjectBuilder().
      add("sd_currency_code",152).
      add("sd_value","1000.00").
      add("il_currency_code",152).
      add("il_value","1000.00").
      add("id_currency_code",152).
      add("id_value","1000.00").
      add("tipo_tx", 100).
      add("id_mensaje",1200).
      add("merchant_code","0008902131").
      add("merchant_name","AMAZON UK").
      add("country_iso_3266_code",152).
      add("country_description","Republica de Chile").
      add("place_name","Santiago").
      add("resolucion_tx",100).
      add("base64_data",base64String).build();

    HttpResponse httpResponse = callNotification(jsonBody,headers);

    messageResponse = httpResponse.getResp();
    Assert.assertEquals(messageResponse,202,httpResponse.getStatus());

  }

  @Test
  public void testCallNotificationWithoutBase64ParamOrNullValue(){
    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    JsonObject jsonBody = Json.createObjectBuilder().
      add("sd_currency_code",152).
      add("sd_value","1000.00").
      add("il_currency_code",152).
      add("il_value","1000.00").
      add("id_currency_code",152).
      add("id_value","1000.00").
      add("tipo_tx", 100).
      add("id_mensaje",1200).
      add("merchant_code","0008902131").
      add("merchant_name","AMAZON UK").
      add("country_iso_3266_code",152).
      add("country_description","Republica de Chile").
      add("place_name","Santiago").
      add("resolucion_tx",100).build();
      //add("base64_data","test").build();

    HttpResponse httpResponse = callNotification(jsonBody,headers);

    messageResponse = "Error:"+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testCallNotificationWithBase64NotValidParamValue(){
    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse = null;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    JsonObject jsonBody = Json.createObjectBuilder().
      add("sd_currency_code",152).
      add("sd_value","1000.00").
      add("il_currency_code",152).
      add("il_value","1000.00").
      add("id_currency_code",152).
      add("id_value","1000.00").
      add("tipo_tx", 100).
      add("id_mensaje",1200).
      add("merchant_code","0008902131").
      add("merchant_name","AMAZON UK").
      add("country_iso_3266_code",152).
      add("country_description","Republica de Chile").
      add("place_name","Santiago").
      add("resolucion_tx",100).
      add("base64_data","This is a Test").build();

    HttpResponse httpResponse = callNotification(jsonBody,headers);

    messageResponse = "Error: "+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,422,httpResponse.getStatus());
  }


  @Test
  public void testCallNotificationMoreThanOneParamsOnNull(){
    ObjectMapper mapperObj = new ObjectMapper();
    String messageResponse = null;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json"),
      new HttpHeader("entidad","9603"),
      new HttpHeader("centro_alta","0001"),
      new HttpHeader("cuenta","000000012345"),
      new HttpHeader("pan","411111******1111")
    };

    JsonObject jsonBody = Json.createObjectBuilder().
      add("sd_currency_code",152).
      add("sd_value","1000.00").
      add("il_currency_code",152).
      add("il_value","1000.00").
      add("id_currency_code",152).
      add("id_value","1000.00").
      add("tipo_tx", 100).
      add("id_mensaje",1200).
      add("merchant_code","0008902131").
      add("merchant_name","AMAZON UK").
      add("country_iso_3266_code",152).
      add("country_description","Republica de Chile").
      add("place_name","Santiago").build();
      //add("resolucion_tx",100).
      //add("base64_data","This is a Test").build();

    HttpResponse httpResponse = callNotification(jsonBody,headers);

    messageResponse = httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndEmptyBodyData(){

    String messageResponse;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    JsonObject jsonBody = Json.createObjectBuilder().build();

    HttpResponse httpResponse = callNotification(jsonBody,headers);
    messageResponse = "Error: "+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testCallNotificationEmptyHeaderDataAndCompleteBodyData(){

    String messageResponse;

    HttpHeader[] headers = {
      new HttpHeader("Content-Type","application/json")
    };

    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));
    JsonObject jsonBody = Json.createObjectBuilder().
      add("sd_currency_code",152).
      add("sd_value","1000.00").
      add("il_currency_code",152).
      add("il_value","1000.00").
      add("id_currency_code",152).
      add("id_value","1000.00").
      add("tipo_tx", 100).
      add("id_mensaje",1200).
      add("merchant_code","0008902131").
      add("merchant_name","AMAZON UK").
      add("country_iso_3266_code",152).
      add("country_description","Republica de Chile").
      add("place_name","Santiago").
      add("resolucion_tx",100).
      add("base64_data",base64String).build();

    HttpResponse httpResponse = callNotification(jsonBody,headers);
    messageResponse = "Error: "+httpResponse.getResp();
    System.out.println(messageResponse);
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

  @Test
  public void testcallNotificationHeaderDataAndEmptyBodyData(){
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

    JsonObject jsonBody = Json.createObjectBuilder().build();

    HttpResponse httpResponse = callNotification(jsonBody,headers);
    messageResponse = "Error: "+httpResponse.getResp();
    Assert.assertEquals(messageResponse,400,httpResponse.getStatus());
  }

}
