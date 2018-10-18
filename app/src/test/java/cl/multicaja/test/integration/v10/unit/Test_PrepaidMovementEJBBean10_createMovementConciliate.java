package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidMovementEJBBean10_createMovementConciliate extends TestBaseUnit  {

  @Test
  public void createMovementConciliate() throws Exception {
    try {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);

    }catch (Exception e){
      e.printStackTrace();
      Assert.fail("No tiene que caer aca");
    }

  }
    @Test
  public void createMovementConciliateError(){
    try{
      getPrepaidMovementEJBBean10().createMovementConciliate(null,null,null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser idMovRef","idMovRef",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
    try{
      getPrepaidMovementEJBBean10().createMovementConciliate(null,getUniqueLong(),null,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser idMovRef","actionType",e.getData()[0].getValue());
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }

    try{
      getPrepaidMovementEJBBean10().createMovementConciliate(null,getUniqueLong(), ReconciliationActionType.CARGA,null);
      Assert.fail("No debe caer aca");
    }catch (BadRequestException e){
      Assert.assertTrue("Debe caer aca",true);
      Assert.assertEquals("Codigo debe ser 101004",Integer.valueOf(101004),e.getCode());
      Assert.assertEquals("Msj Debe ser idMovRef","statusType",e.getData()[0].getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }
      try{
        getPrepaidMovementEJBBean10().createMovementConciliate(null,getUniqueLong(), ReconciliationActionType.CARGA, ReconciliationStatusType.PENDING);
        Assert.fail("No debe caer aca");
      }catch (BadRequestException e){

      } catch (Exception e) {
        Assert.assertTrue("Debe caer aca",true);
      }
  }
}
