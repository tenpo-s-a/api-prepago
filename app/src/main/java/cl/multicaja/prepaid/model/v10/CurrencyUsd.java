package cl.multicaja.prepaid.model.v10;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class CurrencyUsd implements Serializable {

  private Long id;
  private String fileName;
  private Timestamp creationDate;
  private Timestamp endDate;
  private Timestamp expirationUsdDate;
  private Double buyCurrencyConvertion;
  private Double sellCurrencyConvertion;
  private Double midCurrencyConvertion;
  private Integer currencyExponent;
  private Double dayCurrencyConvertion;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Timestamp getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Timestamp creationDate) {
    this.creationDate = creationDate;
  }

  public Timestamp getEndDate() {
    return endDate;
  }

  public void setEndDate(Timestamp endDate) {
    this.endDate = endDate;
  }

  public Timestamp getExpirationUsdDate() {
    return expirationUsdDate;
  }

  public void setExpirationUsdDate(Timestamp expirationUsdDate) {
    this.expirationUsdDate = expirationUsdDate;
  }

  public Double getBuyCurrencyConvertion() {
    return buyCurrencyConvertion;
  }

  public void setBuyCurrencyConvertion(Double buyCurrencyConvertion) {
    this.buyCurrencyConvertion = buyCurrencyConvertion;
  }

  public Double getSellCurrencyConvertion() {
    return sellCurrencyConvertion;
  }

  public void setSellCurrencyConvertion(Double sellCurrencyConvertion) {
    this.sellCurrencyConvertion = sellCurrencyConvertion;
  }

  public Double getMidCurrencyConvertion() {
    return midCurrencyConvertion;
  }

  public void setMidCurrencyConvertion(Double midCurrencyConvertion) {
    this.midCurrencyConvertion = midCurrencyConvertion;
  }

  public Integer getCurrencyExponent() {
    return currencyExponent;
  }

  public void setCurrencyExponent(Integer currencyExponent) {
    this.currencyExponent = currencyExponent;
  }

  public Double getDayCurrencyConvertion() {
    return dayCurrencyConvertion;
  }

  public void setDayCurrencyConvertion(Double dayCurrencyConvertion) {
    this.dayCurrencyConvertion = dayCurrencyConvertion;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
