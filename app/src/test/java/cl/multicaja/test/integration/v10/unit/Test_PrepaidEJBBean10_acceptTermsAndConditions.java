package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.NewTermsAndConditions10;
import org.junit.Assert;
import org.junit.Test;


import java.util.List;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 **/
public class Test_PrepaidEJBBean10_acceptTermsAndConditions extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_McUserIdNull() throws Exception {
    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, null, null);
      Assert.fail("Should not be here");
    } catch(BadRequestException ex) {
      Assert.assertEquals("userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }

    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, Long.valueOf(0), null);
      Assert.fail("Should not be here");
    } catch(BadRequestException ex) {
      Assert.assertEquals("userIdMc 0", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_TermsAndConditionsNull() throws Exception {
    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, Long.MAX_VALUE, null);
      Assert.fail("Should not be here");
    } catch(BadRequestException ex) {
      Assert.assertEquals("userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_TermsAndConditions_Version_Null() throws Exception {
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();

    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, Long.MAX_VALUE, tac);
      Assert.fail("Should not be here");
    } catch(BadRequestException ex) {
      Assert.assertEquals("userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_TermsAndConditions_Version_Empty() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("");

    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, Long.MAX_VALUE, tac);
      Assert.fail("Should not be here");
    } catch(BadRequestException ex) {
      Assert.assertEquals("userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = NotFoundException.class)
  public void shouldReturnExceptionWhen_McUserNull() throws Exception{
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, Long.MAX_VALUE, tac);
      Assert.fail("Should not be here");
    } catch(NotFoundException ex) {
      Assert.assertEquals("user null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_McUserDisabled() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DISABLED);
    updateUser(user);

    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, user.getId(), tac);
      Assert.fail("Should not be here");
    } catch(ValidationException ex) {
      Assert.assertEquals("user disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_McUserLocked() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    user.setGlobalStatus(UserStatus.LOCKED);
    updateUser(user);

    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, user.getId(), tac);
      Assert.fail("Should not be here");
    } catch(ValidationException ex) {
      Assert.assertEquals("user locked", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_McUserDeleted() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DELETED);
    updateUser(user);

    try{
      getPrepaidEJBBean10().acceptTermsAndConditions(null, user.getId(), tac);
      Assert.fail("Should not be here");
    } catch(ValidationException ex) {
      Assert.assertEquals("user deleted", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test
  public void shouldAcceptTermsAndConditions() throws Exception {
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    updateUser(user);

    getPrepaidEJBBean10().acceptTermsAndConditions(null, user.getId(), tac);

    UserFile userFile = getUserClient().getUserFiles(null, user.getId(), "api-prepaid", "TERMS_AND_CONDITIONS", tac.getVersion()).stream().findFirst().get();

    Assert.assertNotNull("Debe tener un registro", userFile);
    Assert.assertEquals("Debe ser del usuario", user.getId(), userFile.getUserId());
    Assert.assertEquals("Debe ser de prepago", "api-prepaid", userFile.getApp());
    Assert.assertEquals("Debe ser TERMS_AND_CONDITIONS", "TERMS_AND_CONDITIONS", userFile.getName());
    Assert.assertEquals("Debe tener version v1.0", "v1.0", userFile.getVersion());
  }

  @Test
  public void shouldDoNothingWhen_TermsAndConditionsAlreadyAccepted() throws Exception {
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    updateUser(user);

    getPrepaidEJBBean10().acceptTermsAndConditions(null, user.getId(), tac);
    getPrepaidEJBBean10().acceptTermsAndConditions(null, user.getId(), tac);

    List<UserFile> files = getUserClient().getUserFiles(null, user.getId(), "api-prepaid", "TERMS_AND_CONDITIONS", tac.getVersion());

    Assert.assertEquals("Debe tener solo 1", 1, files.size());
    UserFile userFile = files.get(0);
    Assert.assertNotNull("Debe tener un registro", userFile);
    Assert.assertEquals("Debe ser del usuario", user.getId(), userFile.getUserId());
    Assert.assertEquals("Debe ser de prepago", "api-prepaid", userFile.getApp());
    Assert.assertEquals("Debe ser TERMS_AND_CONDITIONS", "TERMS_AND_CONDITIONS", userFile.getName());
    Assert.assertEquals("Debe tener version v1.0", "v1.0", userFile.getVersion());
  }
}
