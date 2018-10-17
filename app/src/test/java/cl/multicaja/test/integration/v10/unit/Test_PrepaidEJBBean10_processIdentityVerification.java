package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.RutStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.core.model.Errors.CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO;

/**
 * @author abarazarte
 **/
public class Test_PrepaidEJBBean10_processIdentityVerification extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_userId_null() throws Exception {
    try{
      getPrepaidEJBBean10().processIdentityVerification(null, null, null);
    } catch(BadRequestException ex) {
      Assert.assertEquals("userId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_userId_0() throws Exception {
    try{
      getPrepaidEJBBean10().processIdentityVerification(null, Long.valueOf(0), null);
    } catch(BadRequestException ex) {
      Assert.assertEquals("userId 0", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_identityVerification_null() throws Exception {
    try{
      getPrepaidEJBBean10().processIdentityVerification(null, Long.MAX_VALUE, null);
    } catch(BadRequestException ex) {
      Assert.assertEquals("identityVerification null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = NotFoundException.class)
  public void shouldReturnExceptionWhen_McUserNull() throws Exception {
    IdentityValidation10 identityValidation10 = new IdentityValidation10();
    try{
      getPrepaidEJBBean10().processIdentityVerification(null, Long.MAX_VALUE, identityValidation10);
    } catch(NotFoundException ex) {
      Assert.assertEquals("user null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_McUserDisabled() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DISABLED);
    updateUser(user);

    IdentityValidation10 identityValidation10 = new IdentityValidation10();

    try{
      getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);
    } catch(ValidationException ex) {
      Assert.assertEquals("user disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_McUserLocked() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.LOCKED);
    updateUser(user);

    IdentityValidation10 identityValidation10 = new IdentityValidation10();

    try{
      getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);
    } catch(ValidationException ex) {
      Assert.assertEquals("user locked", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_McUserDeleted() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DELETED);
    updateUser(user);

    IdentityValidation10 identityValidation10 = new IdentityValidation10();

    try{
      getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);
    } catch(ValidationException ex) {
      Assert.assertEquals("user deleted", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = NotFoundException.class)
  public void shouldReturnExceptionWhen_PrepaidUserNull() throws Exception {

    User user = registerUser();
    updateUser(user);

    IdentityValidation10 identityValidation10 = new IdentityValidation10();

    try{
      getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);
    } catch(NotFoundException ex) {
      Assert.assertEquals("user deleted", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
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

    IdentityValidation10 identityValidation10 = new IdentityValidation10();

    try{
      getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);
    } catch(ValidationException ex) {
      Assert.assertEquals("user deleted", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test
  public void shouldProcessIdentityValidation_nameStatus() throws Exception {
    User user = registerUser();
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.setNameStatus(NameStatus.IN_REVIEW);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser = createPrepaidUser10(prepaidUser);
    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 0", Integer.valueOf(0), prepaidUser.getIdentityVerificationAttempts());

    IdentityValidation10 identityValidation10 = new IdentityValidation10();
    identityValidation10.setIsCiValid("Si");
    identityValidation10.setIsGsintelOk("Si");
    identityValidation10.setUserPhotoMatchesCi("Si");
    identityValidation10.setNameAndLastnameMatchesCi("Si");
    identityValidation10.setRutMatchesCi("Si");

    try{
      User verifiedUser = getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);

      Assert.assertNotNull("Debe devovler un usuario", verifiedUser);
      Assert.assertEquals("Debe ser el mismo usuario", user.getId(), verifiedUser.getId());
      Assert.assertEquals("Debe tener rut.status VERIFIED", RutStatus.VERIFIED, verifiedUser.getRut().getStatus());
      Assert.assertEquals("Debe tener nameStatus VERIFIED", NameStatus.VERIFIED, verifiedUser.getNameStatus());

      prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

      Assert.assertEquals("Debe tener intento de validacion = 1", Integer.valueOf(1), prepaidUser.getIdentityVerificationAttempts());

    } catch(Exception ex) {
      Assert.fail("Should not be here");
    }
  }

  @Test
  public void shouldProcessIdentityValidation_nameStatusAndRutStatus() throws Exception {
    User user = registerUser();
    user.getRut().setStatus(RutStatus.UNVERIFIED);
    user.setNameStatus(NameStatus.IN_REVIEW);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser = createPrepaidUser10(prepaidUser);
    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 0", Integer.valueOf(0), prepaidUser.getIdentityVerificationAttempts());

    IdentityValidation10 identityValidation10 = new IdentityValidation10();
    identityValidation10.setIsCiValid("Si");
    identityValidation10.setIsGsintelOk("Si");
    identityValidation10.setUserPhotoMatchesCi("Si");
    identityValidation10.setNameAndLastnameMatchesCi("Si");
    identityValidation10.setRutMatchesCi("Si");

    try{
      User verifiedUser = getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);

      Assert.assertNotNull("Debe devovler un usuario", verifiedUser);
      Assert.assertEquals("Debe ser el mismo usuario", user.getId(), verifiedUser.getId());
      Assert.assertEquals("Debe tener rut.status VERIFIED", RutStatus.VERIFIED, verifiedUser.getRut().getStatus());
      Assert.assertEquals("Debe tener nameStatus VERIFIED", NameStatus.VERIFIED, verifiedUser.getNameStatus());

      prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

      Assert.assertEquals("Debe tener intento de validacion = 1", Integer.valueOf(1), prepaidUser.getIdentityVerificationAttempts());

    } catch(Exception ex) {
      Assert.fail("Should not be here");
    }
  }

  @Test
  public void shouldProcessIdentityValidation_changeName() throws Exception {
    User user = registerUser();
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.setNameStatus(NameStatus.IN_REVIEW);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser = createPrepaidUser10(prepaidUser);
    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 0", Integer.valueOf(0), prepaidUser.getIdentityVerificationAttempts());

    IdentityValidation10 identityValidation10 = new IdentityValidation10();
    identityValidation10.setIsCiValid("Si");
    identityValidation10.setIsGsintelOk("Si");
    identityValidation10.setUserPhotoMatchesCi("Si");
    identityValidation10.setNameAndLastnameMatchesCi("No");
    identityValidation10.setRutMatchesCi("Si");
    identityValidation10.setNewName("NewName");

    try{
      User verifiedUser = getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);

      Assert.assertNotNull("Debe devovler un usuario", verifiedUser);
      Assert.assertEquals("Debe ser el mismo usuario", user.getId(), verifiedUser.getId());
      Assert.assertEquals("Debe tener rut.status VERIFIED", RutStatus.VERIFIED, verifiedUser.getRut().getStatus());
      Assert.assertEquals("Debe tener nameStatus VERIFIED", NameStatus.VERIFIED, verifiedUser.getNameStatus());
      Assert.assertEquals("Debe tener nuevo nombre", identityValidation10.getNewName(), verifiedUser.getName());

      prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

      Assert.assertEquals("Debe tener intento de validacion = 1", Integer.valueOf(1), prepaidUser.getIdentityVerificationAttempts());

    } catch(Exception ex) {
      Assert.fail("Should not be here");
    }
  }

  @Test
  public void shouldProcessIdentityValidation_changeLastname() throws Exception {

    User user = registerUser();
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.setNameStatus(NameStatus.IN_REVIEW);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser = createPrepaidUser10(prepaidUser);
    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 0", Integer.valueOf(0), prepaidUser.getIdentityVerificationAttempts());

    IdentityValidation10 identityValidation10 = new IdentityValidation10();
    identityValidation10.setIsCiValid("Si");
    identityValidation10.setIsGsintelOk("Si");
    identityValidation10.setUserPhotoMatchesCi("Si");
    identityValidation10.setNameAndLastnameMatchesCi("No");
    identityValidation10.setRutMatchesCi("Si");
    identityValidation10.setNewLastname("NewLastname");

    try{
      User verifiedUser = getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);

      Assert.assertNotNull("Debe devovler un usuario", verifiedUser);
      Assert.assertEquals("Debe ser el mismo usuario", user.getId(), verifiedUser.getId());
      Assert.assertEquals("Debe tener rut.status VERIFIED", RutStatus.VERIFIED, verifiedUser.getRut().getStatus());
      Assert.assertEquals("Debe tener nameStatus VERIFIED", NameStatus.VERIFIED, verifiedUser.getNameStatus());
      Assert.assertEquals("Debe tener nuevo apellido", identityValidation10.getNewLastname(), verifiedUser.getLastname_1());

      prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

      Assert.assertEquals("Debe tener intento de validacion = 1", Integer.valueOf(1), prepaidUser.getIdentityVerificationAttempts());

    } catch(Exception ex) {
      Assert.fail("Should not be here");
    }
  }

  @Test
  public void shouldProcessIdentityValidation_changeNameAndLastname() throws Exception {
    User user = registerUser();
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.setNameStatus(NameStatus.IN_REVIEW);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser = createPrepaidUser10(prepaidUser);
    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 0", Integer.valueOf(0), prepaidUser.getIdentityVerificationAttempts());

    IdentityValidation10 identityValidation10 = new IdentityValidation10();
    identityValidation10.setIsCiValid("Si");
    identityValidation10.setIsGsintelOk("Si");
    identityValidation10.setUserPhotoMatchesCi("Si");
    identityValidation10.setNameAndLastnameMatchesCi("No");
    identityValidation10.setRutMatchesCi("Si");
    identityValidation10.setNewLastname("NewLastname");
    identityValidation10.setNewName("NewName");

    try{
      User verifiedUser = getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);

      Assert.assertNotNull("Debe devovler un usuario", verifiedUser);
      Assert.assertEquals("Debe ser el mismo usuario", user.getId(), verifiedUser.getId());
      Assert.assertEquals("Debe tener rut.status VERIFIED", RutStatus.VERIFIED, verifiedUser.getRut().getStatus());
      Assert.assertEquals("Debe tener nameStatus VERIFIED", NameStatus.VERIFIED, verifiedUser.getNameStatus());
      Assert.assertEquals("Debe tener nuevo nombre", identityValidation10.getNewName(), verifiedUser.getName());
      Assert.assertEquals("Debe tener nuevo apellido", identityValidation10.getNewLastname(), verifiedUser.getLastname_1());

      prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

      Assert.assertEquals("Debe tener intento de validacion = 1", Integer.valueOf(1), prepaidUser.getIdentityVerificationAttempts());

    } catch(Exception ex) {
      Assert.fail("Should not be here");
    }
  }

  @Test
  public void shouldProcessIdentityValidation_blockUser() throws Exception {
    User user = registerUser();
    user.getRut().setStatus(RutStatus.UNVERIFIED);
    user.setNameStatus(NameStatus.IN_REVIEW);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser = createPrepaidUser10(prepaidUser);
    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 0", Integer.valueOf(0), prepaidUser.getIdentityVerificationAttempts());

    IdentityValidation10 identityValidation10 = new IdentityValidation10();
    identityValidation10.setIsCiValid("Si");
    identityValidation10.setIsGsintelOk("Si");
    identityValidation10.setUserPhotoMatchesCi("No");
    identityValidation10.setNameAndLastnameMatchesCi("No");
    identityValidation10.setRutMatchesCi("Si");
    identityValidation10.setNewLastname("NewLastname");
    identityValidation10.setNewName("NewName");

    //intento 1
    prepaidUser = getPrepaidUserEJBBean10().incrementIdentityVerificationAttempt(null, prepaidUser);
    Assert.assertEquals("Debe tener intento de validacion = 1", Integer.valueOf(1), prepaidUser.getIdentityVerificationAttempts());
    //inento 2
    prepaidUser = getPrepaidUserEJBBean10().incrementIdentityVerificationAttempt(null, prepaidUser);
    Assert.assertEquals("Debe tener intento de validacion = 2", Integer.valueOf(2), prepaidUser.getIdentityVerificationAttempts());

    try{
      User verifiedUser = getPrepaidEJBBean10().processIdentityVerification(null, user.getId(), identityValidation10);

      Assert.assertNotNull("Debe devovler un usuario", verifiedUser);
      Assert.assertEquals("Debe ser el mismo usuario", user.getId(), verifiedUser.getId());
      Assert.assertEquals("Debe tener rut.status UNVERIFIED", RutStatus.UNVERIFIED, verifiedUser.getRut().getStatus());
      Assert.assertEquals("Debe tener nameStatus IN_REVIEW", NameStatus.IN_REVIEW, verifiedUser.getNameStatus());
      Assert.assertNotEquals("No Debe tener nuevo nombre", identityValidation10.getNewName(), verifiedUser.getName());
      Assert.assertEquals("No Debe tener nuevo nombre", user.getName(), verifiedUser.getName());
      Assert.assertEquals("No Debe tener nuevo apellido", user.getLastname_1(), verifiedUser.getLastname_1());

      prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

      Assert.assertEquals("Debe tener intento de validacion = 3", Integer.valueOf(3), prepaidUser.getIdentityVerificationAttempts());
      Assert.assertEquals("Debe tener status DISABLED", PrepaidUserStatus.DISABLED, prepaidUser.getStatus());

    } catch(Exception ex) {
      Assert.fail("Should not be here");
    }
  }

}
