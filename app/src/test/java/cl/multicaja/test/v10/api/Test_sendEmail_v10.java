package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.users.model.v10.EmailBody;
import cl.multicaja.users.model.v10.User;
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
  public void sendMail() throws Exception {
    final User user = registerUser();
    EmailBody mail = new EmailBody();
    mail.setTemplate("Prepago/Activacion");
    mail.setAddress("codigo."+user.getEmail().getValue());
    Map<String,Object> mailData = new HashMap<>();
    mailData.put("name",user.getName());
    mailData.put("lastName",user.getLastname_1());
    mail.setTemplateData(mailData);
    final HttpResponse httpResponse = sendEmailApi(mail, user.getId());
    Assert.assertEquals("status 201", 201, httpResponse.getStatus());
  }

}
