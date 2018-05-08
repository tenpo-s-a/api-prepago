package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class NewRawTransaction {

  private RawTransactionHeader header;
  private RawTransactionBody body;
  private String base64Data;

  public RawTransactionHeader getHeader() {
    return header;
  }

  public void setHeader(RawTransactionHeader header) {
    this.header = header;
  }

  public RawTransactionBody getBody() {
    return body;
  }

  public void setBody(RawTransactionBody body) {
    this.body = body;
  }

  public String getBase64Data() {
    return base64Data;
  }

  public void setBase64Data(String base64Data) {
    this.base64Data = base64Data;
  }

}
