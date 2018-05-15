package cl.multicaja.prepaid.domain.v10;

import cl.multicaja.users.model.v10.Timestamps;

/**
 * @author abarazarte
 */
public class PrepaidTopup10 extends NewPrepaidTopup {

  private Integer id;
  private String status;
  private Integer userId;
  private Timestamps timestamps;

  public PrepaidTopup10() {
    super();
  }

  public PrepaidTopup10(NewPrepaidTopup topupRequest) {
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

}
