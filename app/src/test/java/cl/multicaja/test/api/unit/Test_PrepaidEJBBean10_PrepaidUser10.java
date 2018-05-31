package cl.multicaja.test.api.unit;

import cl.multicaja.core.exceptions.BaseException;
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

  @Test
  public void insertUserOk() throws Exception {
    PrepaidUser10 user = buildPrepaidUser();
    createPrepaidUser(user);
  }

  @Test
  public void insertUserNotOk() throws Exception {

    /**
     * Caso de registro de un nuevo usuario, pero que luego se intenta registrar el mismo y deberia fallar
     */

    PrepaidUser10 user = buildPrepaidUser();
    user = createPrepaidUser(user);

    //se intenta registrar exactamente el mismo usuario
    try {
      getPrepaidEJBBean10().createPrepaidUser(null, user);
    } catch(BaseException bex) {
      Assert.assertEquals("debe retornar excepcion de dato duplicado", Integer.valueOf(1), bex.getCode());
    }
  }

  @Test
  public void searchUserOk() throws Exception {

    /**
     * Caso en que se registra un nuevo usuario y luego se busca por su id, id_usuario_mc y rut
     */

    PrepaidUser10 user = buildPrepaidUser();
    user = createPrepaidUser(user);

    PrepaidUser10 u1 = getPrepaidEJBBean10().getPrepaidUserById(null, user.getId());

    Assert.assertNotNull("debe retornar un usuario", u1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u1);

    PrepaidUser10 u2 = getPrepaidEJBBean10().getPrepaidUserByUserIdMc(null, user.getIdUserMc());

    Assert.assertNotNull("debe retornar un usuario", u2);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u2);

    PrepaidUser10 u3 = getPrepaidEJBBean10().getPrepaidUserByRut(null, user.getRut());

    Assert.assertNotNull("debe retornar un usuario", u3);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u3);
  }

  @Test
  public void searchUsersOkByStatus() throws Exception {

    /**
     * Caso en que se registra un nuevo usuario y luego se busca por su id, id_usuario_mc y rut
     */

    PrepaidUser10 user1 = buildPrepaidUser();
    user1.setStatus(PrepaidUserStatus.DISABLED);
    user1 = createPrepaidUser(user1);

    PrepaidUser10 user2 = buildPrepaidUser();
    user2.setStatus(PrepaidUserStatus.DISABLED);
    user2 = createPrepaidUser(user2);

    List<PrepaidUser10> lst = getPrepaidEJBBean10().getPrepaidUsers(null, null, null, null, PrepaidUserStatus.DISABLED);

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

    PrepaidUser10 user = buildPrepaidUser();
    user = createPrepaidUser(user);

    getPrepaidEJBBean10().updatePrepaidUserStatus(null, user.getId(), PrepaidUserStatus.DISABLED);

    PrepaidUser10 u1 = getPrepaidEJBBean10().getPrepaidUserById(null, user.getId());

    Assert.assertNotNull("debe retornar un usuario", u1);
    Assert.assertEquals("el estado debe estar actualizado", PrepaidUserStatus.DISABLED, u1.getStatus());
  }
}
