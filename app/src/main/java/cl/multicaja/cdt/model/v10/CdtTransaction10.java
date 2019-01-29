package cl.multicaja.cdt.model.v10;

import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;


public class CdtTransaction10 implements Serializable {

  private Long id;
  private String accountId;
  private CdtTransactionType transactionType;
  private Long transactionReference;
  private String externalTransactionId;
  private String gloss;
  private BigDecimal amount;
  private Boolean indSimulacion;
  private String numError = "0";
  private String msjError = "";

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public CdtTransactionType getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(CdtTransactionType transactionType) {
    this.transactionType = transactionType;
  }

  public Long getTransactionReference() {
    return transactionReference;
  }

  public void setTransactionReference(Long transactionReference) {
    this.transactionReference = transactionReference;
  }


  public String getExternalTransactionId() {
    return externalTransactionId;
  }

  public void setExternalTransactionId(String externalTransactionId) {
    this.externalTransactionId = externalTransactionId;
  }

  public String getGloss() {
    return gloss;
  }

  public void setGloss(String gloss) {
    this.gloss = gloss;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getNumError() {
    return numError;
  }

  public boolean isNumErrorOk() {
    return "0".equals(this.getNumError());
  }

  public int getNumErrorInt() {
    return NumberUtils.getInstance().toInt(this.getNumError(),-1);
  }

  public void setNumError(String numError) {
    this.numError = numError;
  }

  public String getMsjError() {
    return msjError;
  }

  public void setMsjError(String msjError) {
    this.msjError = msjError;
  }

  public Boolean getIndSimulacion() {
    return indSimulacion;
  }

  public void setIndSimulacion(Boolean indSimulacion) {
    this.indSimulacion = indSimulacion;
  }

  public CdtTransactionType getCdtTransactionTypeConfirm() {
    if(CdtTransactionType.PRIMERA_CARGA.equals(this.getTransactionType())){
      return CdtTransactionType.PRIMERA_CARGA_CONF;
    } else if (CdtTransactionType.CARGA_WEB.equals(this.getTransactionType())){
      return CdtTransactionType.CARGA_WEB_CONF;
    } else if (CdtTransactionType.CARGA_POS.equals(this.getTransactionType())) {
      return CdtTransactionType.CARGA_POS_CONF;
    }else if(CdtTransactionType.RETIRO_WEB.equals(this.getTransactionType())) {
      return CdtTransactionType.RETIRO_WEB_CONF;
    }else if (CdtTransactionType.RETIRO_POS.equals(this.getTransactionType())) {
      return CdtTransactionType.RETIRO_POS_CONF;
    } else if (CdtTransactionType.REVERSA_CARGA.equals(this.getTransactionType())) {
      return CdtTransactionType.REVERSA_CARGA_CONF;
    } else if (CdtTransactionType.REVERSA_RETIRO.equals(this.getTransactionType())) {
      return CdtTransactionType.REVERSA_RETIRO_CONF;
    } else {
      return CdtTransactionType.REVERSA_PRIMERA_CARGA_CONF;
    }
  }

  public CdtTransactionType getCdtTransactionTypeReverse() {
    if(CdtTransactionType.PRIMERA_CARGA.equals(this.getTransactionType())){
      return CdtTransactionType.REVERSA_PRIMERA_CARGA;
    } else if (CdtTransactionType.CARGA_WEB.equals(this.getTransactionType())){
      return CdtTransactionType.REVERSA_CARGA;
    } else if (CdtTransactionType.CARGA_POS.equals(this.getTransactionType())) {
      return CdtTransactionType.REVERSA_CARGA;
    }else if(CdtTransactionType.RETIRO_WEB.equals(this.getTransactionType())) {
      return CdtTransactionType.REVERSA_RETIRO;
    }else if (CdtTransactionType.RETIRO_POS.equals(this.getTransactionType())) {
      return CdtTransactionType.REVERSA_RETIRO;
    } else if (CdtTransactionType.REVERSA_CARGA.equals(this.getTransactionType())) {
      return null;
    } else if (CdtTransactionType.REVERSA_RETIRO.equals(this.getTransactionType())) {
      return null;
    } else {
      return null;
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}

