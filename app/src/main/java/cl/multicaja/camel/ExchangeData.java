package cl.multicaja.camel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Representa un mensaje de una ruta camel
 *
 * @autor vutreras
 */
public class ExchangeData<E extends Serializable> implements Serializable {

  private E data;

  private int retryCount = 0;

  private ExchangeContext exchangeContext;

  public ExchangeData() {
    super();
  }

  public ExchangeData(E data) {
    super();
    this.data = data;
  }

  public ExchangeData(E data, ExchangeContext exchangeContext) {
    super();
    this.data = data;
    this.exchangeContext = exchangeContext;
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
