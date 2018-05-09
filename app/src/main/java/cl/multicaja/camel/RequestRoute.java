package cl.multicaja.camel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Representa un requerimiento de una ruta camel, todos los requerimientos camel deben heredar de esta clase
 *
 * @autor vutreras
 */
public class RequestRoute implements Serializable {

  private Serializable data;

  public RequestRoute() {
    super();
  }

  public RequestRoute(Serializable data) {
    super();
    this.data = data;
  }

  public Serializable getData() {
    return data;
  }

  public void setData(Serializable data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
