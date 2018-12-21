package cl.multicaja.accounting.helpers.mastercard.model;


import cl.multicaja.core.model.BaseModel;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IpmMessage extends BaseModel {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
  private static final ZoneId zoneId = ZoneId.of("America/Santiago");

  public static final String HEADER_MESSAGE_TYPE = "1644-697";
  public static final String TRAILER_MESSAGE_TYPE = "1644-695";
  public static final String TRANSACTION_MESSAGE_TYPE = "1240-200";

  private String[] data;

  /**
   * MTI Message Type Identifier
   */
  private String mti;

  /**
   * DE24 Function Code
   */
  private String functionCode;

  /**
   * DE25 Message reason code
   */
  private Integer messageReasonCode;

  /**
   * DE71 Message number
   */
  private Integer messageNumber;

  /**
   * DE2 Pan
   */
  private String pan;

  /**
   * DE4 Transaction amount
   */
  private String transactionAmount;

  /**
   * DE5 Reconciliation amount
   */
  private String reconciliationAmount;

  /**
   * DE6 Cardholder billing amount
   */
  private String cardholderBillingAmount;

  /**
   * DE9 Reconciliation conversion rate
   */
  private String reconciliationConversionRate;

  /**
   * DE10 Cardholder billing conversion rate
   */
  private String cardholderBillingConversionRate;

  /**
   * DE12 Transaction local date time
   */
  private String transactionLocalDate;

  /**
   * DE38 Approval code
   */
  private Integer approvalCode;

  /**
   *  DE49 Transaction currency code
   */
  private Integer transactionCurrencyCode;

  /**
   * DE50 Reconciliation currency code
   */
  private Integer reconciliationCurrencyCode;

  /**
   * DE51 Cardholder billing currency code
   */
  private Integer cardholderBillingCurrencyCode;

  /**
   * PDS0148 Currency exponents
   */
  private String currencyExponents;

  /**
   * DE43 Merchat info
   */
  private String merchantInfo;

  /**
   * DE43 - Merchant name
   */
  private String merchantName;

  /**
   * DE43 - Merchant suburb
   */
  private String merchantSuburb;

  /**
   * DE43 - Merchant postal code
   */
  private String merchantPostalCode;

  /**
   * DE43 - Merchant country
   */
  private String merchantCountry;

  /**
   * PDS0306 - Message count
   */
  private Integer messageCount;

  /**
   * PDS0105 - File Id
   */
  private String fileId;

  public IpmMessage() {
    super();
  }

  public IpmMessage(String[] data) {
    super();
    this.data = data;
  }

  public String getMti() {
    if(mti == null) {
      mti = data[1];
    }
    return mti;
  }

  public String getFunctionCode() {
    if(functionCode == null) {
      functionCode = data[2];
    }
    return functionCode;
  }

  public Integer getMessageReasonCode() {
    return messageReasonCode;
  }

  public Integer getMessageNumber() {
    if(messageNumber == null) {
      messageNumber = NumberUtils.getInstance().toInteger(data[0], null);
    }
    return messageNumber;
  }

  public String getPan() {
    if(pan == null) {
      pan = data[6];
    }
    return pan;
  }

  public String getTransactionAmount() {
    if(transactionAmount == null) {
      transactionAmount = data[8];
    }
    return transactionAmount;
  }

  public String getReconciliationAmount() {
    if(reconciliationAmount == null) {
      reconciliationAmount = data[9];
    }
    return reconciliationAmount;
  }

  public String getCardholderBillingAmount() {
    if(cardholderBillingAmount == null) {
      cardholderBillingAmount = data[10];
    }
    return cardholderBillingAmount;
  }

  public BigDecimal getReconciliationConversionRate() {
    if(reconciliationConversionRate == null) {
      reconciliationConversionRate = data[11];
    }

    return getBigDecimalConversionRate(reconciliationConversionRate);
  }

  public BigDecimal getCardholderBillingConversionRate() {
    if(cardholderBillingConversionRate == null) {
      cardholderBillingConversionRate = data[12];
    }
    return getBigDecimalConversionRate(cardholderBillingConversionRate);
  }

  public ZonedDateTime getTransactionLocalDate() {
    if(transactionLocalDate == null) {
      transactionLocalDate = data[13];
    }
    if(!StringUtils.isAllBlank(transactionLocalDate)) {
      LocalDateTime ldt = LocalDateTime.parse(transactionLocalDate, formatter);
      return ZonedDateTime.of(ldt, zoneId);
    }
    return null;
  }

  public Integer getApprovalCode() {
    if(approvalCode == null) {
      approvalCode = NumberUtils.getInstance().toInteger(data[14]);
    }
    return approvalCode;
  }

  public Integer getTransactionCurrencyCode() {
    if(transactionCurrencyCode == null) {
      transactionCurrencyCode = NumberUtils.getInstance().toInteger(data[15]);
    }
    return transactionCurrencyCode;
  }

  public Integer getReconciliationCurrencyCode() {
    if(reconciliationCurrencyCode == null) {
      reconciliationCurrencyCode = NumberUtils.getInstance().toInteger(data[16]);
    }
    return reconciliationCurrencyCode;
  }

  public Integer getCardholderBillingCurrencyCode() {
    if(cardholderBillingCurrencyCode == null) {
      cardholderBillingCurrencyCode = NumberUtils.getInstance().toInteger(data[17]);
    }
    return cardholderBillingCurrencyCode;
  }

  public String getCurrencyExponents() {
    if(currencyExponents == null) {
      currencyExponents = data[18];
    }
    return currencyExponents;
  }

  public String getMerchantName() {
    if(merchantName == null) {
      merchantName = data[19];
    }
    return merchantName;
  }

  public String getMerchantSuburb() {
    if(merchantSuburb == null) {
      merchantSuburb = data[20];
    }
    return merchantSuburb;
  }

  public String getMerchantPostalCode() {
    if(merchantPostalCode == null) {
      merchantPostalCode = data[21];
    }
    return merchantPostalCode;
  }

  public String getMerchantCountry() {
    if(merchantCountry == null) {
      merchantCountry = data[22];
    }
    return merchantCountry;
  }

  public String getMerchantInfo() {
    //23
    return merchantInfo;
  }

  public Integer getMessageCount() {
    if(this.messageCount == null) {
      this.messageCount = NumberUtils.getInstance().toInteger(this.data[5], null);
    }
    return this.messageCount;
  }

  public String getFileId() {
    if(this.fileId == null) {
      this.fileId = this.data[4];
    }
    return this.fileId;
  }

  /**
   * MTI -> 1644
   * Function Code -> 697
   * @return
   */
  public Boolean isHeader() {
    return "1644".equals(this.getMti()) && "697".equals(this.getFunctionCode());
  }

  /**
   * MTI -> 1644
   * Function Code -> 695
   * @return
   */
  public Boolean isTrailer() {
    return "1644".equals(this.getMti()) && "695".equals(this.getFunctionCode());
  }

  /**
   * MTI -> 1240
   * Function Code -> 200
   * @return
   */
  public Boolean isTransaction() {
    return "1240".equals(this.getMti()) && "200".equals(this.getFunctionCode());
  }

  private BigDecimal getBigDecimalConversionRate(String conversionRate) {
    if(conversionRate == null) {
      return null;
    }

    Integer decimalPositions = NumberUtils.getInstance().toInteger(conversionRate.substring(0, 1));
    Long value = NumberUtils.getInstance().toLong(conversionRate.substring(1));

    return movePeriod(value, decimalPositions);
  }

  public static BigDecimal movePeriod(Long value, Integer positions) {
    return BigDecimal.valueOf(value).movePointLeft(positions);
  }


  public void setData(String[] data) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.data = data;
  }

  public void setMti(String mti) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.mti = mti;
  }

  public void setFunctionCode(String functionCode) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.functionCode = functionCode;
  }

  public void setMessageReasonCode(Integer messageReasonCode) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.messageReasonCode = messageReasonCode;
  }

  public void setMessageNumber(Integer messageNumber) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.messageNumber = messageNumber;
  }

  public void setPan(String pan) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.pan = pan;
  }

  public void setTransactionAmount(String transactionAmount) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.transactionAmount = transactionAmount;
  }

  public void setReconciliationAmount(String reconciliationAmount) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.reconciliationAmount = reconciliationAmount;
  }

  public void setCardholderBillingAmount(String cardholderBillingAmount) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.cardholderBillingAmount = cardholderBillingAmount;
  }

  public void setReconciliationConversionRate(String reconciliationConversionRate) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.reconciliationConversionRate = reconciliationConversionRate;
  }

  public void setCardholderBillingConversionRate(String cardholderBillingConversionRate) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.cardholderBillingConversionRate = cardholderBillingConversionRate;
  }

  public void setTransactionLocalDate(String transactionLocalDate) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.transactionLocalDate = transactionLocalDate;
  }

  public void setApprovalCode(Integer approvalCode) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.approvalCode = approvalCode;
  }

  public void setTransactionCurrencyCode(Integer transactionCurrencyCode) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.transactionCurrencyCode = transactionCurrencyCode;
  }

  public void setReconciliationCurrencyCode(Integer reconciliationCurrencyCode) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.reconciliationCurrencyCode = reconciliationCurrencyCode;
  }

  public void setCardholderBillingCurrencyCode(Integer cardholderBillingCurrencyCode) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.cardholderBillingCurrencyCode = cardholderBillingCurrencyCode;
  }

  public void setCurrencyExponents(String currencyExponents) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.currencyExponents = currencyExponents;
  }

  public void setMerchantInfo(String merchantInfo) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.merchantInfo = merchantInfo;
  }

  public void setMerchantName(String merchantName) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.merchantName = merchantName;
  }

  public void setMerchantSuburb(String merchantSuburb) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.merchantSuburb = merchantSuburb;
  }

  public void setMerchantPostalCode(String merchantPostalCode) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.merchantPostalCode = merchantPostalCode;
  }

  public void setMerchantCountry(String merchantCountry) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.merchantCountry = merchantCountry;
  }

  public void setMessageCount(Integer messageCount) {
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.messageCount = messageCount;
  }

  public void setFileId(String fileId){
    if(!ConfigUtils.isEnvCI() && !ConfigUtils.isEnvTest()) {
      throw new SecurityException("Only available in tests");
    }
    this.fileId = fileId;
  }
}
