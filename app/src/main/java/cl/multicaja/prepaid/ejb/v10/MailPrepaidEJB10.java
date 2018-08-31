package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.helpers.users.model.EmailBody;

import java.util.Map;

/**
 * @author gosalass
 */
public interface MailPrepaidEJB10 {

  /**
   * Envia un email de forma asincrona
   *
   * @param headers
   * @param content
   * @return id del mensaje enviado de forma asincrona, se puede usar para buscar usando el metodo: getEmailDataAsync10
   * @throws Exception
   */
  void sendMailAsync(Map<String, Object> headers, Long userId, EmailBody content) throws Exception;

}
