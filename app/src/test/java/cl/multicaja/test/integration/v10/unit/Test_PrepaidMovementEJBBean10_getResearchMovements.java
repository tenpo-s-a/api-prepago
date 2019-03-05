package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

public class Test_PrepaidMovementEJBBean10_getResearchMovements extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  @Test
  public void findMovement_ok() throws Exception {

    Timestamp dateOfTransaction = new Timestamp((new java.util.Date()).getTime());
    String fileName = "TestFile";
    String idFileOrigin = "idMov=3";
    Long movRef = new Long(0);

    getPrepaidMovementEJBBean10().createMovementResearch(
      null, idFileOrigin, ReconciliationOriginType.CLEARING_RESOLUTION, fileName, dateOfTransaction,
      ResearchMovementResponsibleStatusType.STATUS_UNDEFINED,ResearchMovementDescriptionType.ERROR_UNDEFINED,movRef);

    ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(idFileOrigin);

    Assert.assertNotNull("Debe existir", researchMovement10);
    Assert.assertEquals("Deben tener mismo id", idFileOrigin, researchMovement10.getIdFileOrigin());
    Assert.assertEquals("Debe tener la misma accion", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigin());
    Assert.assertEquals("Debe tener el mismo status", fileName, researchMovement10.getFileName());
    Assert.assertNotNull("Debe tener una fecha de creacion", researchMovement10.getCreatedAt());
    Assert.assertNotNull("Debe tener una fecha de transaccion",researchMovement10.getDateOfTransaction());
    Assert.assertEquals("Debe tener el mismo responsable ",ResearchMovementResponsibleStatusType.STATUS_UNDEFINED,researchMovement10.getResponsible());
    Assert.assertEquals("Debe tener la misma descripción ",ResearchMovementDescriptionType.ERROR_UNDEFINED,researchMovement10.getDescription());
    Assert.assertEquals("Debe tener el mismo movRef",movRef,researchMovement10.getMovRef());

  }

  @Test(expected = BadRequestException.class)
  public void findMovement_nullId() throws Exception {
    ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(null);
  }

  @Test
  public void findMovement_doesntExistId() throws Exception {

    Timestamp dateOfTransaction = new Timestamp((new java.util.Date()).getTime());
    String fileName = "TestFile";
    String idFileOrigin = "idMov=3";
    String idFileOriginNotFound = "idMov=4";
    Long movRef = new Long(0);

    getPrepaidMovementEJBBean10().createMovementResearch(
      null, idFileOrigin, ReconciliationOriginType.CLEARING_RESOLUTION, fileName,dateOfTransaction,
      ResearchMovementResponsibleStatusType.STATUS_UNDEFINED,ResearchMovementDescriptionType.ERROR_UNDEFINED,movRef);
    ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(idFileOriginNotFound);
    Assert.assertNull("No debe existir", researchMovement10);
  }
}
