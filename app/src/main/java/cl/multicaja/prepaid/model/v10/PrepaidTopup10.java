package cl.multicaja.prepaid.model.v10;

import cl.multicaja.users.model.v10.Timestamps;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 */
public class PrepaidTopup10 extends NewPrepaidTopup10 implements IPrepaidTransaction10 {

  private String status;
  private Timestamps timestamps;
  @JsonIgnore
  private NewAmountAndCurrency10 fee;
  @JsonIgnore
  private NewAmountAndCurrency10 total;
  @JsonIgnore
  private String messageId;

  // Utilizados para la respuesta al POS/switch
  private Long id;
  private Long userId;
  private String mcVoucherType;
  private List<Map<String, String>> mcVoucherData;

  public PrepaidTopup10() {
    super();
  }

  public PrepaidTopup10(NewPrepaidTopup10 topupRequest) {
    super(topupRequest.getAmount(), topupRequest.getTransactionId(), topupRequest.getRut(),
          topupRequest.getMerchantCode(), topupRequest.getMerchantName(), topupRequest.getMerchantCategory());
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
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

  public String getMcVoucherType() {
    return mcVoucherType;
  }

  public void setMcVoucherType(String mcVoucherType) {
    this.mcVoucherType = mcVoucherType;
  }

  public List<Map<String, String>> getMcVoucherData() {
    return mcVoucherData;
  }

  public void setMcVoucherData(List<Map<String, String>> mcVoucherData) {
    this.mcVoucherData = mcVoucherData;
  }
}
