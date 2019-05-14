package cl.multicaja.prepaid.helpers.fees.model;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Fee extends BaseModel {
  @JsonProperty("fee")
  private Long fee;
  @JsonProperty("iva")
  private Long iva;
  @JsonProperty("commission")
  private Long commission;

  public Long getFee() {
    return fee;
  }

  public void setFee(Long fee) {
    this.fee = fee;
  }

  public Long getIva() {
    return iva;
  }

  public void setIva(Long iva) {
    this.iva = iva;
  }

  public Long getCommission() {
    return commission;
  }

  public void setCommission(Long commission) {
    this.commission = commission;
  }
}
