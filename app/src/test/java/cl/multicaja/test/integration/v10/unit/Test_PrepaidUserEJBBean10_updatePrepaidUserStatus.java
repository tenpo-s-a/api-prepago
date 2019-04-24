package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser.getStatus());

    getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser.getId(), PrepaidUserStatus.DISABLED);

    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Estado debe ser DISABLED", PrepaidUserStatus.DISABLED, prepaidUser.getStatus());
  }

  @Test
  public void updatePrepaidUserStatus_not_ok() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser.getStatus());

    try {

      getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser.getId(), null);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe se error 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser.getStatus());
  }
}
