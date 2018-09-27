package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
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
    prepaidMovement10.setNumaut("123456");
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement10.getId(), PrepaidMovementStatus.IN_PROCESS);

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
    prepaidMovement10.setNumaut("123456");
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    String pan = Utils.replacePan(getRandomNumericString(16));
    String centalta = getRandomNumericString(4);
    String cuenta = getRandomNumericString(12);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement10.getId(),
      pan,
      centalta,
      cuenta,
      1,
      2,
      CodigoMoneda.CHILE_CLP.getValue(),
      PrepaidMovementStatus.PROCESS_OK);

    prepaidMovement10.setPan(pan);
    prepaidMovement10.setCentalta(centalta);
    prepaidMovement10.setCuenta(cuenta);
    prepaidMovement10.setNumextcta(1);
    prepaidMovement10.setNummovext(2);
    prepaidMovement10.setClamone(CodigoMoneda.CHILE_CLP.getValue());
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);

    PrepaidMovement10 prepaidMovement1_1 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("deben ser iguales", prepaidMovement10, prepaidMovement1_1);
  }
}
