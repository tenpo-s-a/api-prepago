
package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationTecnocom extends BaseModel {

    private NotificationTecnocomHeader header;
    private NotificationTecnocomBody body;
    private String base64Data;

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
}
