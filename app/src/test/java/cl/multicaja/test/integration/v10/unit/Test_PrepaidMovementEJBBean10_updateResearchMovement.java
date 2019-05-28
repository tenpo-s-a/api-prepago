package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10_updateResearchMovement extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimiento_investigar CASCADE", getSchema()));
  }

  @Test
  public void testUpdateResearchMovement(){
    try {

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();

      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
      researchMovementInformationFiles.setIdEnArchivo("idEnArchivi_1");
      researchMovementInformationFiles.setNombreArchivo("nombreArchivo_1");
      researchMovementInformationFiles.setTipoArchivo("tipoArchivo_1");
      researchMovementInformationFilesList.add(researchMovementInformationFiles);

      Timestamp dateOfTransaction = new Timestamp(prepaidMovement10.getFechaCreacion().getTime());

      Map<String,Object> respCreate = getPrepaidMovementEJBBean10().createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.SWITCH.name(),
        dateOfTransaction.toLocalDateTime(),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        100L,
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue());

      Assert.assertNotNull("Data no debe ser null", respCreate);
      Assert.assertEquals("Debe ser 0","0",respCreate.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",respCreate.get("_error_msg"));

      Long idForUpdate = numberUtils.toLong(respCreate.get("_r_id"));
      String sentStatusChng = ResearchMovementSentStatusType.SENT_RESEARCH_OK.getValue();

      Map<String,Object> respUpdate = getPrepaidMovementEJBBean10().updateResearchMovement(
        idForUpdate,sentStatusChng);
      Assert.assertNotNull("Data no debe ser null", respUpdate);
      Assert.assertEquals("Debe ser 0","0",respUpdate.get("_error_code"));
      Assert.assertEquals("Deben ser iguales","",respUpdate.get("_error_msg"));


      ResearchMovement10 researchMovement = getPrepaidMovementEJBBean10().getResearchMovementById(idForUpdate);
      Assert.assertNotNull("Data no debe ser null", researchMovement);
      Assert.assertEquals("El sent_status es el mismo",sentStatusChng,researchMovement.getSentStatus().getValue());


    }catch (Exception e){
      e.printStackTrace();
      Assert.fail("No tiene que caer aca");
    }
  }


  @Test
  public void testUpdateResearchMovementCases(){

    try{
      getPrepaidMovementEJBBean10().updateResearchMovement(
        null,null);

      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser filesInfo","id",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().updateResearchMovement(
        null, ResearchMovementSentStatusType.SENT_RESEARCH_OK.getValue());
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser filesInfo","id",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().updateResearchMovement(
        0L, null);
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser filesInfo","sentStatus",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().updateResearchMovement(
        1L, null);
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser filesInfo","sentStatus",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().updateResearchMovement(
        -1L, null);
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser filesInfo","sentStatus",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

  }

}
