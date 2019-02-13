
package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "header",
    "body",
    "base64_data"
})
//@JsonIgnoreProperties
public class NotificationTecnocom extends BaseModel {

    @JsonProperty("header")
    private NotificationTecnocomHeader header;
    @JsonProperty("body")
    private NotificationTecnocomBody body;
    @JsonProperty("base64_data")
    private String base64Data;
    //@JsonProperty("error_code")
    private String errorCode;
    //@JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("header")
    public NotificationTecnocomHeader getHeader() {
        return header;
    }

    @JsonProperty("header")
    public void setHeader(NotificationTecnocomHeader header) {
        this.header = header;
    }

    @JsonProperty("body")
    public NotificationTecnocomBody getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(NotificationTecnocomBody body) {
        this.body = body;
    }

    @JsonProperty("base64_data")
    public String getBase64Data() {
        return base64Data;
    }

    @JsonProperty("base64_data")
    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }

    public String getErrorCode() {
      return errorCode;
    }

    public void setErrorCode(String errorCode) {
      this.errorCode = errorCode;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }


}
