package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10_getResearchMovementBetweenDates extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  @Test
  public void findResearchMovementByDateTimeRangeOk() throws Exception {

    ArrayList<ResearchMovement10> researchMovement10s = new ArrayList<>();

    ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
    researchMovementInformationFiles.setIdEnArchivo("idEnArchivi_1");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo_1");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo_1");

    Map<String,Object> rmReturn1 = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFiles),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      new Timestamp(System.currentTimeMillis()),
      ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
      ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
      10L,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );

    changeResearch(
      NumberUtils.getInstance().toLong(rmReturn1.get("_r_id")),
      ResearchMovementSentStatusType.SENT_RESEARCH_OK.getValue(),
      "2015-01-01 00:00:00.0");

    ResearchMovement10 insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementById(NumberUtils.getInstance().toLong(rmReturn1.get("_r_id")));
    researchMovement10s.add(insertedMovement);

    Map<String,Object> rmReturn2 = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFiles),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      new Timestamp(System.currentTimeMillis()),
      ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
      ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
      10L,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementById(NumberUtils.getInstance().toLong(rmReturn2.get("_r_id")));
    researchMovement10s.add(insertedMovement);

    Map<String,Object> rmReturn3 = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFiles),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      new Timestamp(System.currentTimeMillis()),
      ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
      ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
      10L,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );
    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementById(NumberUtils.getInstance().toLong(rmReturn3.get("_r_id")));
    researchMovement10s.add(insertedMovement);

    Map<String,Object> rmReturn4 = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFiles),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      new Timestamp(System.currentTimeMillis()),
      ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
      ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
      10L,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
    );

    changeResearch(
      NumberUtils.getInstance().toLong(rmReturn4.get("_r_id")),
      ResearchMovementSentStatusType.SENT_RESEARCH_OK.getValue(),
      "3015-01-01 00:00:00.0");

    insertedMovement = getPrepaidMovementEJBBean10().getResearchMovementById(NumberUtils.getInstance().toLong(rmReturn4.get("_r_id")));
    researchMovement10s.add(insertedMovement);

    LocalDateTime beginDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    beginDateTime = beginDateTime.minusHours(1);

    LocalDateTime endDateTime = LocalDateTime.now(ZoneId.of("UTC"));
    endDateTime = endDateTime.plusHours(1);

    List<ResearchMovement10> foundMovements = getPrepaidMovementEJBBean10().getResearchMovementByDateTimeRange(Timestamp.valueOf(beginDateTime), Timestamp.valueOf(endDateTime));
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 2 objetos", 2, foundMovements.size());
    int comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getId().equals(foundMovement.getId())) {

          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("El Json de informacionArchivos es el mismo ",insMovement.getFilesInfo(),foundMovement.getFilesInfo());
          Assert.assertEquals("El origen es el mismo ",insMovement.getOriginType(),foundMovement.getOriginType());
          Assert.assertEquals("El fechaDeTransaccion es el mismo ",insMovement.getDateOfTransaction(),foundMovement.getDateOfTransaction());
          Assert.assertEquals("El responsable es el mismo ",insMovement.getResponsible(),foundMovement.getResponsible());
          Assert.assertEquals("El descripcion es el mismo ",insMovement.getDescription(),foundMovement.getDescription());
          Assert.assertEquals("El movRef es el mismo ",insMovement.getMovRef(),foundMovement.getMovRef());
          Assert.assertEquals("El tipoMovimiento es el mismo ",insMovement.getMovementType(),foundMovement.getMovementType());
          Assert.assertEquals("El sentStatus es el mismo ",insMovement.getSentStatus(),foundMovement.getSentStatus());

          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 2 movements", 2, comparedMovements);

    foundMovements = getPrepaidMovementEJBBean10().getResearchMovementByDateTimeRange(null, Timestamp.valueOf(endDateTime));
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 3 objetos", 3, foundMovements.size());
    comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getId().equals(foundMovement.getId())) {

          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("El Json de informacionArchivos es el mismo ",insMovement.getFilesInfo(),foundMovement.getFilesInfo());
          Assert.assertEquals("El origen es el mismo ",insMovement.getOriginType(),foundMovement.getOriginType());
          Assert.assertEquals("El fechaDeTransaccion es el mismo ",insMovement.getDateOfTransaction(),foundMovement.getDateOfTransaction());
          Assert.assertEquals("El responsable es el mismo ",insMovement.getResponsible(),foundMovement.getResponsible());
          Assert.assertEquals("El descripcion es el mismo ",insMovement.getDescription(),foundMovement.getDescription());
          Assert.assertEquals("El movRef es el mismo ",insMovement.getMovRef(),foundMovement.getMovRef());
          Assert.assertEquals("El tipoMovimiento es el mismo ",insMovement.getMovementType(),foundMovement.getMovementType());
          Assert.assertEquals("El sentStatus es el mismo ",insMovement.getSentStatus(),foundMovement.getSentStatus());

          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 3 movements", 3, comparedMovements);

    foundMovements = getPrepaidMovementEJBBean10().getResearchMovementByDateTimeRange(Timestamp.valueOf(beginDateTime), null);
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 3 objetos", 3, foundMovements.size());
    comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getId().equals(foundMovement.getId())) {

          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("El Json de informacionArchivos es el mismo ",insMovement.getFilesInfo(),foundMovement.getFilesInfo());
          Assert.assertEquals("El origen es el mismo ",insMovement.getOriginType(),foundMovement.getOriginType());
          Assert.assertEquals("El fechaDeTransaccion es el mismo ",insMovement.getDateOfTransaction(),foundMovement.getDateOfTransaction());
          Assert.assertEquals("El responsable es el mismo ",insMovement.getResponsible(),foundMovement.getResponsible());
          Assert.assertEquals("El descripcion es el mismo ",insMovement.getDescription(),foundMovement.getDescription());
          Assert.assertEquals("El movRef es el mismo ",insMovement.getMovRef(),foundMovement.getMovRef());
          Assert.assertEquals("El tipoMovimiento es el mismo ",insMovement.getMovementType(),foundMovement.getMovementType());
          Assert.assertEquals("El sentStatus es el mismo ",insMovement.getSentStatus(),foundMovement.getSentStatus());

          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 3 movements", 3, comparedMovements);

    foundMovements = getPrepaidMovementEJBBean10().getResearchMovementByDateTimeRange(null, null);
    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe encontrar 4 objetos", 4, foundMovements.size());
    comparedMovements = 0;
    for(ResearchMovement10 insMovement : researchMovement10s) {
      for(ResearchMovement10 foundMovement : foundMovements) {
        if(insMovement.getId().equals(foundMovement.getId())) {

          Assert.assertEquals("Debe tener mismo id", insMovement.getId(), foundMovement.getId());
          Assert.assertEquals("El Json de informacionArchivos es el mismo ",insMovement.getFilesInfo(),foundMovement.getFilesInfo());
          Assert.assertEquals("El origen es el mismo ",insMovement.getOriginType(),foundMovement.getOriginType());
          Assert.assertEquals("El fechaDeTransaccion es el mismo ",insMovement.getDateOfTransaction(),foundMovement.getDateOfTransaction());
          Assert.assertEquals("El responsable es el mismo ",insMovement.getResponsible(),foundMovement.getResponsible());
          Assert.assertEquals("El descripcion es el mismo ",insMovement.getDescription(),foundMovement.getDescription());
          Assert.assertEquals("El movRef es el mismo ",insMovement.getMovRef(),foundMovement.getMovRef());
          Assert.assertEquals("El tipoMovimiento es el mismo ",insMovement.getMovementType(),foundMovement.getMovementType());
          Assert.assertEquals("El sentStatus es el mismo ",insMovement.getSentStatus(),foundMovement.getSentStatus());

          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 4 movements", 4, comparedMovements);

  }

  static public void changeResearch(Long id, String sentStatus, String newDate)  {
    getDbUtils().getJdbcTemplate().execute(
      "UPDATE " + getSchema() + ".prp_movimiento_investigar SET " +
        "fecha_registro = " + "(TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone) ," +
        "sent_status = '"+sentStatus+"' "
        + "WHERE id = " + id);
  }
}
