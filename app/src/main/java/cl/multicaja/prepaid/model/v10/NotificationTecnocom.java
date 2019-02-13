package cl.multicaja.prepaid.model.v10;

import cl.multicaja.prepaid.helpers.freshdesk.model.v10.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationTecnocom extends BaseModel {

  private static Log log = LogFactory.getLog(NotificationTecnocom.class);

  private Long id;

  //@JsonProperty("sd_currency_code")
  //@JsonIgnore
  private Long sdCurrencyCode;

  //@JsonProperty("sd_value")
  //@JsonIgnore
  private String sdValue;

  //@JsonProperty("il_currency_code")
  //@JsonIgnore
  private Long ilCurrencyCode;

  //@JsonProperty("il_value")
  //@JsonIgnore
  private String ilValue;

  //@JsonProperty("id_currency_code")
  //@JsonIgnore
  private Long idCurrencyCode;

  //@JsonProperty("id_value")
  //@JsonIgnore
  private String idValue;

  //@JsonProperty("tipo_tx")
  //@JsonIgnore
  private Long tipoTx;

  //@JsonProperty("id_mensaje")
  //@JsonIgnore
  private Long idMensaje;

  //@JsonProperty("merchant_code")
  //@JsonIgnore
  private String merchantCode;

  //@JsonProperty("merchant_name")
  //@JsonIgnore
  private String merchantName;

  //@JsonProperty("country_iso_3266_code")
  //@JsonIgnore
  private Long countryIso3266Code;

  //@JsonProperty("country_description")
  //@JsonIgnore
  private String countryDescription;

  //@JsonProperty("place_name")
  //@JsonIgnore
  private String placeName;

  //@JsonProperty("resolucion_tx")
  //@JsonIgnore
  private Long resolucionTx;

  //@JsonProperty("base64_data")
  //@JsonIgnore
  private String base64Data;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSdCurrencyCode() {
    return sdCurrencyCode;
  }

  public void setSdCurrencyCode(Long sdCurrencyCode) {
    this.sdCurrencyCode = sdCurrencyCode;
  }

  public String getSdValue() {
    return sdValue;
  }

  public void setSdValue(String sd_value) {
    this.sdValue = sd_value;
  }

  public Long getIlCurrencyCode() {
    return ilCurrencyCode;
  }

  public void setIlCurrencyCode(Long ilCurrencyCode) {
    this.ilCurrencyCode = ilCurrencyCode;
  }

  public String getIlValue() {
    return ilValue;
  }

  public void setIlValue(String ilValue) {
    this.ilValue = ilValue;
  }

  public Long getIdCurrencyCode() {
    return idCurrencyCode;
  }

  public void setIdCurrencyCode(Long idCurrencyCode) {
    this.idCurrencyCode = idCurrencyCode;
  }

  public String getIdValue() {
    return idValue;
  }

  public void setIdValue(String idValue) {
    this.idValue = idValue;
  }

  public Long getTipoTx() {
    return tipoTx;
  }

  public void setTipoTx(Long tipoTx) {
    this.tipoTx = tipoTx;
  }

  public Long getIdMensaje() {
    return idMensaje;
  }

  public void setIdMensaje(Long idMensaje) {
    this.idMensaje = idMensaje;
  }

  public String getMerchantCode() {
    return merchantCode;
  }

  public void setMerchantCode(String merchantCode) {
    this.merchantCode = merchantCode;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  public Long getCountryIso3266Code() {
    return countryIso3266Code;
  }

  public void setCountryIso3266Code(Long countryIso3266Code) {
    this.countryIso3266Code = countryIso3266Code;
  }

  public String getCountryDescription() {
    return countryDescription;
  }

  public void setCountryDescription(String countryDescription) {
    this.countryDescription = countryDescription;
  }

  public String getPlaceName() {
    return placeName;
  }

  public void setPlaceName(String placeName) {
    this.placeName = placeName;
  }

  public Long getResolucionTx() {
    return resolucionTx;
  }

  public void setResolucionTx(Long resolucionTx) {
    this.resolucionTx = resolucionTx;
  }

  public String getBase64Data() {
    return base64Data;
  }

  public void setBase64Data(String base64Data) {
    this.base64Data = base64Data;
  }

  public HashMap<String,Object> checkNull(String [] notNullFields) throws IllegalAccessException {

    HashMap <String,Object> nullFields = new HashMap<String,Object>();

    for (Field f : getClass().getDeclaredFields()){
      if(Arrays.asList(notNullFields).contains(f.getName())) {
        if(f.get(this) == null){
          nullFields.put(f.getName(), f.get(this));
        }
      }
    }
    return nullFields;
  }
}
