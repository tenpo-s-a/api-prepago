package cl.multicaja.prepaid.kafka.events.model;

import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;

public class Fee {

  private NewAmountAndCurrency10 amount;
  private String type;

  public NewAmountAndCurrency10 getAmount() {
    return amount;
  }

  public void setAmount(NewAmountAndCurrency10 amount) {
    this.amount = amount;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
