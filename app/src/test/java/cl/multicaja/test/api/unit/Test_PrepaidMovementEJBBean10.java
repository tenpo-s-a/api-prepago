package cl.multicaja.test.api.unit;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10 extends TestBaseUnit {

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
  public void testeEjbAddMovement() throws Exception {

    User user = registerUser();

    System.out.println("user: " + user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    System.out.println("prepaidTopup: " + prepaidTopup);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(prepaidUser, prepaidTopup);

    createPrepaidMovement(prepaidMovement10);
  }

  @Test
  public void test_updatePrepaidMovement_estado() throws Exception {

    User user = registerUser();

    System.out.println("user: " + user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    System.out.println("prepaidTopup: " + prepaidTopup);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(prepaidUser, prepaidTopup);

    prepaidMovement10 = createPrepaidMovement(prepaidMovement10);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement10.getId(), PrepaidMovementStatus.IN_PROCESS);

    prepaidMovement10.setEstado(PrepaidMovementStatus.IN_PROCESS);

    PrepaidMovement10 prepaidMovement1_1 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("deben ser iguales", prepaidMovement10, prepaidMovement1_1);
  }

  @Test
  public void test_updatePrepaidMovement() throws Exception {

    User user = registerUser();

    System.out.println("user: " + user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    System.out.println("prepaidTopup: " + prepaidTopup);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(prepaidUser, prepaidTopup);

    prepaidMovement10 = createPrepaidMovement(prepaidMovement10);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement10.getId(),1,2, CodigoMoneda.CHILE_CLP.getValue(), PrepaidMovementStatus.PROCESS_OK);

    prepaidMovement10.setNumextcta(1);
    prepaidMovement10.setNummovext(2);
    prepaidMovement10.setClamone(CodigoMoneda.CHILE_CLP.getValue());
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);

    PrepaidMovement10 prepaidMovement1_1 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("deben ser iguales", prepaidMovement10, prepaidMovement1_1);
  }

  @Test
  public void teste_getPrepaidMovements() throws Exception {

    User user = registerUser();

    System.out.println("user: " + user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    System.out.println("prepaidTopup: " + prepaidTopup);

    PrepaidMovement10 prepaidMovement1 = buildPrepaidMovement(prepaidUser, prepaidTopup);

    prepaidMovement1 = createPrepaidMovement(prepaidMovement1);

    PrepaidMovement10 prepaidMovement2 = buildPrepaidMovement(prepaidUser, prepaidTopup);

    prepaidMovement2 = createPrepaidMovement(prepaidMovement2);

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
  }
}
