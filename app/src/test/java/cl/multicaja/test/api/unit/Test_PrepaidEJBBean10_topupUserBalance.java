package cl.multicaja.test.api.unit;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.NewPrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @autor vutreras
 */
@Ignore
public class Test_PrepaidEJBBean10_topupUserBalance extends TestBaseUnit {

  @Test
  public void topupUserBalance_userNotFound() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    newPrepaidTopup.setRut(1);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario", Integer.valueOf(102001), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_prepaidUserNotFound() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario prepago", Integer.valueOf(102003), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_invalidCardByCardLockedhard() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", Integer.valueOf(106000), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_invalidCardByCardExpire() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", Integer.valueOf(106000), vex.getCode());
    }
  }
}
