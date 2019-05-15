package cl.multicaja.prepaid.helpers.fees.model;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Charge extends BaseModel {
  @JsonProperty("charge_type")
  private ChargeType chargeType;
  @JsonProperty("amount")
  private Long amount;

  public ChargeType getChargeType() {
    return chargeType;
  }

  public void setChargeType(ChargeType chargeType) {
    this.chargeType = chargeType;
  }

  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }
}
