package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementFee10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean11_getPrepaidMovementFeeById extends TestBaseUnit {

  @Test
  public void getPrepaidMovementFee_ok() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    PrepaidMovementFee10 fee = buildPrepaidMovementFee10(prepaidMovement);
    fee = createPrepaidMovementFee10(fee);

    PrepaidMovementFee10 foundFee = getPrepaidMovementEJBBean11().getPrepaidMovementFeeById(fee.getId());

    Assert.assertNotNull("Debe existir", foundFee);
    Assert.assertEquals("Debe tener el mismo id", fee.getId(), foundFee.getId());
    Assert.assertEquals("Debe tener mismo id de movimiento", fee.getMovementId(), foundFee.getMovementId());
    Assert.assertEquals("Debe tener mismo amount", fee.getAmount(), foundFee.getAmount());
    Assert.assertEquals("Debe tener mismo iva", fee.getIva(), foundFee.getIva());
    Assert.assertTrue("Debe tener fecha de creacion reciente", isRecentLocalDateTime(foundFee.getTimestamps().getCreatedAt(), 5));
    Assert.assertTrue("Debe tener fecha de creacion actualizacion", isRecentLocalDateTime(foundFee.getTimestamps().getUpdatedAt(), 5));
  }

  @Test
  public void getPrepaidMovementFee_notExist() throws Exception {
    PrepaidMovementFee10 foundFee = getPrepaidMovementEJBBean11().getPrepaidMovementFeeById(Long.MAX_VALUE);
    Assert.assertNull("No debe existir", foundFee);
  }

  @Test(expected = BadRequestException.class)
  public void getPrepaidMovementFee_idNull() throws Exception {
    getPrepaidMovementEJBBean11().getPrepaidMovementFeeById(null);
  }
}
