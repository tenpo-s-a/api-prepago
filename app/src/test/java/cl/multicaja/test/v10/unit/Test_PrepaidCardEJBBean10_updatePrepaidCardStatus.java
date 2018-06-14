package cl.multicaja.test.v10.unit;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @autor vutreras
 */
public class Test_PrepaidCardEJBBean10_updatePrepaidCardStatus extends TestBaseUnit {

  @Test
  public void updatePrepaidCardStatus_ok() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10();
    card = createPrepaidCard10(card);

    getPrepaidCardEJBBean10().updatePrepaidCardStatus(null, card.getId(), PrepaidCardStatus.EXPIRED);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar un usuario", c1);
    Assert.assertEquals("el estado debe estar actualizado", PrepaidCardStatus.EXPIRED, c1.getStatus());
  }

  @Test
  public void updatePrepaidCardStatus_not_ok() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10();
    card = createPrepaidCard10(card);

    try {

      getPrepaidCardEJBBean10().updatePrepaidCardStatus(null, card.getId(), null);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe se error 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar un usuario", c1);
    Assert.assertEquals("el estado debe estar actualizado", PrepaidCardStatus.ACTIVE, c1.getStatus());
  }
}
