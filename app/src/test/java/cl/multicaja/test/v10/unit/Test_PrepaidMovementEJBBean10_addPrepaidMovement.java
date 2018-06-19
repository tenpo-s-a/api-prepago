package cl.multicaja.test.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.users.model.v10.User;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean10_addPrepaidMovement extends TestBaseUnit {

  @Test
  public void addPrepaidMovement_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    createPrepaidMovement10(prepaidMovement10);
  }
}
