package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.ejb.v10.MailEJBBean10;
import cl.multicaja.users.model.v10.EmailBody;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ejb.*;
import javax.inject.Inject;
import java.util.Map;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_SEND_CARD;

/**
 * @author gosalass
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class MailPrepaidEJBBean10 extends PrepaidBaseEJBBean10 implements MailPrepaidEJB10 {

  private static Log log = LogFactory.getLog(MailPrepaidEJBBean10.class);

  @Inject
  private PrepaidTopupDelegate10 prepaidTopupDelegate10;
  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;
  @EJB
  private MailEJBBean10 mailEJBBean10;
  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;
  @EJB
  private UsersEJBBean10 usersEJBBean10;

  public void setPrepaidTopupDelegate10(PrepaidTopupDelegate10 prepaidTopupDelegate10) {
    this.prepaidTopupDelegate10 = prepaidTopupDelegate10;
  }

  public void setPrepaidCardEJBBean10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
  }

  public void setMailEJBBean10(MailEJBBean10 mailEJBBean10) {
    this.mailEJBBean10 = mailEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  public void setUsersEJBBean10(UsersEJBBean10 usersEJBBean10) {
    this.usersEJBBean10 = usersEJBBean10;
  }

  @Override
  public String sendMailAsync(Map<String, Object> headers, Long userId, EmailBody content) throws Exception {

    if (userId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    if (content == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "emailBody"));
    }
    if (content.getTemplate() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "emailBody.template"));
    }

    if(TEMPLATE_MAIL_SEND_CARD.equalsIgnoreCase(content.getTemplate())) {
      log.info("Flujo Envio email con tarjeta en PDF");
      return sendCardAsync(headers,userId);
    }
    else {
      log.info("Envio email flujo normal");
      return this.mailEJBBean10.sendMailAsync(headers, userId, content);
    }

  }

  private String sendCardAsync( Map<String, Object> headers,Long userId) throws Exception {

    User user = this.usersEJBBean10.getUserById(headers, userId);
    if(user == null) {
      throw new ValidationException(Errors.CLIENTE_NO_EXISTE);
    }
    if(!user.getGlobalStatus().equals(UserStatus.ENABLED)){
      throw new ValidationException(Errors.CLIENTE_BLOQUEADO_O_BORRADO);
    }

    PrepaidUser10 prepaidUser10 = prepaidUserEJBBean10.getPrepaidUserByUserIdMc(headers, userId);
    if(prepaidUser10 == null) {
      throw new ValidationException(Errors.CLIENTE_NO_TIENE_PREPAGO);
    }

    if(prepaidUser10.getStatus() == PrepaidUserStatus.DISABLED){
      throw new  ValidationException(Errors.CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }

    PrepaidCard10 prepaidCard10 = prepaidCardEJBBean10.getLastPrepaidCardByUserId(headers, prepaidUser10.getId());
    if(prepaidCard10 == null){
      throw new ValidationException(Errors.TARJETA_NO_EXISTE);
    }
    if(!prepaidCard10.getStatus().equals(PrepaidCardStatus.ACTIVE)) {

      if(prepaidCard10.getStatus().equals(PrepaidCardStatus.LOCKED)) {
        throw new ValidationException(Errors.TARJETA_CON_BLOQUEO_TEMPORAL);
      }
      else if(prepaidCard10.getStatus().equals(PrepaidCardStatus.LOCKED_HARD)) {
        throw new ValidationException(Errors.TARJETA_BLOQUEADA_DE_FORMA_DEFINITIVA);
      }
      else if(prepaidCard10.getStatus().equals(PrepaidCardStatus.EXPIRED)) {
        throw new ValidationException(Errors.TARJETA_EXPIRADA);
      }
      else {
        throw new ValidationException(Errors.TARJETA_ERROR_GENERICO_$VALUE);
      }
    }
    return prepaidTopupDelegate10.sendPdfCardMail(prepaidCard10, user);
  }
}
