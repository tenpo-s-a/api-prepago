package cl.multicaja.test.api.unit;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_topupUserBalance extends TestBaseRouteUnit {

  @Test
  public void topupUserBalance_userNotFound() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    newPrepaidTopup.setRut(1);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario", Integer.valueOf(102001), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_prepaidUserNotFound() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario prepago", Integer.valueOf(102003), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_prepaidUserNotActive() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    prepaidUser = createPrepaidUser(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(ValidationException nfex) {
      Assert.assertEquals("el usuario prepago esta bloqueado", Integer.valueOf(102002), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_invalidCardByCardLockedhard() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", Integer.valueOf(106000), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_invalidCardByCardExpire() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", Integer.valueOf(106000), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_validate_cdt() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    //se debe establecer la primera carga mayor a 3000 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(BigDecimal.valueOf(numberUtils.random(3000, 10000)));

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    String messageId = prepaidTopup.getMessageId();

    if (CamelFactory.getInstance().isCamelRunning()) {
      Assert.assertNotNull("Debe tener messageId dado que camel si se encuentra en ejecucion", messageId);

      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
      ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());

      Assert.assertNotNull("debe tener un objeto de cdt", remoteTopup.getData().getCdtTransaction10());
      Assert.assertNotNull("debe tener un id de cdt", remoteTopup.getData().getCdtTransaction10().getExternalTransactionId());

    } else {
      Assert.assertNull("No debe tener messageId dado que camel no se encuentra en ejecucion", messageId);
    }
  }

  @Test
  public void topupUserBalance_validate_prepaidMovement() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    //se debe establecer la primera carga mayor a 3000 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(BigDecimal.valueOf(numberUtils.random(3000, 10000)));

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    String messageId = prepaidTopup.getMessageId();

    if (CamelFactory.getInstance().isCamelRunning()) {
      Assert.assertNotNull("Debe tener messageId dado que camel si se encuentra en ejecucion", messageId);

      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
      ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());

      Assert.assertNotNull("debe tener un objeto de prepaidMovement", remoteTopup.getData().getPrepaidMovement10());
      Assert.assertTrue("debe tener un id de prepaidMovement", remoteTopup.getData().getPrepaidMovement10().getId() > 0);

    } else {
      Assert.assertNull("No debe tener messageId dado que camel no se encuentra en ejecucion", messageId);
    }
  }
}
