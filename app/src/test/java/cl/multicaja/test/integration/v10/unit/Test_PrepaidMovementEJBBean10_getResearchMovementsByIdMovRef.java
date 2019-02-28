package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean10_getResearchMovementsByIdMovRef extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  @Test
  public void findMovement_ok() throws Exception {
    getPrepaidMovementEJBBean10().createMovementResearch(null, "idMov=3", ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt");

    ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef("idMov=3");

    Assert.assertNotNull("Debe existir", researchMovement10);
    Assert.assertEquals("Deben tener mismo id", "idMov=3", researchMovement10.getIdRef());
    Assert.assertEquals("Debe tener accion none", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    Assert.assertEquals("Debe tener status reconciled", "archivo_test.txt", researchMovement10.getFileName());
    Assert.assertNotNull("Debe tener una fecha de creacion", researchMovement10.getCreatedAt());
  }

  @Test(expected = BadRequestException.class)
  public void findMovement_nullId() throws Exception {
    ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(null);
  }

  @Test
  public void findMovement_doesntExistId() throws Exception {
    getPrepaidMovementEJBBean10().createMovementResearch(null, "idMov=3", ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt");
    ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef("idMov=4");
    Assert.assertNull("No debe existir", researchMovement10);
  }
}
