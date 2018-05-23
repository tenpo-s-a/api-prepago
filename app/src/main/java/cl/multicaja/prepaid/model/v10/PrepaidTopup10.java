package cl.multicaja.prepaid.model.v10;

import cl.multicaja.users.model.v10.Timestamps;

/**
 * @author abarazarte
 */
public class PrepaidTopup10 extends NewPrepaidTopup10 {

  private Integer id;
  private String status;
  private Integer userId;
  private Timestamps timestamps;
  private NewAmountAndCurrency10 fee;
  private NewAmountAndCurrency10 total;
  private String messageId;

  public PrepaidTopup10() {
    super();
  }

  public PrepaidTopup10(NewPrepaidTopup10 topupRequest) {
    super(topupRequest.getAmount(), topupRequest.getTransactionId(), topupRequest.getRut(), topupRequest.getMerchantCode());
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(Timestamps timestamps) {
    this.timestamps = timestamps;
  }

  public NewAmountAndCurrency10 getFee() {
    return fee;
  }

  public void setFee(NewAmountAndCurrency10 fee) {
    this.fee = fee;
  }

  public NewAmountAndCurrency10 getTotal() {
    return total;
  }

  public void setTotal(NewAmountAndCurrency10 total) {
    this.total = total;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }
}
