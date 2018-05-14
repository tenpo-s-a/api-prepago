package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public class PrepaidTopup extends NewPrepaidTopup {

  private Integer id;
  private String status;
  private Integer userId;
  private Timestamps timestamps;

  private NewAmountAndCurrency commission;
  private NewAmountAndCurrency total;

  public PrepaidTopup() {
    super();
  }

  public PrepaidTopup(NewPrepaidTopup topupRequest) {
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

  public NewAmountAndCurrency getCommission() {
    return commission;
  }

  public void setCommission(AmountAndCurrency commission) {
    this.commission = commission;
  }

  public NewAmountAndCurrency getTotal() {
    return total;
  }

  public void setTotal(AmountAndCurrency total) {
    this.total = total;
  }
}
