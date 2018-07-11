package cl.multicaja.prepaid.mail.ejb.v10;

import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;
import cl.multicaja.users.model.v10.EmailBody;
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

  @Inject
  private PrepaidTopupDelegate10 prepaidTopupDelegate10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private MailEJBBean10 mailEJBBean10;

  public static final String SEND_CARD_TEMPLATE_NAME = "Tarjeta/Envio";

  @Override
  public String sendMailAsync(Map<String, Object> headers, Long userId, EmailBody content) throws Exception {
    if(SEND_CARD_TEMPLATE_NAME.equalsIgnoreCase(content.getTemplate())) {
      log.info("Envio email con tarjeta en PDF");
      PrepaidCard10 prepaidCard10 = prepaidCardEJBBean10.getLastPrepaidCardByUserIdAndStatus(headers, userId, PrepaidCardStatus.ACTIVE);
      return prepaidTopupDelegate10.sendPdfCardMail(prepaidCard10);
    }

    log.info("Envio email flujo normal");
    return this.mailEJBBean10.sendMailAsync(headers, userId, content);
  }

}
