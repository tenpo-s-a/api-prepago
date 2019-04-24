package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean10_buscaMovimientosConciliados extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", getSchema()));
  }

  @Test
  public void findMovement_ok() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(prepaidMovement10.getId());

    Assert.assertNotNull("Debe existir", reconciliedMovement10);
    Assert.assertEquals("Deben tener mismo id", prepaidMovement10.getId(), reconciliedMovement10.getIdMovRef());
    Assert.assertEquals("Debe tener accion none", ReconciliationActionType.NONE, reconciliedMovement10.getActionType());
    Assert.assertEquals("Debe tener status reconciled", ReconciliationStatusType.RECONCILED, reconciliedMovement10.getReconciliationStatusType());
  }

  @Test(expected = BadRequestException.class)
  public void findMovement_nullId() throws Exception {
    ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(null);
  }

  @Test
  public void findMovement_doesntExistId() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(prepaidMovement10.getId() + 1L);

    Assert.assertNull("No debe existir", reconciliedMovement10);
  }
}
