package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_PrepaidMovementEJBBean10_isFirstTopup extends TestBaseUnit {

  @Test
  public void isFirstTopup_False() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement1 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement1.setEstadoNegocio(BusinessStatusType.REVERSED);

    createPrepaidMovement10(prepaidMovement1);

    PrepaidMovement10 prepaidMovement2 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement2.setEstado(PrepaidMovementStatus.PROCESS_OK);

    createPrepaidMovement10(prepaidMovement2);

    Assert.assertFalse("No es primera carga", getPrepaidMovementEJBBean10().isFirstTopup(prepaidUser.getId()));
  }

  @Test
  public void isFirstTopup_True() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Assert.assertTrue("Es primera carga", getPrepaidMovementEJBBean10().isFirstTopup(prepaidUser.getId()));
  }

  @Test
  public void  shouldReturnError_PrepaidUserId_Null() throws Exception {

    try{
      getPrepaidMovementEJBBean10().isFirstTopup(null);
    } catch (BadRequestException ex) {
      Assert.assertEquals("error parametro faltante", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }

  }
}
