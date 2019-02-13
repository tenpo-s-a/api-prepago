
package cl.multicaja.prepaid.model.v10;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationTecnocomBody extends BaseModel {

  //@JsonProperty("sd_currency_code")
  private Integer sdCurrencyCode;
  //@JsonProperty("sd_value")
  private String sdValue;
  //@JsonProperty("il_currency_code")
  private Integer ilCurrencyCode;
  //@JsonProperty("il_value")
  private String ilValue;
  //@JsonProperty("id_currency_code")
  private Integer idCurrencyCode;
  //@JsonProperty("id_value")
  private String idValue;
  //@JsonProperty("tipo_tx")
  private Integer tipoTx;
  //@JsonProperty("id_mensaje")
  private Integer idMensaje;
  //@JsonProperty("merchant_code")
  private String merchantCode;
  //@JsonProperty("merchant_name")
  private String merchantName;
  @JsonProperty("country_iso_3266_code")
  private Integer countryIso3266Code;
  //@JsonProperty("country_description")
  private String countryDescription;
  //@JsonProperty("place_name")
  private String placeName;
  //@JsonProperty("resolucion_tx")
  private Integer resolucionTx;

    public Integer getSdCurrencyCode() {
        return sdCurrencyCode;
    }

    public void setSdCurrencyCode(Integer sdCurrencyCode) {
        this.sdCurrencyCode = sdCurrencyCode;
    }

    public String getSdValue() {
        return sdValue;
    }

    public void setSdValue(String sdValue) {
        this.sdValue = sdValue;
    }

    public Integer getIlCurrencyCode() {
        return ilCurrencyCode;
    }

    public void setIlCurrencyCode(Integer ilCurrencyCode) {
        this.ilCurrencyCode = ilCurrencyCode;
    }

    public String getIlValue() {
        return ilValue;
    }

    public void setIlValue(String ilValue) {
        this.ilValue = ilValue;
    }

    public Integer getIdCurrencyCode() {
        return idCurrencyCode;
    }

    public void setIdCurrencyCode(Integer idCurrencyCode) {
        this.idCurrencyCode = idCurrencyCode;
    }

    public String getIdValue() {
        return idValue;
    }

    public void setIdValue(String idValue) {
        this.idValue = idValue;
    }

    public Integer getTipoTx() {
        return tipoTx;
    }

    public void setTipoTx(Integer tipoTx) {
        this.tipoTx = tipoTx;
    }

    public Integer getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Integer idMensaje) {
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

    public Integer getCountryIso3266Code() {
        return countryIso3266Code;
    }

    public void setCountryIso3266Code(Integer countryIso3266Code) {
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

    public Integer getResolucionTx() {
        return resolucionTx;
    }

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
