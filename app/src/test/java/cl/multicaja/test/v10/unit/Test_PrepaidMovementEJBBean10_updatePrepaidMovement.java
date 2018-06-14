package cl.multicaja.test.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean10_updatePrepaidMovement extends TestBaseUnit {

  @Test
  public void updatePrepaidMovement_status_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement10.getId(), PrepaidMovementStatus.IN_PROCESS);

    prepaidMovement10.setEstado(PrepaidMovementStatus.IN_PROCESS);

    PrepaidMovement10 prepaidMovement1_1 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("deben ser iguales", prepaidMovement10, prepaidMovement1_1);
  }

  @Test
  public void updatePrepaidMovement_data_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement10.getId(),1,2, CodigoMoneda.CHILE_CLP.getValue(), PrepaidMovementStatus.PROCESS_OK);

    prepaidMovement10.setNumextcta(1);
    prepaidMovement10.setNummovext(2);
    prepaidMovement10.setClamone(CodigoMoneda.CHILE_CLP.getValue());
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);

    PrepaidMovement10 prepaidMovement1_1 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("deben ser iguales", prepaidMovement10, prepaidMovement1_1);
  }
}
