package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.ERROR_DE_COMUNICACION_CON_BBDD;

/**
 * @autor vutreras
 */
public class Test_PrepaidUserEJBBean10_createPrepaidUser extends TestBaseUnit {

  @Test
  public void createPrepaidUser_ok() throws Exception {
    PrepaidUser10 user = buildPrepaidUserv2();
    user = createPrepaidUserV2(user);
    Assert.assertNotNull("Debe existir",user);
  }

  @Test
  public void createPrepaidUser_not_ok() throws Exception {

    /**
     * Caso de registro de un nuevo usuario, pero que luego se intenta registrar el mismo y deberia fallar
     */

    PrepaidUser10 user = buildPrepaidUserv2();
    user = createPrepaidUserV2(user);

    //se intenta registrar exactamente el mismo usuario
    try {

      getPrepaidUserEJBBean10().createPrepaidUser(null, user);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BaseException bex) {
      Assert.assertEquals("debe retornar excepcion de dato duplicado", ERROR_DE_COMUNICACION_CON_BBDD.getValue(), bex.getCode());
    }
  }
}
