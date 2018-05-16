package cl.multicaja.test.api.unit;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_PrepaidUser10 extends TestBaseUnit {

  private PrepaidEJBBean10 prepaidEJBBean10 = new PrepaidEJBBean10();

  public static PrepaidUser10 buildUser() {
    PrepaidUser10 user = new PrepaidUser10();
    user.setIdUserMc(getUniqueLong());
    user.setRut(getUniqueRutNumber());
    user.setStatus(PrepaidUserStatus.ACTIVE);
    return user;
  }

  private PrepaidUser10 createUser(PrepaidUser10 user) throws Exception {

    user = prepaidEJBBean10.createPrepaidUser(null, user);

    Assert.assertNotNull("debe retornar un usuario", user);
    Assert.assertEquals("debe tener id", true, user.getId() > 0);
    Assert.assertEquals("debe tener idUserMc", true, user.getIdUserMc() > 0);
    Assert.assertEquals("debe tener rut", true, user.getRut() > 0);
    Assert.assertNotNull("debe tener status", user.getStatus());

    return user;
  }

  @Test
  public void insertUserOk() throws Exception {
    PrepaidUser10 user = buildUser();
    createUser(user);
  }

  @Test
  public void insertUserNotOk() throws Exception {

    /**
     * Caso de registro de un nuevo usuario, pero que luego se intenta registrar el mismo y deberia fallar
     */

    PrepaidUser10 user = buildUser();
    user = createUser(user);

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

    PrepaidUser10 user = buildUser();
    user = createUser(user);

    PrepaidUser10 u1 = prepaidEJBBean10.getPrepaidUserById(null, user.getId());

    Assert.assertNotNull("debe retornar un usuario", u1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u1);

    PrepaidUser10 u2 = prepaidEJBBean10.getPrepaidUserByUserIdMc(null, user.getIdUserMc());

    Assert.assertNotNull("debe retornar un usuario", u2);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u2);

    PrepaidUser10 u3 = prepaidEJBBean10.getPrepaidUserByRut(null, user.getRut());

    Assert.assertNotNull("debe retornar un usuario", u3);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u3);
  }

  @Test
  public void searchUsersOkByStatus() throws Exception {

    /**
     * Caso en que se registra un nuevo usuario y luego se busca por su id, id_usuario_mc y rut
     */

    PrepaidUser10 user1 = buildUser();
    user1.setStatus(PrepaidUserStatus.DISABLED);
    user1 = createUser(user1);

    PrepaidUser10 user2 = buildUser();
    user2.setStatus(PrepaidUserStatus.DISABLED);
    user2 = createUser(user2);

    List<PrepaidUser10> lst = prepaidEJBBean10.getPrepaidUsers(null, null, null, null, PrepaidUserStatus.DISABLED);

    List<Long> lstFind = new ArrayList<>();

    for (PrepaidUser10 p : lst) {
      if (p.getId().equals(user1.getId()) || p.getId().equals(user2.getId())) {
        lstFind.add(p.getId());
      }
    }

    Assert.assertEquals("deben ser 2", 2 , lstFind.size());
    Assert.assertEquals("debe contener id", true, lstFind.contains(user1.getId()) && lstFind.contains(user2.getId()));
  }

  @Test
  public void updateStatusOk() throws Exception {

    PrepaidUser10 user = buildUser();
    user = createUser(user);

    prepaidEJBBean10.updatePrepaidUserStatus(null, user.getId(), PrepaidUserStatus.DISABLED);

    PrepaidUser10 u1 = prepaidEJBBean10.getPrepaidUserById(null, user.getId());

    Assert.assertNotNull("debe retornar un usuario", u1);
    Assert.assertEquals("el estado debe estar actualizado", PrepaidUserStatus.DISABLED, u1.getStatus());
  }
}
