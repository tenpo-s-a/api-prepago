package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

import java.math.BigDecimal;
import java.util.Date;

public class PrepaidTransaction10 extends BaseModel {

  private Date date;
  private BigDecimal exchangeRate;
  private String commerceCode;
  private Integer economicConcept1;
  private String descEconomicConcept1;
  private String amountDescriptionType1;
  private BigDecimal applicationAmount1;
  private BigDecimal grossValue1;
  private Integer economicConcept2;
  private String descEconomicConcept2;
  private String amountDescriptionType2;
  private BigDecimal applicationAmount2;
  private BigDecimal grossValue2;
  private String invoiceDescription;
  private Integer extractAccount;
  private Integer extractTransaction;
  private Integer invoiceType;
  private NewAmountAndCurrency10 amountPrimary;
  private NewAmountAndCurrency10 amountSecondary;

  private String description;
  private String operation;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public String getCommerceCode() {
    return commerceCode;
  }

  public void setCommerceCode(String commerceCode) {
    this.commerceCode = commerceCode;
  }

  public Integer getEconomicConcept1() {
    return economicConcept1;
  }

  public void setEconomicConcept1(Integer economicConcept1) {
    this.economicConcept1 = economicConcept1;
  }

  public String getDescEconomicConcept1() {
    return descEconomicConcept1;
  }

  public void setDescEconomicConcept1(String descEconomicConcept1) {
    this.descEconomicConcept1 = descEconomicConcept1;
  }

  public String getAmountDescriptionType1() {
    return amountDescriptionType1;
  }

  public void setAmountDescriptionType1(String amountDescriptionType1) {
    this.amountDescriptionType1 = amountDescriptionType1;
  }

  public BigDecimal getApplicationAmount1() {
    return applicationAmount1;
  }

  public void setApplicationAmount1(BigDecimal applicationAmount1) {
    this.applicationAmount1 = applicationAmount1;
  }

  public BigDecimal getGrossValue1() {
    return grossValue1;
  }

  public void setGrossValue1(BigDecimal grossValue1) {
    this.grossValue1 = grossValue1;
  }

  public Integer getEconomicConcept2() {
    return economicConcept2;
  }

  public void setEconomicConcept2(Integer economicConcept2) {
    this.economicConcept2 = economicConcept2;
  }

  public String getDescEconomicConcept2() {
    return descEconomicConcept2;
  }

  public void setDescEconomicConcept2(String descEconomicConcept2) {
    this.descEconomicConcept2 = descEconomicConcept2;
  }

  public String getAmountDescriptionType2() {
    return amountDescriptionType2;
  }

  public void setAmountDescriptionType2(String amountDescriptionType2) {
    this.amountDescriptionType2 = amountDescriptionType2;
  }

  public BigDecimal getApplicationAmount2() {
    return applicationAmount2;
  }

  public void setApplicationAmount2(BigDecimal applicationAmount2) {
    this.applicationAmount2 = applicationAmount2;
  }

  public BigDecimal getGrossValue2() {
    return grossValue2;
  }

  public void setGrossValue2(BigDecimal grossValue2) {
    this.grossValue2 = grossValue2;
  }

  public String getInvoiceDescription() {
    return invoiceDescription;
  }

  public void setInvoiceDescription(String invoiceDescription) {
    this.invoiceDescription = invoiceDescription;
  }

  public Integer getExtractAccount() {
    return extractAccount;
  }

  public void setExtractAccount(Integer extractAccount) {
    this.extractAccount = extractAccount;
  }

  public Integer getExtractTransaction() {
    return extractTransaction;
  }

  public void setExtractTransaction(Integer extractTransaction) {
    this.extractTransaction = extractTransaction;
  }

  public Integer getInvoiceType() {
    return invoiceType;
  }

  public void setInvoiceType(Integer invoiceType) {
    this.invoiceType = invoiceType;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }
}
