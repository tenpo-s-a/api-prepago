package cl.multicaja.test.api.unit;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10 extends TestBaseUnit {

  @Inject
  private PrepaidEJBBean10 prepaidEJBBean10 = new PrepaidEJBBean10();

  private PrepaidUser10 createUser() throws Exception {
    PrepaidUser10 user = new PrepaidUser10();
    user.setIdUser(new Long(getUniqueInteger()));
    user.setRut(getUniqueRutNumber());
    user.setStatus(PrepaidUserStatus.ACTIVE);
    return user;
  }

  @Test
  public void insertUserOk() throws Exception {

    /**
     * Caso de registro de un nuevo usuario
     */

    PrepaidUser10 user = createUser();

    user = prepaidEJBBean10.createPrepaidUser(null, user);

    Assert.assertNotNull("debe retornar un usuario", user);
    Assert.assertEquals("debe tener id", true, user.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, user.getIdUser() > 0);
    Assert.assertEquals("debe tener rut", true, user.getRut() > 0);
    Assert.assertNotNull("debe tener status", user.getStatus());
  }

  @Test
  public void insertUserNotOk() throws Exception {

    /**
     * Caso de registro de un nuevo usuario, pero que luego se intenta registrar el mismo y deberia fallar
     */

    PrepaidUser10 user = createUser();

    user = prepaidEJBBean10.createPrepaidUser(null, user);

    Assert.assertNotNull("debe retornar un usuario", user);
    Assert.assertEquals("debe tener id", true, user.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, user.getIdUser() > 0);
    Assert.assertEquals("debe tener rut", true, user.getRut() > 0);
    Assert.assertNotNull("debe tener status", user.getStatus());

    //se intenta registrar exactamente el mismo usuario
    try {
      user = prepaidEJBBean10.createPrepaidUser(null, user);
    } catch(BaseException bex) {
      Assert.assertEquals("debe retornar excepcion de dato duplicado", Integer.valueOf(1), bex.getCode());
    }
  }

  @Test
  public void searchUserOk() throws Exception {

    /**
     * Caso en que se registra un nuevo usuario y luego se busca por su id, id_usuario_mc y rut
     */

    PrepaidUser10 user = createUser();

    user = prepaidEJBBean10.createPrepaidUser(null, user);

    Assert.assertNotNull("debe retornar un usuario", user);
    Assert.assertEquals("debe tener id", true, user.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, user.getIdUser() > 0);
    Assert.assertEquals("debe tener rut", true, user.getRut() > 0);
    Assert.assertNotNull("debe tener status", user.getStatus());

    PrepaidUser10 u1 = prepaidEJBBean10.getPrepaidUserById(null, user.getId());

    Assert.assertNotNull("debe retornar un usuario", u1);
    Assert.assertEquals("debe tener id y ser igual al registrado anteriormemte", user.getId(), u1.getId());
    Assert.assertEquals("debe tener idUser y ser igual al registrado anteriormemte", user.getIdUser(), u1.getIdUser());
    Assert.assertEquals("debe tener rut y ser igual al registrado anteriormemte", user.getRut(), u1.getRut());
    Assert.assertEquals("debe tener status y ser igual al registrado anteriormemte", user.getStatus(), u1.getStatus());

    PrepaidUser10 u2 = prepaidEJBBean10.getPrepaidUserByUserIdMc(null, user.getIdUser());

    Assert.assertNotNull("debe retornar un usuario", u2);
    Assert.assertEquals("debe tener id y ser igual al registrado anteriormemte", user.getId(), u2.getId());
    Assert.assertEquals("debe tener idUser y ser igual al registrado anteriormemte", user.getIdUser(), u2.getIdUser());
    Assert.assertEquals("debe tener rut y ser igual al registrado anteriormemte", user.getRut(), u2.getRut());
    Assert.assertEquals("debe tener status y ser igual al registrado anteriormemte", user.getStatus(), u2.getStatus());

    PrepaidUser10 u3 = prepaidEJBBean10.getPrepaidUserByRut(null, user.getRut());

    Assert.assertNotNull("debe retornar un usuario", u3);
    Assert.assertEquals("debe tener id y ser igual al registrado anteriormemte", user.getId(), u3.getId());
    Assert.assertEquals("debe tener idUser y ser igual al registrado anteriormemte", user.getIdUser(), u3.getIdUser());
    Assert.assertEquals("debe tener rut y ser igual al registrado anteriormemte", user.getRut(), u3.getRut());
    Assert.assertEquals("debe tener status y ser igual al registrado anteriormemte", user.getStatus(), u3.getStatus());
  }

  @Test
  public void searchUserOkByStatus() throws Exception {

    /**
     * Caso en que se registra un nuevo usuario y luego se busca por su id, id_usuario_mc y rut
     */

    PrepaidUser10 user1 = createUser();
    user1.setStatus(PrepaidUserStatus.DISABLED);
    user1 = prepaidEJBBean10.createPrepaidUser(null, user1);

    PrepaidUser10 user2 = createUser();
    user2.setStatus(PrepaidUserStatus.DISABLED);
    user2 = prepaidEJBBean10.createPrepaidUser(null, user2);

    List<PrepaidUser10> lst = prepaidEJBBean10.getPrepaidUsers(null, null, null, null, PrepaidUserStatus.DISABLED);

    List<Long> lstFind = new ArrayList<>();

    for (PrepaidUser10 p : lst) {
      if (p.getId() == user1.getId() || p.getId() == user2.getId()) {
        lstFind.add(p.getId());
      }
    }

    //Assert.assertEquals("deben ser 2", 2 , lstFind.size());
    //Assert.assertEquals("debe contener id", true, lstFind.contains(user1.getId()) && lstFind.contains(user2.getId()));
  }
}
