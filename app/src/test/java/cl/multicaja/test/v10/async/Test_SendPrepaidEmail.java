package cl.multicaja.test.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.users.async.v10.model.EmailDataAsync10;
import cl.multicaja.users.async.v10.routes.UsersEmailRoute10;
import cl.multicaja.users.model.v10.EmailBody;
import cl.multicaja.users.model.v10.SignUp;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

public class Test_SendPrepaidEmail extends TestBaseUnitAsync {

  private static final String from = "prueba@multicaja.cl";

  @Test
  public void sendEmailAsync_ok() throws Exception {

    SignUp signUp = getSignup();
    EmailBody content = newEmailBody(String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(10)), from, true);
    String messageId = getMailPrepaidWrappedEJBBean10().sendMailAsync(null, signUp.getUserId(), content);
    ExchangeData<EmailDataAsync10> resp = getMailPrepaidWrappedEJBBean10().getEmailDataAsync10(messageId);
    Assert.assertNotNull("debe existir una respuesta", resp);
    EmailDataAsync10 emailDataAsync10 = resp.getData();
    Assert.assertNotNull("Deberia existir datos de envio de email", emailDataAsync10);
    Assert.assertTrue("El email debe haber sido enviado", emailDataAsync10.isSent());
    Assert.assertFalse("No debe ser simulado", emailDataAsync10.isSimulated());
    Assert.assertNull("No debe tener attachment", emailDataAsync10.getContent().getAttachments());
  }

  @Test
  public void sendEmailAsync_ok_simulated() throws Exception {
    SignUp signUp = getSignup();
    EmailBody content = newEmailBody(getUniqueEmail(), from, true);
    String messageId = getMailPrepaidWrappedEJBBean10().sendMailAsync(null, signUp.getUserId(), content);
    ExchangeData<EmailDataAsync10> resp = getMailPrepaidWrappedEJBBean10().getEmailDataAsync10(messageId);
    Assert.assertNotNull("debe existir una respuesta", resp);
    EmailDataAsync10 emailDataAsync10 = resp.getData();
    Assert.assertNotNull("Deberia existir datos de envio de email", emailDataAsync10);
    Assert.assertTrue("El email debe haber sido enviado", emailDataAsync10.isSent());
    Assert.assertTrue("Debe ser simulado", emailDataAsync10.isSimulated());
    Assert.assertNull("No debe tener attachment", emailDataAsync10.getContent().getAttachments());
  }

  @Test
  public void sendEmailAsync_not_ok_with_error() throws Exception {
    SignUp signUp = getSignup();
    Long userId = signUp.getUserId();
    String messageId = String.format("%s#%s#%s", userId, RandomStringUtils.randomAlphabetic(5), Utils.uniqueCurrentTimeNano());
    {
      Queue qReq = camelFactory.createJMSQueue(UsersEmailRoute10.PENDING_SEND_EMAIL_REQ);
      //se crea un EmailBody con error en el formato de la direccion de email
      EmailBody content = newEmailBody("email sin formato", from, true);
      EmailDataAsync10 emailDataAsync10 = new EmailDataAsync10(null, userId, content);
      ExchangeData req = new ExchangeData<>(emailDataAsync10);
      //se envia directamente al proceso asincrono para probocar un error de forma interna y gatillar el reintento
      camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));
    }

    Thread.sleep(50000);

    {
      //se busca el mensaje en la cola de emails con error, debe exitir
      Queue qResp = camelFactory.createJMSQueue(UsersEmailRoute10.ERROR_PENDING_SEND_EMAIL_RESP);
      ExchangeData<EmailDataAsync10> resp = (ExchangeData) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
      Assert.assertNotNull("Deberia existir datos de envio de email en la cola de emails con error", resp);
      EmailDataAsync10 emailDataAsync10 = resp.getData();
      Assert.assertNotNull("Deberia existir datos de envio de email", emailDataAsync10);
      Assert.assertFalse("El email no debe haber sido enviado", emailDataAsync10.isSent());
      Assert.assertNull("No debe tener attachment", emailDataAsync10.getContent().getAttachments());
    }
  }

}
