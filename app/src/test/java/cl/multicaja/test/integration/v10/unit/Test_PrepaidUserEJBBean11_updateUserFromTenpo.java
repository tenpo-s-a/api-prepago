package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidUser10;
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
    PrepaidUser10 user = buildPrepaidUser11();

    PrepaidUser10 userCreated = getPrepaidUserEJBBean10().createUser(null,user);
    Assert.assertNotNull("No debe ser null",userCreated);
    Assert.assertNotNull("No debe ser null",userCreated.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,userCreated.getId().longValue());


    PrepaidUser10 userUpdated = getPrepaidUserEJBBean10().updatePrepaidUser(null,userCreated);

    PrepaidUser10 userFound = getPrepaidUserEJBBean10().findByExtId(null,userUpdated.getUuid());

    Assert.assertNotNull("No es nulo", userFound);
    Assert.assertEquals("Igual",userUpdated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userUpdated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userUpdated.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userUpdated.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userUpdated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userUpdated.getUserLevel(),userFound.getUserLevel());
    Assert.assertEquals("Igual",userUpdated.getUuid(),userFound.getUuid());

  }

}
