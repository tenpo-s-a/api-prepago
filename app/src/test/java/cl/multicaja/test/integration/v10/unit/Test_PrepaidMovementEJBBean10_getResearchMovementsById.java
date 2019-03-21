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
import java.util.Map;

public class Test_PrepaidMovementEJBBean10_getResearchMovementsById extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  @Test
  public void findResearchMovementByIdOk() throws Exception {


    Timestamp dateOfTransaction = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));
    Long movRef = 100L;

    ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
    researchMovementInformationFiles.setIdEnArchivo("idEnArchivi_1");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo_1");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo_1");


    Map<String,Object> rmReturn = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFiles),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
      ResearchMovementResponsibleStatusType.STATUS_UNDEFINED.getValue(),
      ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
      movRef,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue());

    ResearchMovement10 researchMovement = getPrepaidMovementEJBBean10().getResearchMovementById(NumberUtils.getInstance().toLong(rmReturn.get("_r_id")));

    Assert.assertNotNull("No esta vacio ",researchMovement.getFilesInfo());
    Assert.assertEquals("El Json de informacionArchivos es el mismo ",toJson(researchMovementInformationFiles),researchMovement.getFilesInfo());
    Assert.assertEquals("El origen es el mismo ",ReconciliationOriginType.CLEARING_RESOLUTION.name(),researchMovement.getOriginType().name());
    Assert.assertTrue("La fechaDeTransaccion debe ser aproximadamente la misma", Math.abs(dateOfTransaction.getTime() - researchMovement.getDateOfTransaction().getTime()) < 1000);
    Assert.assertEquals("El responsable es el mismo ",ResearchMovementResponsibleStatusType.STATUS_UNDEFINED.getValue(),researchMovement.getResponsible().getValue());
    Assert.assertEquals("El descripcion es el mismo ",ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),researchMovement.getDescription().getValue());
    Long movRefReturn = researchMovement.getMovRef().longValue();
    Assert.assertEquals("El movRef es el mismo ",movRef,movRefReturn);
    Assert.assertEquals("El tipoMovimiento es el mismo ",PrepaidMovementType.TOPUP.name(),researchMovement.getMovementType().name());
    Assert.assertEquals("El sentStatus es el mismo ",ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue(),researchMovement.getSentStatus().getValue());

  }

  //@Test(expected = BadRequestException.class)
  @Test //Se espera que retorne un objeto null, no una excepción.
  public void findResearchMovementByNullId() throws Exception {
    ResearchMovement10 researchMovement = getPrepaidMovementEJBBean10().getResearchMovementById(null);
    Assert.assertNull("Es nulo",researchMovement);
  }

  @Test
  public void findResearchMovementNotFoundId() throws Exception {

    Long idNotFound = NumberUtils.getInstance().toLong(100);
    Timestamp dateOfTransaction = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));
    Long movRef = 100L;

    ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
    researchMovementInformationFiles.setIdEnArchivo("idEnArchivi_1");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo_1");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo_1");

    getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFiles),
      ReconciliationOriginType.CLEARING_RESOLUTION.name(),
      dateOfTransaction,
      ResearchMovementResponsibleStatusType.STATUS_UNDEFINED.getValue(),
      ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
      movRef,
      PrepaidMovementType.TOPUP.name(),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue());


    ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementById(idNotFound);
    Assert.assertNull("No debe existir", researchMovement10);
  }
}
