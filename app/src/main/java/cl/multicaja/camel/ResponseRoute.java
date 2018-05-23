package cl.multicaja.camel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Representa una respuesta de una ruta camel, todas las respuestas camel deben heredar de esta clase
 *
 * @autor vutreras
 */
public class ResponseRoute<E extends Serializable> implements Serializable {

  private E data;

  private ExchangeContext exchangeContext;

  public ResponseRoute() {
    super();
  }

  public ResponseRoute(E data) {
    super();
    this.data = data;
  }

  public ResponseRoute(E data, ExchangeContext exchangeContext) {
    super();
    this.data = data;
    this.exchangeContext = exchangeContext;
  }

  public E getData() {
    return data;
  }

  public void setData(E data) {
    this.data = data;
  }

  public ExchangeContext getExchangeContext() {
    return exchangeContext;
  }

  public void setExchangeContext(ExchangeContext exchangeContext) {
    this.exchangeContext = exchangeContext;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
