package cl.multicaja.test.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @autor vutreras
 */
public class Test_PrepaidUserEJBBean10_getPrepaidUser extends TestBaseUnit {

  @Test
  public void getPrepaidUserById_getPrepaidUserByUserIdMc_getPrepaidUserByRut_ok() throws Exception {

    /**
     * Caso en que se registra un nuevo usuario y luego se busca por su id, id_usuario_mc y rut
     */

    PrepaidUser10 user = buildPrepaidUser10();
    user = createPrepaidUser10(user);

    PrepaidUser10 u1 = getPrepaidUserEJBBean10().getPrepaidUserById(null, user.getId());

    Assert.assertNotNull("debe retornar un usuario", u1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u1);

    PrepaidUser10 u2 = getPrepaidUserEJBBean10().getPrepaidUserByUserIdMc(null, user.getUserIdMc());

    Assert.assertNotNull("debe retornar un usuario", u2);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u2);

    PrepaidUser10 u3 = getPrepaidUserEJBBean10().getPrepaidUserByRut(null, user.getRut());

    Assert.assertNotNull("debe retornar un usuario", u3);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", user, u3);
  }

  @Test
  public void getPrepaidUsers_ok_by_status() throws Exception {

    /**
     * Caso en que se registra un nuevo usuario y luego se busca por su id, id_usuario_mc y rut
     */

    PrepaidUser10 user1 = buildPrepaidUser10();
    user1.setStatus(PrepaidUserStatus.DISABLED);
    user1 = createPrepaidUser10(user1);

    PrepaidUser10 user2 = buildPrepaidUser10();
    user2.setStatus(PrepaidUserStatus.DISABLED);
    user2 = createPrepaidUser10(user2);

    List<PrepaidUser10> lst = getPrepaidUserEJBBean10().getPrepaidUsers(null, null, null, null, PrepaidUserStatus.DISABLED);

    List<Long> lstFind = new ArrayList<>();

    for (PrepaidUser10 p : lst) {
      if (p.getId().equals(user1.getId()) || p.getId().equals(user2.getId())) {
        lstFind.add(p.getId());
      }
    }

    Assert.assertEquals("deben ser 2", 2 , lstFind.size());
    Assert.assertTrue("debe contener id", lstFind.contains(user1.getId()) && lstFind.contains(user2.getId()));
  }
}
