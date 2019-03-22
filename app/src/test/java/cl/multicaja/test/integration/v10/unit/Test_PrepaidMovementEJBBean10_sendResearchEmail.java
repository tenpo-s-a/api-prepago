package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10_sendResearchEmail extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  //@Ignore
  @Test
  public void sendResearchEmail() throws Exception {

    ArrayList<ResearchMovement10> researchMovement10s = new ArrayList<>();
    List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
    ResearchMovementInformationFiles researchMovementInformationFiles;

    ZonedDateTime nowDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
    ZonedDateTime yesterdayDateTime = nowDateTime.minusDays(1);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String yesterdayString = yesterdayDateTime.format(formatter);

    Map<String, Object> resp;

    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
    researchMovementInformationFiles.setIdEnArchivo("1");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo1");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo1");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(2));
    researchMovementInformationFiles.setIdEnArchivo("2");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo2");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo2");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    Long movementId = 3L;
    resp = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFilesList),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
      ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
      ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
      movementId,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );
    changeResearch(numberUtils.toLong(resp.get("_r_id")), ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue(),"2015-01-01 00:00:00.0");
    ResearchMovement10 insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByMovRef(numberUtils.toBigDecimal(movementId)).get(0);
    researchMovement10s.add(insertedMovement);

    movementId = 4L;

    researchMovementInformationFilesList.clear();
    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(3));
    researchMovementInformationFiles.setIdEnArchivo("3");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo3");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo3");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    resp = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFilesList),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
      ResearchMovementResponsibleStatusType.RECONCIALITION_MULTICAJA_OTI.getValue(),
      ResearchMovementDescriptionType.ERROR_STATUS_IN_DB.getValue(),
      movementId,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );
    changeResearch(numberUtils.toLong(resp.get("_r_id")), ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue(), yesterdayString);
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByMovRef(numberUtils.toBigDecimal(movementId)).get(0);
    researchMovement10s.add(insertedMovement);

    movementId = 4L;
    researchMovementInformationFilesList.clear();
    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(4));
    researchMovementInformationFiles.setIdEnArchivo("4");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo4");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo4");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(5));
    researchMovementInformationFiles.setIdEnArchivo("5");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo5");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo5");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    resp = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFilesList),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
      ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID.getValue(),
      ResearchMovementDescriptionType.MOVEMENT_REJECTED_IN_AUTHORIZATION.getValue(),
      movementId,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );
    changeResearch(numberUtils.toLong(resp.get("_r_id")), ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue(), yesterdayString);
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByMovRef(numberUtils.toBigDecimal(movementId)).get(0);
    researchMovement10s.add(insertedMovement);

    movementId = 6L;

    researchMovementInformationFilesList.clear();
    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(6));
    researchMovementInformationFiles.setIdEnArchivo("6");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo6");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo6");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    resp = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFilesList),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
      ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
      ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
      movementId,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );
    changeResearch(numberUtils.toLong(resp.get("_r_id")), ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue(), "3015-01-01 00:00:00.0");
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByMovRef(numberUtils.toBigDecimal(movementId)).get(0);
    researchMovement10s.add(insertedMovement);

    movementId = 7L;

    researchMovementInformationFilesList.clear();
    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(7));
    researchMovementInformationFiles.setIdEnArchivo("7");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo7");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo7");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(8));
    researchMovementInformationFiles.setIdEnArchivo("8");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo8");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo8");
    researchMovementInformationFilesList.add(researchMovementInformationFiles);

    getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFilesList),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
      ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
      ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
      movementId,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementByMovRef(numberUtils.toBigDecimal(movementId)).get(0);
    researchMovement10s.add(insertedMovement);

    getPrepaidMovementEJBBean10().sendResearchEmail();
  }

  static public void changeResearch(Long idMovimiento, String sentStatus, String newDate)  {
    getDbUtils().getJdbcTemplate().execute(
      "UPDATE " + getSchema() + ".prp_movimiento_investigar SET " +
        "fecha_registro = " + "(TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone) ," +
        "sent_status = '"+sentStatus+"' "
        + "WHERE mov_ref = " + idMovimiento);
  }
}
