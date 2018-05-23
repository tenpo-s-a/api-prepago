package cl.multicaja.camel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Representa un requerimiento de una ruta camel, todos los requerimientos camel deben heredar de esta clase
 *
 * @autor vutreras
 */
public class RequestRoute<E extends Serializable> implements Serializable {

  private E data;

  private int retryCount = 0;

  public RequestRoute() {
    super();
  }

  public RequestRoute(E data) {
    super();
    this.data = data;
  }

  public E getData() {
    return data;
  }

  public void setData(E data) {
    this.data = data;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public void retryCountNext() {
    this.retryCount++;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
