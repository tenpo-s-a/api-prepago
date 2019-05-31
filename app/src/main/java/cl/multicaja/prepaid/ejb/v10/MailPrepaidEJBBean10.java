package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;

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
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  public void setPrepaidTopupDelegate10(PrepaidTopupDelegate10 prepaidTopupDelegate10) {
    this.prepaidTopupDelegate10 = prepaidTopupDelegate10;
  }

  public PrepaidCardEJBBean11 getPrepaidCardEJBBean11() {
    return prepaidCardEJBBean11;
  }

  public void setPrepaidCardEJBBean11(PrepaidCardEJBBean11 prepaidCardEJBBean11) {
    this.prepaidCardEJBBean11 = prepaidCardEJBBean11;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  /*@Override
  public void sendMailAsync(Map<String, Object> headers, Long userId, EmailBody content) throws Exception {

    if (userId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    if (content == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "emailBody"));
    }
    if (content.getTemplate() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "emailBody.template"));
    }

    if(!TEMPLATE_MAIL_SEND_CARD.equalsIgnoreCase(content.getTemplate())) {
      log.info("Envio email flujo normal");
      getUserClient().sendMail(headers, userId, content);
    }
  }

   */

  /*@Override
  public void sendMailAsync(Map<String, Object> headers, EmailBody content) throws Exception {

    if (content == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "emailBody"));
    }
    if (content.getTemplate() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "emailBody.template"));
    }

    log.info("Envio email flujo normal");
    getUserClient().sendInternalMail(headers, content);
  }

  public void sendInternalEmail(String template, Map<String, Object> templateData) throws Exception {
    EmailBody emailBody = new EmailBody();
    emailBody.setTemplateData(templateData);
    emailBody.setTemplate(template);
    emailBody.setAddress("soporte-prepago@multicaja.cl");
    sendMailAsync(null, emailBody);
  }
   */
}
