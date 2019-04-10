package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Test_PrepaidUserEJBBean11_createUserFromTenpo extends TestBaseUnit {

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_movimiento"));
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_usuario"));
  }

  @Test
  public void createUserOk() throws Exception{
    PrepaidUser10 user = buildPrepaidUser11();
    PrepaidUser10 userCreated = getPrepaidUserEJBBean10().createUser(null,user);
    PrepaidUser10 userFound = getPrepaidUserEJBBean10().findByExtId(null, userCreated.getUuid());

    Assert.assertNotNull("No es nulo", userFound);

    Assert.assertEquals("Igual",userCreated.getUuid(),userFound.getUuid());
    Assert.assertEquals("Igual",userCreated.getRut(),userFound.getRut());
    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userCreated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userCreated.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userCreated.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userCreated.getUserLevel(),userFound.getUserLevel());

  }

}
