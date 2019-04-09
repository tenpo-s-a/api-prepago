package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.ERROR_DE_COMUNICACION_CON_BBDD;

public class Test_PrepaidUserEJBBean11_createUserFromTenpo extends TestBaseUnit {

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_movimiento"));
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_usuario"));
  }

  @Test
  public void createUserOk() throws Exception{
    PrepaidUser10 user = buildPrepaidUser11();
    PrepaidUser10 userCreated = getPrepaidUserEJBBean10().createPrepaidUserV10(null, user);
    PrepaidUser10 userFound = getPrepaidUserEJBBean10().findPrepaidUserV10(null, null,userCreated.getUuid(), null);

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

  @Test
  public void createUserNotOk() throws Exception{

    PrepaidUser10 user = buildPrepaidUser11();
    getPrepaidUserEJBBean10().createPrepaidUserV10(null, user);

    //se intenta registrar exactamente el mismo usuario
    try {

      getPrepaidUserEJBBean10().createPrepaidUserV10(null, user);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BaseException bex){
      Assert.assertEquals("debe retornar excepcion de dato duplicado", ERROR_DE_COMUNICACION_CON_BBDD.getValue(), bex.getCode());
    }

  }

}
