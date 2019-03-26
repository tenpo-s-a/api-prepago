package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_SEND_CARD;

public class Test_sendEmail_v10 extends TestBaseUnitApi {

  private HttpResponse sendEmailApi(EmailBody emailBody, Long userId) {
    final HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/mail", userId), toJson(emailBody));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void sendMailDefault() throws Exception {
    User user = registerUser();
    final String template = "Users/EmailValidation";
    final EmailBody email = getEmailBody(user, template);
    final HttpResponse httpResponse = sendEmailApi(email, user.getId());
    Assert.assertEquals("status 201", 201, httpResponse.getStatus());
  }

  private EmailBody getEmailBody(User user, String template) {
    EmailBody mail = new EmailBody();
    mail.setTemplate(template);
    mail.setAddress(user.getEmail().getValue());
    Map<String,Object> mailData = new HashMap<>();
    mailData.put("name",user.getName());
    mailData.put("lastName",user.getLastname_1());
    mail.setTemplateData(mailData);
    return mail;
  }

}
