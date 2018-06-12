package cl.multicaja.test.v10.unit;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.PrepaidBalance10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserBalance10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @autor vutreras
 */
public class Test_PrepaidUserEJBBean10 extends TestBaseUnit {

  @Test
  public void createPrepaidUser_ok() throws Exception {
    PrepaidUser10 user = buildPrepaidUser10();
    createPrepaidUser10(user);
  }

  @Test
  public void createPrepaidUser_not_ok() throws Exception {

    /**
     * Caso de registro de un nuevo usuario, pero que luego se intenta registrar el mismo y deberia fallar
     */

    PrepaidUser10 user = buildPrepaidUser10();
    user = createPrepaidUser10(user);

    //se intenta registrar exactamente el mismo usuario
    try {
      getPrepaidUserEJBBean10().createPrepaidUser(null, user);
    } catch(BaseException bex) {
      Assert.assertEquals("debe retornar excepcion de dato duplicado", Integer.valueOf(1), bex.getCode());
    }
  }

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

    PrepaidUser10 u2 = getPrepaidUserEJBBean10().getPrepaidUserByUserIdMc(null, user.getIdUserMc());

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

  @Test
  public void updatePrepaidUserStatus_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());

    getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser10.getId(), PrepaidUserStatus.DISABLED);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser DISABLED", PrepaidUserStatus.DISABLED, prepaidUser10.getStatus());
  }

  @Test
  public void updatePrepaidUserStatus_not_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());

    try {
      getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser10.getId(), null);
    } catch(ValidationException vex) {
      Assert.assertEquals("debe se error 101004", Integer.valueOf(101004), vex.getCode());
    }

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());
  }

  private PrepaidUserBalance10 newBalance() {
    return new PrepaidUserBalance10(152, 152,
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)));
  }

  @Test
  public void upadetPrepaidUserBalance_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    final PrepaidUserBalance10 newBalance = newBalance();

    getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), newBalance);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Saldo debe igual", newBalance, prepaidUser10.getBalance());
    Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", prepaidUser10.getBalanceExpiration() > System.currentTimeMillis());
  }

  @Test
  public void upadetPrepaidUserBalance_not_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    try {
      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), null);
    } catch(ValidationException vex) {
      Assert.assertEquals("debe se error 101004", Integer.valueOf(101004), vex.getCode());
    }

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());
  }

  @Test
  public void getPrepaidUserBalance() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    {
      PrepaidBalance10 balance = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, prepaidUser10.getId());

      Assert.assertNull("Saldo debe ser null", balance);
    }

    final PrepaidUserBalance10 newBalance = newBalance();

    //actualizar saldo
    {
      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), newBalance);

      prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

      Assert.assertEquals("Saldo debe ser igual", newBalance, prepaidUser10.getBalance());
      Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", prepaidUser10.getBalanceExpiration() > System.currentTimeMillis());
    }

    //obtener nuevo balance
    {
      PrepaidBalance10 balance = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, prepaidUser10.getId());

      System.out.println(toJson(balance));

      Assert.assertEquals("Saldo debe ser igual", newBalance.getClamonp(), balance.getPrimary().getCurrencyCode().getValue());
      Assert.assertEquals("Saldo debe ser igual", newBalance.getSalautconp(), balance.getPrimary().getValue());
      Assert.assertEquals("Saldo debe ser igual", newBalance.getClamons(), balance.getSecondary().getCurrencyCode().getValue());
      Assert.assertEquals("Saldo debe ser igual", newBalance.getSalautcons(), balance.getSecondary().getValue());
    }
  }
}
