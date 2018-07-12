package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;
import cl.multicaja.users.model.v10.EmailBody;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.util.Map;

/**
 * @author gosalass
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class MailPrepaidEJBBean10 implements MailPrepaidEJB10 {

  private static Log log = LogFactory.getLog(MailPrepaidEJBBean10.class);
  public static final String SEND_CARD_TEMPLATE_NAME = "Prepago/EnvioTarjeta";

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


  @Override
  public String sendMailAsync(Map<String, Object> headers, Long userId, EmailBody content) throws Exception {

    if(SEND_CARD_TEMPLATE_NAME.equalsIgnoreCase(content.getTemplate())) {
      log.info("Flujo Envio email con tarjeta en PDF");
      PrepaidUser10 prepaidUser10 = prepaidUserEJBBean10.getPrepaidUserByUserIdMc(headers, userId);
      PrepaidCard10 prepaidCard10 = prepaidCardEJBBean10.getLastPrepaidCardByUserIdAndStatus(headers, prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);
      if(prepaidCard10 != null){
        User user = usersEJBBean10.getUserById(headers, userId);
        return prepaidTopupDelegate10.sendPdfCardMail(prepaidCard10, user);
      } else {
        throw new ValidationException(Errors.TARJETA_NO_EXISTE);
      }
    }

    log.info("Envio email flujo normal");
    return this.mailEJBBean10.sendMailAsync(headers, userId, content);
  }

}
