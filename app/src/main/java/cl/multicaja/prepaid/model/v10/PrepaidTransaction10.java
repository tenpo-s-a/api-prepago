package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.util.Date;

public class PrepaidTransaction10 extends BaseModel {

  private Date date;
  private String description;
  private NewAmountAndCurrency10 amountPrimary;
  private NewAmountAndCurrency10 amountSecondary;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public NewAmountAndCurrency10 getAmountPrimary() {
    return amountPrimary;
  }

  public void setAmountPrimary(NewAmountAndCurrency10 amountPrimary) {
    this.amountPrimary = amountPrimary;
  }

  public NewAmountAndCurrency10 getAmountSecondary() {
    return amountSecondary;
  }

  public void setAmountSecondary(NewAmountAndCurrency10 amountSecondary) {
    this.amountSecondary = amountSecondary;
  }
}
