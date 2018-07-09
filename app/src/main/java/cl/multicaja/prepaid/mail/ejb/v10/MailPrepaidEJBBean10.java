package cl.multicaja.prepaid.mail.ejb.v10;

import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;
import cl.multicaja.users.model.v10.EmailBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.util.Map;

/**
 * @author gosalass
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class MailPrepaidEJBBean10 implements MailPrepaidEJB10 {
  private static Log log = LogFactory.getLog(MailPrepaidEJBBean10.class);

  @EJB
  private MailEJBBean10 mailEJBBean10;

  @Override
  public String sendMailAsync(Map<String, Object> headers, Long userId, EmailBody content) throws Exception {
    return this.mailEJBBean10.sendMailAsync(headers, userId, content);
  }

}
