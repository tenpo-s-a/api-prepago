package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean10_createMovementResearch extends TestBaseUnit {
  @Test
  public void createMovementReseach() throws Exception {
    try {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      getPrepaidMovementEJBBean10().createMovementResearch(null, String.format("%s",prepaidMovement10.getId()), ReconciliationOriginType.SWITCH,"TABLE");

    }catch (Exception e){
      e.printStackTrace();
      Assert.fail("No tiene que caer aca");
    }

  }
  @Test
  public void createMovementReseachError(){
    try{
      getPrepaidMovementEJBBean10().createMovementResearch(null,null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      //Assert.assertEquals("Msj Debe ser idMovRef","movRef",e.getData()[0].getValue());
      Assert.assertEquals("Msj Debe ser idArchivoOrigen","id_archivo_origen",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
    try{
      getPrepaidMovementEJBBean10().createMovementResearch(null,getRandomString(10),null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser idMovRef","originType",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().createMovementResearch(null,getRandomString(10), ReconciliationOriginType.SWITCH,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser idMovRef","fileName",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
