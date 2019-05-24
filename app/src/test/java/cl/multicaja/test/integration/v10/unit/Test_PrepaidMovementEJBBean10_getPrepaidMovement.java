package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class Test_PrepaidMovementEJBBean10_getPrepaidMovement extends TestBaseUnit {

  private boolean contains(List<PrepaidMovement10> lst, PrepaidMovement10 prepaidMovement) {
    System.out.println(prepaidMovement);
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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement1 = buildPrepaidMovement11(prepaidUser, prepaidTopup,prepaidCard10);
    prepaidMovement1 = createPrepaidMovement11(prepaidMovement1);

    PrepaidMovement10 prepaidMovement2 = buildPrepaidMovement11(prepaidUser, prepaidTopup,prepaidCard10);
    prepaidMovement2 = createPrepaidMovement11(prepaidMovement2);


    List<PrepaidMovement10> lst = getPrepaidMovementEJBBean11().getPrepaidMovementByCardId(prepaidCard10.getId());

    Assert.assertNotNull("debe retornar una lista", lst);
    Assert.assertEquals("deben ser 2", 2, lst.size());

    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement1));
    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement2));

    lst = getPrepaidMovementEJBBean11().getPrepaidMovementByCardIdAndEstado(prepaidCard10.getId(), prepaidMovement1.getEstado());

    Assert.assertNotNull("debe retornar una lista", lst);
    Assert.assertEquals("deben ser 2", 2, lst.size());

    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement1));
    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement2));

    lst = getPrepaidMovementEJBBean11().getPrepaidMovementByCardIdAndTipoMovimiento(prepaidCard10.getId(), prepaidMovement1.getTipoMovimiento());

    Assert.assertNotNull("debe retornar una lista", lst);
    Assert.assertEquals("deben ser 2", 2, lst.size());

    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement1));
    Assert.assertTrue("debe contener el movimiento", contains(lst, prepaidMovement2));

    lst = getPrepaidMovementEJBBean11().getPrepaidMovementByCardIdAndEstado(prepaidUser.getId(), PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP);

    Assert.assertEquals("debe retornar una lista", 0,lst.size());

    PrepaidMovement10 prepaidMovement1_1 = getPrepaidMovementEJBBean11().getPrepaidMovementById(prepaidMovement1.getId());
    Assert.assertEquals("deben ser iguales", prepaidMovement1, prepaidMovement1_1);

    PrepaidMovement10 prepaidMovement1_2 = getPrepaidMovementEJBBean11().getPrepaidMovementById(prepaidMovement2.getId());
    Assert.assertEquals("deben ser iguales", prepaidMovement2, prepaidMovement1_2);

    PrepaidMovement10 prepaidMovement1_3 = getPrepaidMovementEJBBean11().getPrepaidMovementByIdTxExterno(prepaidMovement1_2.getIdTxExterno(), prepaidMovement1_2.getTipoMovimiento(), prepaidMovement1_2.getIndnorcor());
    Assert.assertEquals("deben ser iguales",  prepaidMovement1_2,prepaidMovement1_3);

    PrepaidMovement10 prepaidMovement2_1 = getPrepaidMovementEJBBean11().getPrepaidMovementForTecnocomReconciliationV2(prepaidCard10.getId(), prepaidMovement2.getNumaut(), new java.sql.Date(prepaidMovement2.getFecfac().getTime()), prepaidMovement2.getTipofac());
    Assert.assertEquals("deben ser iguales",  prepaidMovement2,prepaidMovement2_1);
  }

}
