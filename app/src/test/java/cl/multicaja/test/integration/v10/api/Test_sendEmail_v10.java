package cl.multicaja.test.integration.v10.api;

import org.junit.Ignore;
import org.junit.Test;

public class Test_sendEmail_v10 extends TestBaseUnitApi {

  /*private HttpResponse sendEmailApi(EmailBody emailBody, Long userId) {
    final HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/mail", userId), toJson(emailBody));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }
   */

  //TODO: Revisar cuando se implemente el envio de mail
  @Ignore
  @Test
  public void sendMailDefault() throws Exception {
    /*final String template = "Users/EmailValidation";
    final EmailBody email = getEmailBody(user, template);
    final HttpResponse httpResponse = sendEmailApi(email, user.getId());
    Assert.assertEquals("status 201", 201, httpResponse.getStatus());

     */
  }

  /*private EmailBody getEmailBody(User user, String template) {
    EmailBody mail = new EmailBody();
    mail.setTemplate(template);
    mail.setAddress(user.getEmail().getValue());
    Map<String,Object> mailData = new HashMap<>();
    mailData.put("name",user.getName());
    mailData.put("lastName",user.getLastname_1());
    mail.setTemplateData(mailData);
    return mail;
  }
   */

}
