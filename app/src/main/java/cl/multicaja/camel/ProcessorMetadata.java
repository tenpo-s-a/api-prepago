package cl.multicaja.camel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public class ProcessorMetadata implements Serializable {

  private int retry;
  private String endpoint;
  private boolean redirect;

  public ProcessorMetadata(int retry, String endpoint) {
    this.retry = retry;
    this.setEndpoint(endpoint);
  }

  public ProcessorMetadata(int retry, String endpoint, boolean redirect) {
    this.retry = retry;
    this.setEndpoint(endpoint);
    this.redirect = redirect;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry(int retry) {
    this.retry = retry;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    try {
      if (endpoint.contains("?")) {
        this.endpoint = endpoint.split("\\?")[0];
      } else {
        this.endpoint = endpoint;
      }
    } catch(Exception ex) {
      this.endpoint = endpoint;
    }
  }

  public boolean isRedirect() {
    return redirect;
  }

  public void setRedirect(boolean redirect) {
    this.redirect = redirect;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }
}
