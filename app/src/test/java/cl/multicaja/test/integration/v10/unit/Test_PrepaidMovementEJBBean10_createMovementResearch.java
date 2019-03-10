package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Test_PrepaidMovementEJBBean10_createMovementResearch extends TestBaseUnit {


  @Test
  public void testCreateReseachMovement(){
    try {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      ResearchMovementInformationFiles researchMovementInformationFiles = new ResearchMovementInformationFiles();
      researchMovementInformationFiles.setIdArchivo(Long.valueOf(1));
      researchMovementInformationFiles.setIdEnArchivo("idEnArchivi_1");
      researchMovementInformationFiles.setNombreArchivo("nombreArchivo_1");
      researchMovementInformationFiles.setTipoArchivo("tipoArchivo_1");

      Timestamp dateOfTransaction = new Timestamp(prepaidMovement10.getFechaCreacion().getTime());

      getPrepaidMovementEJBBean10().createResearchMovement(
        null,
        toJson(researchMovementInformationFiles),
        ReconciliationOriginType.SWITCH.name(),
        dateOfTransaction,
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
  public void createReseachMovementError(){
    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,null,null,null,
        null,null,null,null,null);

      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser idFileOrigin","filesInfo",e.getData()[0].getValue());
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
      Assert.assertEquals("Msj Debe ser fileName","dateOfTransaction",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
        null, null, null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser dateOfTransaction","responsible",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        null, null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser dateOfTransaction","description",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser responsible","movRef",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        100L,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser description","movementType",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

    try{
      getPrepaidMovementEJBBean10().createResearchMovement(
        null,getRandomString(10), ReconciliationOriginType.SWITCH.name(),
        Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))),
        ResearchMovementResponsibleStatusType.IS_TABLE.getValue(),
        ResearchMovementDescriptionType.ERROR_UNDEFINED.getValue(),
        100L,PrepaidMovementType.TOPUP.name(),null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser movRef","sentStatus",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
