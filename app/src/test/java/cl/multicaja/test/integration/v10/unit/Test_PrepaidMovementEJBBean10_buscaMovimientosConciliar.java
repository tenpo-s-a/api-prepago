package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class Test_PrepaidMovementEJBBean10_buscaMovimientosConciliar  extends TestBaseUnit {

  @BeforeClass
  public static void beforeClass() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", getSchema()));
  }

  @AfterClass
  public static void afterClass() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", getSchema()));
  }

  @Test
  public void searchMovementForConciliate()throws Exception {
    {
      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertNull("Debe ser null",lstMovement10s);
    }
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ConciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ConciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);
      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 1", 1,lstMovement10s.size());
      getPrepaidMovementEJBBean10().createMovementConciliate(null,prepaidMovement10.getId(), ConciliationActionType.CARGA,ConciliationStatusType.RECONCILED);

      lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertNull("Debe ser null",lstMovement10s);

      // CREA MOVIMIENTOS
      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ConciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ConciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ConciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ConciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ConciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ConciliationStatusType.PENDING);
      createPrepaidMovement10(prepaidMovement10);

      lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 2 el tercero aun no esta revisado", 2,lstMovement10s.size());

    }
  }
}
