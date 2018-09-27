package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class Test_PrepaidMovementEJBBean10_getPrepaidMovement extends TestBaseUnit {

  private boolean contains(List<PrepaidMovement10> lst, PrepaidMovement10 prepaidMovement) {
    for (int j = 0; j < lst.size(); j++) {
      boolean equals = lst.get(j).equals(prepaidMovement);
      System.out.println("---------------" + j + "---------------");
      System.out.println("equals: " + equals);
      System.out.println(lst.get(j));
      System.out.println(prepaidMovement);
      System.out.println("--------------------------------");
      if (equals) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void getPrepaidMovement() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement1 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    prepaidMovement1 = createPrepaidMovement10(prepaidMovement1);

    PrepaidMovement10 prepaidMovement2 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    prepaidMovement2 = createPrepaidMovement10(prepaidMovement2);

    List<PrepaidMovement10> lst = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUser(prepaidUser.getId());

    Assert.assertNotNull("debe retornar una lista", lst);
    Assert.assertEquals("deben ser 2", 2, lst.size());

    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement1));
    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement2));

    lst = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndEstado(prepaidUser.getId(), prepaidMovement1.getEstado());

    Assert.assertNotNull("debe retornar una lista", lst);
    Assert.assertEquals("deben ser 2", 2, lst.size());

    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement1));
    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement2));

    lst = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento(prepaidUser.getId(), prepaidMovement1.getTipoMovimiento());

    Assert.assertNotNull("debe retornar una lista", lst);
    Assert.assertEquals("deben ser 2", 2, lst.size());

    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement1));
    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement2));

    lst = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndEstado(prepaidUser.getId(), PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP);

    Assert.assertNull("debe retornar una lista", lst);

    PrepaidMovement10 prepaidMovement1_1 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement1.getId());
    Assert.assertEquals("deben ser iguales", prepaidMovement1, prepaidMovement1_1);

    PrepaidMovement10 prepaidMovement1_2 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement2.getId());
    Assert.assertEquals("deben ser iguales", prepaidMovement2, prepaidMovement1_2);

    PrepaidMovement10 prepaidMovement1_3 = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidMovement1_2.getIdTxExterno(),prepaidMovement1_2.getTipoMovimiento());
    Assert.assertEquals("deben ser iguales",  prepaidMovement1_2,prepaidMovement1_3);

  }
}
