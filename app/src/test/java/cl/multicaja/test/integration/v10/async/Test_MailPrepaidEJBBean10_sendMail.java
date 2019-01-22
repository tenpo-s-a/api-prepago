package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_SEND_CARD;

public class Test_MailPrepaidEJBBean10_sendMail extends TestBaseUnitAsync {

  @Test
  public void sendMailOk() throws Exception {
    User user = registerUser();
    updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser);
    createPrepaidCard10(prepaidCard10);

    EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD,user.getEmail().getValue());
    getMailPrepaidEJBBean10().sendMailAsync(null,user.getId(),emailBody);
    //Assert.assertNotNull("Resp no null", resp);
  }

  @Test
  public void sendMailError() throws Exception {
    try { // USUARIO NO EXISTE
      EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD, "");
      getMailPrepaidEJBBean10().sendMailAsync(null,numberUtils.toLong(numberUtils.random(800000,900000)), emailBody);
      //Assert.assertNull(resp);
    }catch (NotFoundException e){
      Assert.assertEquals("Cliente no existe",CLIENTE_NO_EXISTE.getValue(),e.getCode());
      System.out.println("Cliente no existe");
    }

    try { // USUARIO BLOQUEADO
      User user = registerUser();
      user.setGlobalStatus(UserStatus.LOCKED);
      updateUser(user);

      EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD, "");
      getMailPrepaidEJBBean10().sendMailAsync(null,user.getId(), emailBody);
      //Assert.assertNull(resp);
    }catch (ValidationException e){
      Assert.assertEquals("Cliente Borrado o bloqueado",CLIENTE_BLOQUEADO_O_BORRADO.getValue(),e.getCode());
      System.out.println("Cliente Borrado o bloqueado");
    }
    try { // NO TIENE PREPAGO
      User user = registerUser();
      updateUser(user);
      EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD, "");
      getMailPrepaidEJBBean10().sendMailAsync(null,user.getId(), emailBody);
      //Assert.assertNull(resp);
    }catch (ValidationException e){
      Assert.assertEquals("Cliente no tiene prepago",CLIENTE_NO_TIENE_PREPAGO.getValue(),e.getCode());
      System.out.println("Cliente no tiene prepago");
    }
    try { // PREPAGO BLOQUEADO
      User user = registerUser();
      updateUser(user);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
      prepaidUser = createPrepaidUser10(prepaidUser);

      EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD, user.getEmail().getValue());
      getMailPrepaidEJBBean10().sendMailAsync(null,user.getId(), emailBody);
      //Assert.assertNull(resp);
    }catch (ValidationException e){
      Assert.assertEquals("Cliente prepago bloqueado",CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(),e.getCode());
      System.out.println("Cliente prepago bloqueado");
    }
    try { // NO TIENE TARJETA
      User user = registerUser();
      updateUser(user);
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD, user.getEmail().getValue());
      getMailPrepaidEJBBean10().sendMailAsync(null,user.getId(), emailBody);
      //Assert.assertNull(resp);
    }catch (ValidationException e){
      Assert.assertEquals("Cliente no tiene tarjeta",TARJETA_NO_EXISTE.getValue(),e.getCode());
      System.out.println("Cliente no tiene tarjeta");
    }
    try { // TARJETA BLOQUEADA
      User user = registerUser();
      updateUser(user);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser);
      prepaidCard10.setStatus(PrepaidCardStatus.LOCKED);
      createPrepaidCard10(prepaidCard10);

      EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD, user.getEmail().getValue());
      getMailPrepaidEJBBean10().sendMailAsync(null,user.getId(), emailBody);
      //Assert.assertNull(resp);
    }catch (ValidationException e){
      Assert.assertEquals("Tarjeta bloqueada",TARJETA_CON_BLOQUEO_TEMPORAL.getValue(),e.getCode());
      System.out.println("Tarjeta bloqueada");
    }
    try { // ARJETA EXPIRADA
      User user = registerUser();
      updateUser(user);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser);
      prepaidCard10.setStatus(PrepaidCardStatus.EXPIRED);
      createPrepaidCard10(prepaidCard10);

      EmailBody emailBody = new EmailBody(TEMPLATE_MAIL_SEND_CARD, user.getEmail().getValue());
      getMailPrepaidEJBBean10().sendMailAsync(null,user.getId(), emailBody);
      //Assert.assertNull(resp);
    }catch (ValidationException e){
      Assert.assertEquals("Tarjeta expirada",TARJETA_EXPIRADA.getValue(),e.getCode());
      System.out.println("Tarjeta expirada");
    }
  }

}
