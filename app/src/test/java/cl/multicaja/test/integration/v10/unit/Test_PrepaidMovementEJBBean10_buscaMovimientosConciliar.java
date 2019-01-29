package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.*;

import java.util.List;

public class Test_PrepaidMovementEJBBean10_buscaMovimientosConciliar  extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
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
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);
      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 1", 1,lstMovement10s.size());
      getPrepaidMovementEJBBean10().createMovementConciliate(null,prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);

      lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertNull("Debe ser null",lstMovement10s);

      // CREA MOVIMIENTOS
      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      createPrepaidMovement10(prepaidMovement10);

      lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 2 el tercero aun no esta revisado", 2,lstMovement10s.size());

    }
  }

  /**
   * Verifica que los retiros web se excluyen para conciliar por esta funcion
   * @throws Exception
   */
  @Test
  public void searchMovementForConciliate_webWithdraw()throws Exception {
    {
      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertNull("Debe ser null",lstMovement10s);
    }
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // se agrega un movmiento de carga
    {
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 1", 1,lstMovement10s.size());

      getPrepaidMovementEJBBean10().createMovementConciliate(null,prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);

      lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertNull("Debe ser null",lstMovement10s);

      // CREA MOVIMIENTOS
      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      createPrepaidMovement10(prepaidMovement10);

      // se agrega un movimiento de retiro web
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      PrepaidMovement10 prepaidWithdrawMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);

      prepaidWithdrawMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidWithdrawMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidWithdrawMovement);

      lstMovement10s = getPrepaidMovementEJBBean10().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 2 el tercero aun no esta revisado", 2,lstMovement10s.size());
    }

  }
}
