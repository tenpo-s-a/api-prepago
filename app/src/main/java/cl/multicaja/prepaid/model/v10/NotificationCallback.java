package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.helpers.freshdesk.model.v10.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import java.util.Base64;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
  "id",
  "sd_currency_code",
  "sd_value",
  "il_currency_code",
  "il_value",
  "id_currency_code",
  "id_value",
  "tipo_tx",
  "id_mensaje",
  "merchant_code",
  "merchant_name",
  "country_iso_3266_code",
  "country_description",
  "place_name",
  "resolucion_tx",
  "base64_data"
})
public class NotificationCallback extends BaseModel {

  @JsonProperty("id")
  private Long id;
  @JsonProperty("sd_currency_code")
  private Long sd_currency_code;
  @JsonProperty("sd_value")
  private String sd_value;
  @JsonProperty("il_currency_code")
  private Long il_currency_code;
  @JsonProperty("il_value")
  private String il_value;
  @JsonProperty("id_currency_code")
  private Long id_currency_code;
  @JsonProperty("id_value")
  private String id_value;
  @JsonProperty("tipo_tx")
  private Long tipo_tx;
  @JsonProperty("id_mensaje")
  private Long id_mensaje;
  @JsonProperty("merchant_code")
  private String merchant_code;
  @JsonProperty("merchant_name")
  private String merchant_name;
  @JsonProperty("country_iso_3266_code")
  private Long country_iso_3266_code;
  @JsonProperty("country_description")
  private String country_description;
  @JsonProperty("place_name")
  private String place_name;
  @JsonProperty("resolucion_tx")
  private Long resolucion_tx;
  @JsonProperty("base64_data")
  private Base64 base64_data;


  //@JsonProperty("base64_data")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSd_currency_code() {
    return sd_currency_code;
  }

  public void setSd_currency_code(Long sd_currency_code) {
    this.sd_currency_code = sd_currency_code;
  }

  public String getSd_value() {
    return sd_value;
  }

  public void setSd_value(String sd_value) {
    this.sd_value = sd_value;
  }

  public Long getIl_currency_code() {
    return il_currency_code;
  }

  public void setIl_currency_code(Long il_currency_code) {
    this.il_currency_code = il_currency_code;
  }

  public String getIl_value() {
    return il_value;
  }

  public void setIl_value(String il_value) {
    this.il_value = il_value;
  }

  public Long getId_currency_code() {
    return id_currency_code;
  }

  public void setId_currency_code(Long id_currency_code) {
    this.id_currency_code = id_currency_code;
  }

  public String getId_value() {
    return id_value;
  }

  public void setId_value(String id_value) {
    this.id_value = id_value;
  }

  public Long getTipo_tx() {
    return tipo_tx;
  }

  public void setTipo_tx(Long tipo_tx) {
    this.tipo_tx = tipo_tx;
  }

  public Long getId_mensaje() {
    return id_mensaje;
  }

  public void setId_mensaje(Long id_mensaje) {
    this.id_mensaje = id_mensaje;
  }

  public String getMerchant_code() {
    return merchant_code;
  }

  public void setMerchant_code(String merchant_code) {
    this.merchant_code = merchant_code;
  }

  public String getMerchant_name() {
    return merchant_name;
  }

  public void setMerchant_name(String merchant_name) {
    this.merchant_name = merchant_name;
  }

  public Long getCountry_iso_3266_code() {
    return country_iso_3266_code;
  }

  public void setCountry_iso_3266_code(Long country_iso_3266_code) {
    this.country_iso_3266_code = country_iso_3266_code;
  }

  public String getCountry_description() {
    return country_description;
  }

  public void setCountry_description(String country_description) {
    this.country_description = country_description;
  }

  public String getPlace_name() {
    return place_name;
  }

  public void setPlace_name(String place_name) {
    this.place_name = place_name;
  }

  public Long getResolucion_tx() {
    return resolucion_tx;
  }

  public void setResolucion_tx(Long resolucion_tx) {
    this.resolucion_tx = resolucion_tx;
  }

  public Base64 getBase64_data() {
    return base64_data;
  }

  public void setBase64_data(Base64 base64_data) {
    this.base64_data = base64_data;
  }
}
