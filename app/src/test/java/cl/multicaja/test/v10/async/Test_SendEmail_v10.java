package cl.multicaja.test.v10.async;

import cl.multicaja.core.utils.Utils;
import cl.multicaja.users.model.v10.EmailBody;
import org.junit.Ignore;

/**
 * @autor vutreras
 */
@Ignore
public class Test_SendEmail_v10 extends TestBaseUnitAsync {

  /*
  public static EmailBody newEmailBody(String address) {
    long id = Utils.uniqueCurrentTimeMillis();
    EmailBody content = new EmailBody();
    content.setAddress(address);
    content.setBody("Prueba de email: " + id);
    content.setFrom("info@multicaja.cl");
    content.setSubject("Prueba: " + id);
    return content;
  }

  @Test
  public void sendEmailAsync() throws Exception {

    SignUp signUp = getSignup();

    EmailBody content = newEmailBody(String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(10)));

    String messageId = getMailEJBBean10().sendMailAsync(null, signUp.getUserIdMc(), content);

    ExchangeData<EmailDataAsync10> resp = getMailEJBBean10().getEmailDataAsync10(messageId);

    Assert.assertNotNull("debe existir una respuesta", resp);

    EmailDataAsync10 emailDataAsync10 = resp.getData();

    Assert.assertNotNull("Deberia existir datos de envio de email", emailDataAsync10);
    Assert.assertTrue("El email debe haber sido enviado", emailDataAsync10.isSent());
    Assert.assertFalse("No debe ser simulado", emailDataAsync10.isSimulated());
  }

  @Test
  public void sendEmailAsync_simulated() throws Exception {

    SignUp signUp = getSignup();

    EmailBody content = newEmailBody(getUniqueEmail());

    String messageId = getMailEJBBean10().sendMailAsync(null, signUp.getUserIdMc(), content);

    ExchangeData<EmailDataAsync10> resp = getMailEJBBean10().getEmailDataAsync10(messageId);

    Assert.assertNotNull("debe existir una respuesta", resp);

    EmailDataAsync10 emailDataAsync10 = resp.getData();

    Assert.assertNotNull("Deberia existir datos de envio de email", emailDataAsync10);
    Assert.assertTrue("El email debe haber sido enviado", emailDataAsync10.isSent());
    Assert.assertTrue("Debe ser simulado", emailDataAsync10.isSimulated());
  }
  */
}
