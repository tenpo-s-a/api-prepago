
package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

public class NotificationTecnocom extends BaseModel {

    private NotificationTecnocomHeader header;
    private NotificationTecnocomBody body;
    private String base64Data;
    private String errorCode;
    private String errorMessage;

    public NotificationTecnocomHeader getHeader() {
        return header;
    }

    public void setHeader(NotificationTecnocomHeader header) {
        this.header = header;
    }

    public NotificationTecnocomBody getBody() {
        return body;
    }

    public void setBody(NotificationTecnocomBody body) {
        this.body = body;
    }

    public String getBase64Data() {
        return base64Data;
    }

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
