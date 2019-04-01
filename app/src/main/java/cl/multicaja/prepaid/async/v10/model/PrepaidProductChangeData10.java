package cl.multicaja.prepaid.async.v10.model;

import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.TipoAlta;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author abarazarte
 **/
public class PrepaidProductChangeData10 implements Serializable {

  private PrepaidUser10 prepaidUser;
  private PrepaidCard10 prepaidCard;
  private TipoAlta tipoAlta;

  private Errors numError;
  private String msjError;

  public PrepaidProductChangeData10() {
    super();
  }

  public PrepaidProductChangeData10(PrepaidUser10 user, PrepaidCard10 prepaidCard, TipoAlta tipoAlta) {
    this.prepaidUser = user;
    this.prepaidCard = prepaidCard;
    this.tipoAlta = tipoAlta;
  }

  public PrepaidUser10 getPrepaidUser() {
    return prepaidUser;
  }

  public void setPrepaidUser(PrepaidUser10 user) {
    this.prepaidUser = user;
  }

  public PrepaidCard10 getPrepaidCard() {
    return prepaidCard;
  }

  public void setPrepaidCard(PrepaidCard10 prepaidCard) {
    this.prepaidCard = prepaidCard;
  }

  public TipoAlta getTipoAlta() {
    return tipoAlta;
  }

  public void setTipoAlta(TipoAlta tipoAlta) {
    this.tipoAlta = tipoAlta;
  }

  public Errors getNumError() {
    return numError;
  }

  public void setNumError(Errors numError) {
    this.numError = numError;
  }

  public String getMsjError() {
    return msjError;
  }

  public void setMsjError(String msjError) {
    this.msjError = msjError;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
