package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean10_updateStatusMovementConSwitch extends TestBaseUnit {

    @Test(expected = BadRequestException.class)
    public void testUpdateMovementBadRequest1() throws Exception {
      getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null,null,null);
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateMovementBadRequest2() throws Exception {
      getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, getUniqueLong(),null);
    }

    @Test
    public void updateOk() throws  Exception {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      Assert.assertNotNull("Debe existir el movimiento",prepaidMovement10);

      boolean bResult = getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, prepaidMovement10.getId(), ConciliationStatusType.NOT_RECONCILED);
      Assert.assertTrue("Update exitoso", bResult);

      PrepaidMovement10 prepaidMovement10_2 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      Assert.assertNotEquals("Deben Ser diferentes", prepaidMovement10, prepaidMovement10_2);
      Assert.assertEquals("El status con switch debe ser no conciliado",ConciliationStatusType.NOT_RECONCILED,prepaidMovement10_2.getConSwitch());
    }

}