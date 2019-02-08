package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Test_sendNotificationTecnocom extends TestBaseUnitApi {


  private HttpResponse callNotification() {
    HttpResponse httpResponse = apiPOST(String.format("/1.0/prepaid_testhelpers/processor/notification"), null);
    System.out.println("RESP HTTP: " + httpResponse);
    return httpResponse;
  }

  private HttpResponse callNotificationWithParams(JsonObject body){
    System.out.println("body.toString(): "+body.toString());
    HttpResponse httpResponse = apiPOST(String.format("/1.0/prepaid_testhelpers/processor/notification"),body.toString());
    System.out.println("RESP HTTP: "+ httpResponse);
    return httpResponse;
  }

  private HashMap<String,Object> setJsonObjectToMap(JsonObject jsonObject) throws Exception{
    ObjectMapper mapperObj = new ObjectMapper();
    HashMap<String,Object> resultMap = mapperObj.readValue(jsonObject.toString(),
      new TypeReference<HashMap<String,Object>>(){});
    System.out.println("Output Map: "+resultMap);
    return resultMap;
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

  @Test
  public void testParametersWithParamsOk() {

    ObjectMapper mapperObj = new ObjectMapper();
    String base64String = Base64.getEncoder().encodeToString(("Test").getBytes(StandardCharsets.UTF_8));

    JsonObject params = Json.createObjectBuilder().
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

    HttpResponse httpResponse = callNotificationWithParams(params);
    System.out.println("Notificacion aceptada sin errores: "+ httpResponse.getStatus());
    Assert.assertEquals("Notificacion aceptada sin errores",202,httpResponse.getStatus());

  }

  @Test
  public void testParameterBase64Null(){
    ObjectMapper mapperObj = new ObjectMapper();
    String messageError = null;

    JsonObject params = Json.createObjectBuilder().
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

    HttpResponse httpResponse = callNotificationWithParams(params);

    messageError = "Notificacion no aceptada, campo base64 faltante HttpError:"+httpResponse.getStatus();
    System.out.println(messageError);
    Assert.assertEquals(messageError,422,httpResponse.getStatus());
  }

  @Test
  public void testParameterBase64FormatFail(){
    ObjectMapper mapperObj = new ObjectMapper();
    String messageError = null;

    JsonObject params = Json.createObjectBuilder().
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

    HttpResponse httpResponse = callNotificationWithParams(params);

    messageError = "Notificacion no aceptada, campo base64 incorrecto HttpError:"+httpResponse.getStatus();
    System.out.println(messageError);
    Assert.assertEquals(messageError,422,httpResponse.getStatus());
  }




}
