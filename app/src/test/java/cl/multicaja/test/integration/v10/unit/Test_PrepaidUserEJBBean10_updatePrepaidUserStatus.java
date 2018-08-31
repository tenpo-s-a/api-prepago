package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @autor vutreras
 */
public class Test_PrepaidUserEJBBean10_updatePrepaidUserStatus extends TestBaseUnit {

  @Test
  public void updatePrepaidUserStatus_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());

    getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser10.getId(), PrepaidUserStatus.DISABLED);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser DISABLED", PrepaidUserStatus.DISABLED, prepaidUser10.getStatus());
  }

  @Test
  public void updatePrepaidUserStatus_not_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());

    try {

      getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser10.getId(), null);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe se error 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());
  }
}
