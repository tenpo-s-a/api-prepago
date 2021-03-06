package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class Test_PrepaidMovementEJBBean10_buscaMovimientosConciliar  extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE  %s.prp_movimiento CASCADE", getSchema()));
  }

  @Test
  public void searchMovementForConciliate()throws Exception {
    {
      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean11().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser null",0,lstMovement10s.size());
    }
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement11(prepaidMovement10);

      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean11().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 1", 1,lstMovement10s.size());
      getPrepaidMovementEJBBean11().createMovementConciliate(null,prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);

      lstMovement10s = getPrepaidMovementEJBBean11().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser null",0,lstMovement10s.size());

      // CREA MOVIMIENTOS
      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement11(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      createPrepaidMovement11(prepaidMovement10);

      lstMovement10s = getPrepaidMovementEJBBean11().getMovementsForConciliate(null);
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
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    // se agrega un movmiento de carga
    {
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement11(prepaidMovement10);

      List<PrepaidMovement10> lstMovement10s = getPrepaidMovementEJBBean11().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 1", 1,lstMovement10s.size());

      getPrepaidMovementEJBBean11().createMovementConciliate(null,prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);

      lstMovement10s = getPrepaidMovementEJBBean11().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser null",0,lstMovement10s.size());

      // CREA MOVIMIENTOS
      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      createPrepaidMovement10(prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      createPrepaidMovement11(prepaidMovement10);

      // se agrega un movimiento de retiro web
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      PrepaidMovement10 prepaidWithdrawMovement = buildPrepaidMovement11(prepaidUser, prepaidWithdraw);

      prepaidWithdrawMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidWithdrawMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidWithdrawMovement.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      createPrepaidMovement10(prepaidWithdrawMovement);

      lstMovement10s = getPrepaidMovementEJBBean11().getMovementsForConciliate(null);
      Assert.assertEquals("Debe ser 2 el tercero aun no esta revisado", 2,lstMovement10s.size());
    }

  }
}
