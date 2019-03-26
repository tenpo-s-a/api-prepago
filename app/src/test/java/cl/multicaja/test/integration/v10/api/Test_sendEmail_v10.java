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

  /*//FIXME: Eliminacion de email tarjeta
  @Test
  public void sendMailWithCard() throws Exception {
    User user = registerUser();
    user.getEmail().setValue(String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(15)).toLowerCase());
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    final EmailBody email = getEmailBody(user, TEMPLATE_MAIL_SEND_CARD);
    final HttpResponse httpResponse = sendEmailApi(email, user.getId());
    Assert.assertEquals("status 201", 201, httpResponse.getStatus());
  }*/

  /*//FIXME: Eliminacion de email tarjeta
  @Test
  public void sendMailWithCardErrors() throws Exception {

    { // NO EXISTE USUARIO
      final EmailBody email = new EmailBody();
      email.setTemplate(TEMPLATE_MAIL_SEND_CARD);
      final HttpResponse httpResponse = sendEmailApi(email,1L);
      Assert.assertEquals("Error al llamar 422",422, httpResponse.getStatus());
      System.out.println("No existe usuario");
    }

    { // NO EXISTE USUARIO PREPAGO
      User user = registerUser();
      user.getEmail().setValue(String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(15)).toLowerCase());
      updateUser(user);
      final EmailBody email = getEmailBody(user, TEMPLATE_MAIL_SEND_CARD);
      final HttpResponse httpResponse = sendEmailApi(email, user.getId());
      Assert.assertEquals("Error al llamar 422",422,httpResponse.getStatus());
      System.out.println("No existe usuario Prepago");
    }

    {
      User user = registerUser();
      user.getEmail().setValue(String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(15)).toLowerCase());
      updateUser(user);
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      final EmailBody email = getEmailBody(user, TEMPLATE_MAIL_SEND_CARD);
      final HttpResponse httpResponse = sendEmailApi(email, user.getId());
      Assert.assertEquals("Error al llamar 422",422,httpResponse.getStatus());
      System.out.println("Tarjeta no existe");
    }

    {
      User user = registerUser();
      user.getEmail().setValue(String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(15)).toLowerCase());
      user.setGlobalStatus(UserStatus.LOCKED);
      updateUser(user);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      final EmailBody email = getEmailBody(user, TEMPLATE_MAIL_SEND_CARD);
      final HttpResponse httpResponse = sendEmailApi(email, user.getId());
      Assert.assertEquals("Error al llamar 422",422,httpResponse.getStatus());
      System.out.println("Tarjeta no existe");
    }
  }*/

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
