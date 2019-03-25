package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor abarazarte
 */
public class Test_PrepaidEJBBean10_unlockPrepaidCard extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_McUserIdNull() throws Exception{
    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, null);
    } catch(BadRequestException ex) {
      Assert.assertEquals("user null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, Long.valueOf(0));
    } catch(BadRequestException ex) {
      Assert.assertEquals("user id null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = NotFoundException.class)
  public void shouldReturnExceptionWhen_McUserNull() throws Exception{
    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, Long.MAX_VALUE);
    } catch(NotFoundException ex) {
      Assert.assertEquals("user null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = NotFoundException.class)
  public void shouldReturnExceptionWhen_PrepaidUserNull() throws Exception {

    User user = registerUser();
    updateUser(user);

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, user.getId());
    } catch(NotFoundException ex) {
      Assert.assertEquals("user deleted", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_PrepaidNotLocked() throws Exception  {
    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10Pending(prepaidUser);
    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);
    prepaidCard = createPrepaidCard10(prepaidCard);

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("prepaid card not active", TARJETA_NO_BLOQUEADA.getValue(), ex.getCode());
      throw ex;
    }

  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_PrepaidCardInProgress() throws Exception  {
    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10Pending(prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    Assert.assertEquals("status PENDING", PrepaidCardStatus.PENDING, prepaidCard.getStatus());

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("prepaid card not active", TARJETA_PRIMERA_CARGA_EN_PROCESO.getValue(), ex.getCode());
      throw ex;
    }

  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_PrepaidCardNotExists() throws Exception  {
    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("prepaid card not exists", TARJETA_PRIMERA_CARGA_PENDIENTE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_PrepaidUserDisabled() throws Exception {

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    createPrepaidUser10(prepaidUser);

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("user deleted", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test
  public void shouldUnlockPrepaidCard() throws Exception {

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
    prepaidCard.setStatus(PrepaidCardStatus.LOCKED);
    prepaidCard = createPrepaidCard10(prepaidCard);

    Assert.assertEquals("status LOCKED", PrepaidCardStatus.LOCKED, prepaidCard.getStatus());

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, user.getId());
    } catch(Exception ex) {
      Assert.fail("should not be here");
    }

    prepaidCard = getPrepaidEJBBean10().getPrepaidCard(null, user.getId());

    Assert.assertEquals("status ACTIVE", PrepaidCardStatus.ACTIVE, prepaidCard.getStatus());
  }

  @Test
  public void shouldDoNothing_PrepaidCardActive() throws Exception {

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    prepaidCard = createPrepaidCard10(prepaidCard);

    Assert.assertEquals("status ACTIVE", PrepaidCardStatus.ACTIVE, prepaidCard.getStatus());

    try{
      getPrepaidEJBBean10().unlockPrepaidCard(null, user.getId());
    } catch(Exception ex) {
      Assert.fail("should not be here");
    }

    prepaidCard = getPrepaidEJBBean10().getPrepaidCard(null, user.getId());

    Assert.assertEquals("status ACTIVE", PrepaidCardStatus.ACTIVE, prepaidCard.getStatus());
  }
}
