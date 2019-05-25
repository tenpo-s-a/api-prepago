package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
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

public class Test_PrepaidMovementEJBBean10_createMovementResearch extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_investigar", getSchema()));
  }


  @Test
  public void testCreateResearchMovement(){
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

      getPrepaidMovementEJBBean10().createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.SWITCH.name(),
        dateOfTransaction.toLocalDateTime(),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        100L,
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue());


    }catch (Exception e){
      e.printStackTrace();
      Assert.fail("No tiene que caer aca");
    }

  }

  @Test
  public void createResearchMovementError(){

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,null,null,null,
        null,null,null,null,null);

      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser filesInfo","filesInfo",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), null,null,
        null,null,null,null,null);

      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser originType","originType",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        null, null, null, null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser dateOfTransaction","dateOfTransaction",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        LocalDateTime.now(ZoneId.of("UTC")),
        null, null, null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser responsible","responsible",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,
        getRandomString(10),
        ReconciliationOriginType.SWITCH.name(),
       LocalDateTime.now(ZoneId.of("UTC")),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        null, null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser description","description",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        LocalDateTime.now(ZoneId.of("UTC")),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser movRef","movRef",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        LocalDateTime.now(ZoneId.of("UTC")),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        100L,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser movementType","movementType",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        LocalDateTime.now(ZoneId.of("UTC")),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        100L,PrepaidMovementType.TOPUP.name(),null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser sentStatus","sentStatus",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
