package cl.multicaja.test.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor abarazarte
 */
public class Test_PrepaidEJBBean10_lockPrepaidCard extends TestBaseUnit {

  @Test
  public void shouldReturnExceptionWhen_McUserIdNull() throws Exception{
    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, null);
    } catch(BadRequestException ex) {
      Assert.assertEquals("user null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, Long.valueOf(0));
    } catch(NotFoundException ex) {
      Assert.assertEquals("user id null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserNull() throws Exception{
    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, Long.MAX_VALUE);
    } catch(NotFoundException ex) {
      Assert.assertEquals("user null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserDisabled() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DISABLED);
    updateUser(user);

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("user disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserLocked() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.LOCKED);
    updateUser(user);

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("user locked", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserDeleted() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DELETED);
    updateUser(user);

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("user deleted", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidUserNull() throws Exception {

    User user = registerUser();
    updateUser(user);

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(NotFoundException ex) {
      Assert.assertEquals("user deleted", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidUserDisabled() throws Exception {

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    createPrepaidUser10(prepaidUser);

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("user deleted", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidNotActive() throws Exception  {
    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10Pending(prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    Assert.assertEquals("status PENDING", PrepaidCardStatus.PENDING, prepaidCard.getStatus());

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("prepaid card not active", TARJETA_NO_ACTIVA.getValue(), ex.getCode());
    }

  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardNotExists() throws Exception  {
    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(ValidationException ex) {
      Assert.assertEquals("prepaid card not exists", TARJETA_NO_EXISTE.getValue(), ex.getCode());
    }

  }

  @Test
  public void shouldLockPrepaidCard() throws Exception {

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    Assert.assertEquals("status ACTIVE", PrepaidCardStatus.ACTIVE, prepaidCard.getStatus());

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(Exception ex) {
      Assert.fail("should not be here");
    }

    prepaidCard = getPrepaidEJBBean10().getPrepaidCard(null, user.getId());

    Assert.assertEquals("status ACTIVE", PrepaidCardStatus.LOCKED, prepaidCard.getStatus());
  }

  @Test
  public void shouldDoNothing_PrepaidCardLocked() throws Exception {

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
    prepaidCard.setStatus(PrepaidCardStatus.LOCKED);
    prepaidCard = createPrepaidCard10(prepaidCard);

    Assert.assertEquals("status LOCKED", PrepaidCardStatus.LOCKED, prepaidCard.getStatus());

    try{
      getPrepaidEJBBean10().lockPrepaidCard(null, user.getId());
    } catch(Exception ex) {
      Assert.fail("should not be here");
    }

    prepaidCard = getPrepaidEJBBean10().getPrepaidCard(null, user.getId());

    Assert.assertEquals("status ACTIVE", PrepaidCardStatus.LOCKED, prepaidCard.getStatus());
  }

}
