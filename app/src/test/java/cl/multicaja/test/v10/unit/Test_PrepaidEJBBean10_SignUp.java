package cl.multicaja.test.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.NewPrepaidUserSignup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserSignup10;
import cl.multicaja.users.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.*;

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
        Assert.assertEquals("Error Usuario ya existe", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), e.getCode());
      } catch (Exception e) {
        Assert.fail("No debe llegar aca !!ERROR!!");
      }
    }
  }

  @Test
  public void testFinishSignupError() {

    try { // VERIFICA QUE FALTA UN PARAMETRO DE ENTRADA
      NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
      newPrepaidUserSignup10.setRut(getUniqueRutNumber());
      newPrepaidUserSignup10.setEmail(getUniqueEmail());
      PrepaidUserSignup10 prepaidUserSignup10 = getPrepaidEJBBean10().initUserSignup(null,newPrepaidUserSignup10);
      Assert.assertNotNull("Debe retornar prepaidSignup",prepaidUserSignup10);
      PrepaidUser10 prepaidUser10 = getPrepaidEJBBean10().finishSignup(null,null);
      Assert.assertNull(prepaidUser10);
    }catch (BadRequestException e){
      System.out.println("PARAMETRO_FALTANTE_$VALUE");
      Assert.assertEquals("Debe Fallar ",e.getCode(),PARAMETRO_FALTANTE_$VALUE.getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try { //VERIFICA QUE EL EMAIL NO ESTA VALIDADADO
      NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
      newPrepaidUserSignup10.setRut(getUniqueRutNumber());
      newPrepaidUserSignup10.setEmail(getUniqueEmail());
      PrepaidUserSignup10 prepaidUserSignup10 = getPrepaidEJBBean10().initUserSignup(null,newPrepaidUserSignup10);
      Assert.assertNotNull("Debe retornar prepaidSignup",prepaidUserSignup10);
      PrepaidUser10 prepaidUser10 = getPrepaidEJBBean10().finishSignup(null,prepaidUserSignup10.getUserId());
      Assert.assertNull(prepaidUser10);
    }catch (ValidationException e){
      System.out.println("CLIENTE_NO_TIENE_CLAVE");
      Assert.assertEquals("Debe Fallar ",e.getCode(),CLIENTE_NO_TIENE_CLAVE.getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
    try {// VERIFICA QUE EL CELULAR NO ESTA VALIDADO
      NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
      newPrepaidUserSignup10.setRut(getUniqueRutNumber());
      newPrepaidUserSignup10.setEmail(getUniqueEmail());
      PrepaidUserSignup10 prepaidUserSignup10 = getPrepaidEJBBean10().initUserSignup(null,newPrepaidUserSignup10);
      Assert.assertNotNull("Debe retornar prepaidSignup",prepaidUserSignup10);

      User user = getUsersEJBBean10().getUserById(null,prepaidUserSignup10.getUserId());
      user = getUsersEJBBean10().fillUser(user); // Actualiza los estados del usuario a Validado
      user.getEmail().setStatus(EmailStatus.VERIFIED);
      user.getCellphone().setStatus(CellphoneStatus.UNVERIFIED);
      user.setPassword(null);
      user = updateUser(user);
      PrepaidUser10 prepaidUser10 = getPrepaidEJBBean10().finishSignup(null,prepaidUserSignup10.getUserId());
      Assert.assertNull(prepaidUser10);
    }catch (ValidationException e){
      System.out.println("PROCESO_DE_REGISTRO_CELULAR_NO_VALIDADO");
      Assert.assertEquals("Debe Fallar ",e.getCode(),PROCESO_DE_REGISTRO_CELULAR_NO_VALIDADO.getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
    try { // ERROR SIN PASSWORD
      NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
      newPrepaidUserSignup10.setRut(getUniqueRutNumber());
      newPrepaidUserSignup10.setEmail(getUniqueEmail());
      PrepaidUserSignup10 prepaidUserSignup10 = getPrepaidEJBBean10().initUserSignup(null,newPrepaidUserSignup10);
      Assert.assertNotNull("Debe retornar prepaidSignup",prepaidUserSignup10);
      User user = getUsersEJBBean10().getUserById(null,prepaidUserSignup10.getUserId());

      Assert.assertNotNull("Debe existir user ",user);

      user = getUsersEJBBean10().fillUser(user); // Actualiza los estados del usuario a Validado
      user.getCellphone().setStatus(CellphoneStatus.VERIFIED);
      user.getEmail().setStatus(EmailStatus.UNVERIFIED);
      user = updateUser(user);

      PrepaidUser10 prepaidUser10 = getPrepaidEJBBean10().finishSignup(null,prepaidUserSignup10.getUserId());
      Assert.assertNull(prepaidUser10);
    }catch (ValidationException e) {
      System.out.println("PROCESO_DE_REGISTRO_EMAIL_NO_VALIDADO");
      Assert.assertEquals("Debe Fallar ",e.getCode(),PROCESO_DE_REGISTRO_EMAIL_NO_VALIDADO.getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

  }
  @Test
  public void testFinishSignupOk() {
    try {
      NewPrepaidUserSignup10 newPrepaidUserSignup10 = new NewPrepaidUserSignup10();
      newPrepaidUserSignup10.setRut(getUniqueRutNumber());
      newPrepaidUserSignup10.setEmail(getUniqueEmail());
      PrepaidUserSignup10 prepaidUserSignup10 = getPrepaidEJBBean10().initUserSignup(null,newPrepaidUserSignup10);
      Assert.assertNotNull("Debe retornar prepaidSignup",prepaidUserSignup10);
      User user = getUsersEJBBean10().getUserById(null,prepaidUserSignup10.getUserId());
      Assert.assertNotNull("Debe existir user ",user);
      System.out.println(user);
      user = getUsersEJBBean10().fillUser(user); // Actualiza los estados del usuario a Validado
      user.getEmail().setStatus(EmailStatus.VERIFIED);
      user = updateUser(user);
      System.out.println(user);
      PrepaidUser10 prepaidUser10 = getPrepaidEJBBean10().finishSignup(null,prepaidUserSignup10.getUserId());
      Assert.assertNotNull("Usuario prepago debe ser no nulo",prepaidUser10);

    } catch (BadRequestException e){
        System.out.println(e);
        Assert.fail("No debe caer aca.");
    } catch (Exception e) {
        System.out.println(e);
        Assert.fail("No debe caer aca.");
    }
  }
}
