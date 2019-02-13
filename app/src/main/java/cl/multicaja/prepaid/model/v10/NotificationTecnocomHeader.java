
package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "entidad",
    "centro_alta",
    "cuenta",
    "pan"
})
public class NotificationTecnocomHeader extends BaseModel {

    @JsonProperty("entidad")
    private String entidad;
    @JsonProperty("centro_alta")
    private String centroAlta;
    @JsonProperty("cuenta")
    private String cuenta;
    @JsonProperty("pan")
    private String pan;

    @JsonProperty("entidad")
    public String getEntidad() {
        return entidad;
    }

    @JsonProperty("entidad")
    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    @JsonProperty("centro_alta")
    public String getCentroAlta() {
        return centroAlta;
    }

    @JsonProperty("centro_alta")
    public void setCentroAlta(String centroAlta) {
        this.centroAlta = centroAlta;
    }

    @JsonProperty("cuenta")
    public String getCuenta() {
        return cuenta;
    }

    @JsonProperty("cuenta")
    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    @JsonProperty("pan")
    public String getPan() {
        return pan;
    }

    @JsonProperty("pan")
    public void setPan(String pan) {
        this.pan = pan;
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
