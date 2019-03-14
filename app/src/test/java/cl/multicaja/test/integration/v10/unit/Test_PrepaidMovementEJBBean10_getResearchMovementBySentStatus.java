package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10_getResearchMovementBySentStatus extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }

  @Test
  public void findResearchMovementBySentStatusOk() throws Exception {


    ResearchMovement10 researchMovement;
    List<ResearchMovement10> researchMovementsSent = new ArrayList<>();

    ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
    researchMovementInformationFiles.setIdEnArchivo("idEnArchivi_1");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo_1");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo_1");

    String jsonSent = this.toJson(researchMovementInformationFiles);
    Long numRecords = 10L;

    for(int i=0;i<numRecords;i++){

      researchMovement = new ResearchMovement10();
      researchMovement.setDateOfTransaction(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
      researchMovement.setFilesInfo(jsonSent);
      researchMovement.setOriginType(ReconciliationOriginType.CLEARING_RESOLUTION);
      researchMovement.setResponsible(ResearchMovementResponsibleStatusType.STATUS_UNDEFINED);
      researchMovement.setDescription(ResearchMovementDescriptionType.ERROR_UNDEFINED);
      researchMovement.setMovRef(BigDecimal.valueOf(100));
      researchMovement.setMovementType(PrepaidMovementType.TOPUP);
      researchMovement.setSentStatus(ResearchMovementSentStatusType.SENT_RESEARCH_PENDING);

      Map<String, Object> resp = getPrepaidMovementEJBBean10().createResearchMovement(
        null,
        jsonSent,
        researchMovement.getOriginType().name(),
        researchMovement.getDateOfTransaction(),
        researchMovement.getResponsible().getValue(),
        researchMovement.getDescription().getValue(),
        researchMovement.getMovRef().longValue(),
        researchMovement.getMovementType().name(),
        researchMovement.getSentStatus().getValue()
      );

      researchMovement.setId(numberUtils.toLong(resp.get("_r_id")));
      researchMovementsSent.add(researchMovement);

      Assert.assertNotNull("Data no debe ser null", resp);
      Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));
    }

    List<ResearchMovement10> researchMovementsReturn = getPrepaidMovementEJBBean10().
      getResearchMovementBySentStatus(ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue());

    Assert.assertEquals("Se debe encontrar "+numRecords+" registros",numRecords,Long.valueOf(researchMovementsReturn.size()));
    System.out.println("testSearchResearchMovement_by_sentStatus: "+researchMovementsReturn.size());

    for(int i=0; i<researchMovementsReturn.size();i++){
      researchMovementsSent.sort(new IdSorterDesc());
      ResearchMovement10 rmSent = researchMovementsSent.get(i);
      ResearchMovement10 rmReturn = researchMovementsReturn.get(i);

      Assert.assertNotNull("No esta vacio ",rmReturn.getFilesInfo());
      Assert.assertEquals("El Json de informacionArchivos es el mismo ",jsonSent,rmReturn.getFilesInfo());
      Assert.assertEquals("El origen es el mismo ",rmSent.getOriginType(),rmReturn.getOriginType());
      Assert.assertEquals("El fechaDeTransaccion es el mismo ",rmSent.getDateOfTransaction(),rmReturn.getDateOfTransaction());
      Assert.assertEquals("El responsable es el mismo ",rmSent.getResponsible(),rmReturn.getResponsible());
      Assert.assertEquals("El descripcion es el mismo ",rmSent.getDescription(),rmReturn.getDescription());
      Assert.assertEquals("El movRef es el mismo ",rmSent.getMovRef(),rmReturn.getMovRef());
      Assert.assertEquals("El tipoMovimiento es el mismo ",rmSent.getMovementType(),rmReturn.getMovementType());
      Assert.assertEquals("El sentStatus es el mismo ",rmSent.getSentStatus(),rmReturn.getSentStatus());
    }
  }

  @Test
  public void findResearchMovementBySentStatusNotFound() throws Exception {

    ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
    researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
    researchMovementInformationFiles.setIdEnArchivo("idEnArchivi_1");
    researchMovementInformationFiles.setNombreArchivo("nombreArchivo_1");
    researchMovementInformationFiles.setTipoArchivo("tipoArchivo_1");

    ResearchMovement10 researchMovement = new ResearchMovement10();
    researchMovement.setDateOfTransaction(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
    researchMovement.setFilesInfo(toJson(researchMovementInformationFiles));
    researchMovement.setOriginType(ReconciliationOriginType.CLEARING_RESOLUTION);
    researchMovement.setResponsible(ResearchMovementResponsibleStatusType.STATUS_UNDEFINED);
    researchMovement.setDescription(ResearchMovementDescriptionType.ERROR_UNDEFINED);
    researchMovement.setMovRef(BigDecimal.valueOf(100));
    researchMovement.setMovementType(PrepaidMovementType.TOPUP);
    researchMovement.setSentStatus(ResearchMovementSentStatusType.SENT_RESEARCH_OK);


    Map<String, Object> resp = getPrepaidMovementEJBBean10().createResearchMovement(
      null,
      toJson(researchMovementInformationFiles),
      researchMovement.getOriginType().name(),
      researchMovement.getDateOfTransaction(),
      researchMovement.getResponsible().getValue(),
      researchMovement.getDescription().getValue(),
      researchMovement.getMovRef().longValue(),
      researchMovement.getMovementType().name(),
      researchMovement.getSentStatus().getValue()
    );

    Assert.assertNotNull("Data no debe ser null", resp);
    Assert.assertEquals("Debe ser 0","0",resp.get("_error_code"));
    Assert.assertEquals("Deben ser iguales","",resp.get("_error_msg"));

    List<ResearchMovement10> researchMovementsReturn = getPrepaidMovementEJBBean10().
      getResearchMovementBySentStatus(ResearchMovementSentStatusType.SENT_RESEARCH_OK.getValue()+"_TEST");
    Assert.assertEquals("Se debe encontrar 0 registros",0,researchMovementsReturn.size());

  }

  public class IdSorterDesc implements Comparator<ResearchMovement10>
  {
    @Override
    public int compare(ResearchMovement10 o1, ResearchMovement10 o2) {
      return o2.getId().compareTo(o1.getId());
    }
  }

}
