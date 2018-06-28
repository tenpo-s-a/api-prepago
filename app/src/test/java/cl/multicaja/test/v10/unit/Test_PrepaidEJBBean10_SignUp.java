package cl.multicaja.test.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.NewPrepaidUserSignup10;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.CLIENTE_BLOQUEADO_O_BORRADO;
import static cl.multicaja.core.model.Errors.CLIENTE_YA_TIENE_CLAVE;

public class Test_PrepaidEJBBean10_SignUp extends TestBaseUnit {



  @Test(expected = BadRequestException.class)
  public void initSignUpErrorSignUpNull() throws Exception {
    getPrepaidEJBBean10().initUserSignup(null,null);
  }
  @Test(expected = BadRequestException.class)
  public void initSignUpErrorRutNull() throws Exception {
    NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
    newPrepaidUserSignup10.setEmail(getUniqueEmail());
    getPrepaidEJBBean10().initUserSignup(null,newPrepaidUserSignup10);
  }
  @Test(expected = BadRequestException.class)
  public void initSignUpErrorMailNull() throws Exception {
    NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
    newPrepaidUserSignup10.setRut(getUniqueRutNumber());
    getPrepaidEJBBean10().initUserSignup(null,newPrepaidUserSignup10);
  }
  @Test
  public void initSignUpErroresValidacion() {
    {// USUARIO YA TIENE CLAVE
      try
      {
        User user = registerUser();
        NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
        newPrepaidUserSignup10.setRut(user.getRut().getValue());
        newPrepaidUserSignup10.setEmail(user.getEmail().getValue());
        getPrepaidEJBBean10().initUserSignup(null, newPrepaidUserSignup10);

      } catch (ValidationException e) {
        Assert.assertEquals("Error Usuario ya existe", CLIENTE_YA_TIENE_CLAVE.getValue(), e.getCode());
      } catch (Exception e) {
        Assert.fail("No debe llegar aca !!ERROR!!");
      }
    }

    {// USUARIO BLOQUEADO
      try
      {
        User user = registerUser(UserStatus.DELETED);
        NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
        newPrepaidUserSignup10.setRut(user.getRut().getValue());
        newPrepaidUserSignup10.setEmail(user.getEmail().getValue());
        getPrepaidEJBBean10().initUserSignup(null, newPrepaidUserSignup10);
      } catch (ValidationException e) {
        Assert.assertEquals("Error Usuario ya existe", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), e.getCode());
      } catch (Exception e) {
        Assert.fail("No debe llegar aca !!ERROR!!");
      }
    }


  }
}
