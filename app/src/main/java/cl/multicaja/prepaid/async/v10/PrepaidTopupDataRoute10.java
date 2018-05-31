package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @autor vutreras
 */
public class PrepaidTopupDataRoute10 implements Serializable {

  private PrepaidTopup10 prepaidTopup10;
  private User user;
  private PrepaidUser10 prepaidUser10;
  private PrepaidCard10 prepaidCard10;
  private CdtTransaction10 cdtTransaction10;
  private PrepaidMovement10 prepaidMovement10;
  private PrepaidMovement10 issuanceFeeMovement10;
  private List<ProcessorMetadata> processorMetadata = new ArrayList<>();

  public PrepaidTopupDataRoute10() {
    super();
  }

  public PrepaidTopupDataRoute10(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {
    this.prepaidTopup10 = prepaidTopup;
    this.user = user;
    this.cdtTransaction10 = cdtTransaction;
    this.prepaidMovement10 = prepaidMovement;
  }

  public PrepaidTopup10 getPrepaidTopup10() {
    return prepaidTopup10;
  }

  public void setPrepaidTopup10(PrepaidTopup10 prepaidTopup10) {
    this.prepaidTopup10 = prepaidTopup10;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public PrepaidUser10 getPrepaidUser10() {
    return prepaidUser10;
  }

  public void setPrepaidUser10(PrepaidUser10 prepaidUser10) {
    this.prepaidUser10 = prepaidUser10;
  }

  public PrepaidCard10 getPrepaidCard10() {
    return prepaidCard10;
  }

  public void setPrepaidCard10(PrepaidCard10 prepaidCard10) {
    this.prepaidCard10 = prepaidCard10;
  }

  public CdtTransaction10 getCdtTransaction10() {
    return cdtTransaction10;
  }

  public void setCdtTransaction10(CdtTransaction10 cdtTransaction10) {
    this.cdtTransaction10 = cdtTransaction10;
  }

  public PrepaidMovement10 getPrepaidMovement10() {
    return prepaidMovement10;
  }

  public void setPrepaidMovement10(PrepaidMovement10 prepaidMovement10) {
    this.prepaidMovement10 = prepaidMovement10;
  }

  public PrepaidMovement10 getIssuanceFeeMovement10() {
    return issuanceFeeMovement10;
  }

  public void setIssuanceFeeMovement10(PrepaidMovement10 issuanceFeeMovement10) {
    this.issuanceFeeMovement10 = issuanceFeeMovement10;
  }
  public List<ProcessorMetadata> getProcessorMetadata() {
    return processorMetadata;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
