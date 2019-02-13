
package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationTecnocomHeader extends BaseModel {

    private String entidad;
    private String centroAlta;
    private String cuenta;
    private String pan;

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getCentroAlta() {
        return centroAlta;
    }

    public void setCentroAlta(String centroAlta) {
        this.centroAlta = centroAlta;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getPan() {
        return pan;
    }

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
