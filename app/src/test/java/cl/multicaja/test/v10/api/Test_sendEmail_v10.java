package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.ejb.v10.MailPrepaidEJBBean10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.users.model.v10.EmailBody;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class Test_sendEmail_v10 extends TestBaseUnitApi {

  private HttpResponse sendEmailApi(EmailBody emailBody, Long userId) {
    final HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/mail", userId), toJson(emailBody));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void sendMailDefault() throws Exception {
    User user = registerUser();
    final String template = "Prepago/Activacion";
    final EmailBody email = getEmailBody(user, template);
    final HttpResponse httpResponse = sendEmailApi(email, user.getId());
    Assert.assertEquals("status 201", 201, httpResponse.getStatus());
  }

  @Test
  public void sendMailWithCard() throws Exception {
    User user = registerUser();
    user.getEmail().setValue(String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(15)).toLowerCase());
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    final EmailBody email = getEmailBody(user, MailPrepaidEJBBean10.SEND_CARD_TEMPLATE_NAME);
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
