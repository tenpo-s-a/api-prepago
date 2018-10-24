package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import cl.multicaja.core.utils.http.HttpError;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BaseModel {

  private String code;
  private String message;
  @JsonIgnore
  private HttpError httpError = HttpError.NONE;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public HttpError getHttpError() {
    return httpError;
  }

  public void setHttpError(HttpError httpError) {
    this.httpError = httpError;
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
