package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.IpmMovement10;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


public class Test_IpmEJBBean10_updateIpmMovementReconciledStatus extends TestBaseUnit {
  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file_data CASCADE", getSchemaAccounting()));
  }

  @Test
  public void updateIpmMovementReconciledStatus_changesReconciledStatusToTrue() throws Exception {
    IpmMovement10 insertedMovement = buildIpmMovement10();
    insertedMovement.setReconciled(false);
    insertedMovement = createIpmMovement(insertedMovement);
    Assert.assertFalse("Debe haberse guardado como reconciled false", insertedMovement.getReconciled());

    // Actualiza estado reconciliado a true
    getIpmEJBBean10().updateIpmMovementReconciledStatus(insertedMovement.getId(), true);

    IpmMovement10 foundMovement = getIpmMovementById(insertedMovement.getId());
    Assert.assertTrue("Su nuevo estado debe ser reconciled true", foundMovement.getReconciled());
  }

  @Test(expected = BadRequestException.class)
  public void updateIpmMovementReconciledStatus_idNull() throws Exception {
    getIpmEJBBean10().updateIpmMovementReconciledStatus(null, true);
  }
}
