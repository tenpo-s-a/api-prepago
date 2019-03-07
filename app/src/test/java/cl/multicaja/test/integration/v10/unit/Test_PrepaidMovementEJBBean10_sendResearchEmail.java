package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Test_PrepaidMovementEJBBean10_sendResearchEmail extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  @Ignore
  @Test
  public void sendResearchEmail() throws Exception {
    ZonedDateTime nowDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
    ZonedDateTime yesterdayDateTime = nowDateTime.minusDays(1);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String yesterdayString = yesterdayDateTime.format(formatter);

    ArrayList<ResearchMovement10> researchMovement10s = new ArrayList<>();

    String movementId = "idMov=3";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, 0L);
    changeResearch(movementId, "2015-01-01 00:00:00.0");
    ResearchMovement10 insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    movementId = "idMov=4";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.RECONCIALITION_MULTICAJA_OTI, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB, 32L);
    changeResearch(movementId, yesterdayString);
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    movementId = "idMov=5";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "sin_archivo", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.MOVEMENT_REJECTED_IN_AUTHORIZATION, 57L);
    changeResearch(movementId, yesterdayString);
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    movementId = "idMov=6";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, 0L);
    changeResearch(movementId, "3015-01-01 00:00:00.0");
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    movementId = "idMov=7";
    getPrepaidMovementEJBBean10().createMovementResearch(null, movementId, ReconciliationOriginType.CLEARING_RESOLUTION, "archivo_test.txt", new Timestamp(System.currentTimeMillis()), ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, 0L);
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(movementId);
    researchMovement10s.add(insertedMovement);

    getPrepaidMovementEJBBean10().sendResearchEmail();
  }

  static public void changeResearch(String idMovimiento, String newDate)  {
    getDbUtils().getJdbcTemplate().execute(
      "UPDATE " + getSchema() + ".prp_movimiento_investigar SET fecha_registro = "
        + "(TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone) "
        + "WHERE id_archivo_origen = '" + idMovimiento + "'");
  }
}
