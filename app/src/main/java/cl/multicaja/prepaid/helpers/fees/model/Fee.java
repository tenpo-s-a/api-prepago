package cl.multicaja.prepaid.helpers.fees.model;

import cl.multicaja.core.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Fee extends BaseModel {
  @JsonProperty("total")
  private Long total;
  @JsonProperty("charges")
  private List<Charge> charges;

  public Long getTotal() { return total; }

  public void setTotal(Long total) { this.total = total; }

  public List<Charge> getCharges() {
    return charges;
  }

  public void setCharges(List<Charge> charges) {
    this.charges = charges;
  }
}
