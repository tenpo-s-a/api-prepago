package cl.multicaja.test.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.NewPrepaidUserSignup10;
import cl.multicaja.prepaid.model.v10.PrepaidUserSignup10;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.CLIENTE_BLOQUEADO_O_BORRADO;
import static cl.multicaja.core.model.Errors.CORREO_YA_UTILIZADO;

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
        PrepaidUserSignup10 prepaidUserSignup10 = getPrepaidEJBBean10().initUserSignup(null, newPrepaidUserSignup10);
        Assert.assertNotNull("Debe existir prepaid user signup",prepaidUserSignup10);
        System.out.println("CLIENTE SIN PROBLEMAS");
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
        System.out.println("CLIENTE_BLOQUEADO_O_BORRADO");
        Assert.assertEquals("Error Usuario ya existe", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), e.getCode());
      } catch (Exception e) {
        Assert.fail("No debe llegar aca !!ERROR!!");
      }
    }

    {// USUARIO CON MAILS INCORRECTO
      try
      {
        User user = registerUser(UserStatus.DELETED);
        NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
        newPrepaidUserSignup10.setRut(getUniqueRutNumber());
        newPrepaidUserSignup10.setEmail(user.getEmail().getValue());
        getPrepaidEJBBean10().initUserSignup(null, newPrepaidUserSignup10);
      } catch (ValidationException e) {
        System.out.println("CORREO_YA_UTILIZADO");
        Assert.assertEquals("Error Usuario ya existe", CORREO_YA_UTILIZADO.getValue(), e.getCode());
      } catch (Exception e) {
        Assert.fail("No debe llegar aca !!ERROR!!");
      }
    }
  }
}
