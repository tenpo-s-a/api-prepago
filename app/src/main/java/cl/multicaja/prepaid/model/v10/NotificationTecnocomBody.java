
package cl.multicaja.prepaid.model.v10;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
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
    "resolucion_tx"
})
public class NotificationTecnocomBody extends BaseModel {

    @JsonProperty("sd_currency_code")
    private Integer sdCurrencyCode;
    @JsonProperty("sd_value")
    private String sdValue;
    @JsonProperty("il_currency_code")
    private Integer ilCurrencyCode;
    @JsonProperty("il_value")
    private String ilValue;
    @JsonProperty("id_currency_code")
    private Integer idCurrencyCode;
    @JsonProperty("id_value")
    private String idValue;
    @JsonProperty("tipo_tx")
    private Integer tipoTx;
    @JsonProperty("id_mensaje")
    private Integer idMensaje;
    @JsonProperty("merchant_code")
    private String merchantCode;
    @JsonProperty("merchant_name")
    private String merchantName;
    @JsonProperty("country_iso_3266_code")
    private Integer countryIso3266Code;
    @JsonProperty("country_description")
    private String countryDescription;
    @JsonProperty("place_name")
    private String placeName;
    @JsonProperty("resolucion_tx")
    private Integer resolucionTx;

    @JsonProperty("sd_currency_code")
    public Integer getSdCurrencyCode() {
        return sdCurrencyCode;
    }

    @JsonProperty("sd_currency_code")
    public void setSdCurrencyCode(Integer sdCurrencyCode) {
        this.sdCurrencyCode = sdCurrencyCode;
    }

    @JsonProperty("sd_value")
    public String getSdValue() {
        return sdValue;
    }

    @JsonProperty("sd_value")
    public void setSdValue(String sdValue) {
        this.sdValue = sdValue;
    }

    @JsonProperty("il_currency_code")
    public Integer getIlCurrencyCode() {
        return ilCurrencyCode;
    }

    @JsonProperty("il_currency_code")
    public void setIlCurrencyCode(Integer ilCurrencyCode) {
        this.ilCurrencyCode = ilCurrencyCode;
    }

    @JsonProperty("il_value")
    public String getIlValue() {
        return ilValue;
    }

    @JsonProperty("il_value")
    public void setIlValue(String ilValue) {
        this.ilValue = ilValue;
    }

    @JsonProperty("id_currency_code")
    public Integer getIdCurrencyCode() {
        return idCurrencyCode;
    }

    @JsonProperty("id_currency_code")
    public void setIdCurrencyCode(Integer idCurrencyCode) {
        this.idCurrencyCode = idCurrencyCode;
    }

    @JsonProperty("id_value")
    public String getIdValue() {
        return idValue;
    }

    @JsonProperty("id_value")
    public void setIdValue(String idValue) {
        this.idValue = idValue;
    }

    @JsonProperty("tipo_tx")
    public Integer getTipoTx() {
        return tipoTx;
    }

    @JsonProperty("tipo_tx")
    public void setTipoTx(Integer tipoTx) {
        this.tipoTx = tipoTx;
    }

    @JsonProperty("id_mensaje")
    public Integer getIdMensaje() {
        return idMensaje;
    }

    @JsonProperty("id_mensaje")
    public void setIdMensaje(Integer idMensaje) {
        this.idMensaje = idMensaje;
    }

    @JsonProperty("merchant_code")
    public String getMerchantCode() {
        return merchantCode;
    }

    @JsonProperty("merchant_code")
    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    @JsonProperty("merchant_name")
    public String getMerchantName() {
        return merchantName;
    }

    @JsonProperty("merchant_name")
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    @JsonProperty("country_iso_3266_code")
    public Integer getCountryIso3266Code() {
        return countryIso3266Code;
    }

    @JsonProperty("country_iso_3266_code")
    public void setCountryIso3266Code(Integer countryIso3266Code) {
        this.countryIso3266Code = countryIso3266Code;
    }

    @JsonProperty("country_description")
    public String getCountryDescription() {
        return countryDescription;
    }

    @JsonProperty("country_description")
    public void setCountryDescription(String countryDescription) {
        this.countryDescription = countryDescription;
    }

    @JsonProperty("place_name")
    public String getPlaceName() {
        return placeName;
    }

    @JsonProperty("place_name")
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    @JsonProperty("resolucion_tx")
    public Integer getResolucionTx() {
        return resolucionTx;
    }

    @JsonProperty("resolucion_tx")
    public void setResolucionTx(Integer resolucionTx) {
        this.resolucionTx = resolucionTx;
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
