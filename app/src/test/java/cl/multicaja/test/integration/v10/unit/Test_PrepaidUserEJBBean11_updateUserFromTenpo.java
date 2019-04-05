package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidUser11;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Test_PrepaidUserEJBBean11_updateUserFromTenpo extends TestBaseUnit {

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_movimiento"));
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_usuario"));
  }

  @Test
  public void updateUserOk() throws Exception{
    PrepaidUser11 user = buildPrepaidUser11();

    PrepaidUser11 userCreated = getPrepaidUserEJBBean10().createPrepaidUserV11(null,user);
    Assert.assertNotNull("No debe ser null",userCreated);
    Assert.assertNotNull("No debe ser null",userCreated.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,userCreated.getId().longValue());

    Integer newRut = getUniqueRutNumber();
    userCreated.setRut(newRut);
    PrepaidUser11 userUpdated = getPrepaidUserEJBBean10().updatePrepaidUserV11(null,userCreated);

    PrepaidUser11 userFound = getPrepaidUserEJBBean10().findPrepaidUserV11(null,null,userUpdated.getUuid(), null);

    Assert.assertNotNull("No es nulo", userFound);

    Assert.assertEquals("Igual",userUpdated.getRut(),userFound.getRut());
    Assert.assertEquals("Igual",userUpdated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userUpdated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userUpdated.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userUpdated.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userUpdated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userUpdated.getLevel(),userFound.getLevel());
    Assert.assertEquals("Igual",userUpdated.getUuid(),userFound.getUuid());

  }

}
