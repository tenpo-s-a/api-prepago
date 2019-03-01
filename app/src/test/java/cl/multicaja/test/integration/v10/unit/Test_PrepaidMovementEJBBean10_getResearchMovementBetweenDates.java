package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.ReconciliationOriginType;
import cl.multicaja.prepaid.model.v10.ResearchMovement10;
import cl.multicaja.prepaid.model.v10.ResearchMovementDescriptionType;
import cl.multicaja.prepaid.model.v10.ResearchMovementResponsibleStatusType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidMovementEJBBean10_getResearchMovementBetweenDates extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  @Test
  public void findMovement_ok() throws Exception {
    ArrayList<ResearchMovement10> researchMovement10s = new ArrayList<>();

    String movementId = "idMov=3";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, 0L);
    changeResearch(movementId, "2015-01-01 00:00:00.0");
    ResearchMovement10 insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    movementId = "idMov=4";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, 0L);
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    movementId = "idMov=5";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, 0L);
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    movementId = "idMov=6";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, 0L);
    changeResearch(movementId, "3015-01-01 00:00:00.0");
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);

    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    endDateTime = endDateTime.plusHours(1);

    List<ResearchMovement10> foundMovements = getPrepaidMovementEJBBean10().getResearchMovementBetweenDates(Timestamp.valueOf(beginDateTime), Timestamp.valueOf(endDateTime));
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 2 objetos", 2, foundMovements.size());
    int comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getIdFileOrigin().equals(foundMovement.getIdFileOrigin())) {
          Assert.assertEquals("Debe tener mismo filename", insMovement.getFileName(), foundMovement.getFileName());
          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("Debe tener origen", insMovement.getOrigin(), foundMovement.getOrigin());
          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 2 movements", 2, comparedMovements);

    foundMovements = getPrepaidMovementEJBBean10().getResearchMovementBetweenDates(null, Timestamp.valueOf(endDateTime));
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 3 objetos", 3, foundMovements.size());
    comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getIdFileOrigin().equals(foundMovement.getIdFileOrigin())) {
          Assert.assertEquals("Debe tener mismo filename", insMovement.getFileName(), foundMovement.getFileName());
          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("Debe tener origen", insMovement.getOrigin(), foundMovement.getOrigin());
          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 3 movements", 3, comparedMovements);

    foundMovements = getPrepaidMovementEJBBean10().getResearchMovementBetweenDates(Timestamp.valueOf(beginDateTime), null);
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 3 objetos", 3, foundMovements.size());
    comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getIdFileOrigin().equals(foundMovement.getIdFileOrigin())) {
          Assert.assertEquals("Debe tener mismo filename", insMovement.getFileName(), foundMovement.getFileName());
          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("Debe tener origen", insMovement.getOrigin(), foundMovement.getOrigin());
          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 3 movements", 3, comparedMovements);

    foundMovements = getPrepaidMovementEJBBean10().getResearchMovementBetweenDates(null, null);
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 4 objetos", 4, foundMovements.size());
    comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getIdFileOrigin().equals(foundMovement.getIdFileOrigin())) {
          Assert.assertEquals("Debe tener mismo filename", insMovement.getFileName(), foundMovement.getFileName());
          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("Debe tener origen", insMovement.getOrigin(), foundMovement.getOrigin());
          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 4 movements", 4, comparedMovements);
  }

  static public void changeResearch(String idMovimiento, String newDate)  {
    getDbUtils().getJdbcTemplate().execute(
      "UPDATE " + getSchema() + ".prp_movimiento_investigar SET fecha_registro = "
        + "(TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone) "
        + "WHERE id_archivo_origen = '" + idMovimiento + "'");
  }
}
