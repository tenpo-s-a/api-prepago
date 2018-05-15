package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

/**
 * @author abarazarte
 */
public class NewRawTransaction10 extends BaseModel {

  private RawTransactionHeader10 header;
  private RawTransactionBody10 body;
  private String base64Data;

  public NewRawTransaction10() {
    super();
  }

  public RawTransactionHeader10 getHeader() {
    return header;
  }

  public void setHeader(RawTransactionHeader10 header) {
    this.header = header;
  }

  public RawTransactionBody10 getBody() {
    return body;
  }

  public void setBody(RawTransactionBody10 body) {
    this.body = body;
  }

  public String getBase64Data() {
    return base64Data;
  }

  public void setBase64Data(String base64Data) {
    this.base64Data = base64Data;
  }

}
