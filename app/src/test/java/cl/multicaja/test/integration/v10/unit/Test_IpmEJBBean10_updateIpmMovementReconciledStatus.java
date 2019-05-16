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
    insertIpmMovement(insertedMovement);
    insertedMovement = getIpmEJBBean10().findByReconciliationSimilarity(insertedMovement.getPan(), insertedMovement.getMerchantCode(), insertedMovement.getTransactionAmount(), insertedMovement.getApprovalCode());
    Assert.assertFalse("Debe haberse guardado como reconciled false", insertedMovement.getReconciled());

    // Actualiza estado reconciliado a true
    getIpmEJBBean10().updateIpmMovementReconciledStatus(insertedMovement.getId(), true);

    IpmMovement10 foundMovement = selectIpmMovement(insertedMovement.getId());
    Assert.assertTrue("Su nuevo estado debe ser reconciled true", foundMovement.getReconciled());
  }

  @Test(expected = BadRequestException.class)
  public void updateIpmMovementReconciledStatus_idNull() throws Exception {
    getIpmEJBBean10().updateIpmMovementReconciledStatus(null, true);
  }

  void insertIpmMovement(IpmMovement10 ipmMovement10) throws Exception {
    new Test_IpmEJBBean10_findByReconciliationSimilarity().insertIpmMovement(ipmMovement10);
  }

  public IpmMovement10 selectIpmMovement(Long id) {
    List<IpmMovement10> ipmMovement10List = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.ipm_file_data WHERE id = %s", getSchemaAccounting(), id), getIpmEJBBean10().getIpmMovementMapper());
    return ipmMovement10List != null && !ipmMovement10List.isEmpty() ? ipmMovement10List.get(0) : null;
  }
}
