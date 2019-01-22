package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.RutStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserIdentityStatus;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_topupUserBalance extends TestBaseUnitAsync {

  @Test
  public void topupUserBalance_not_ok_by_user_not_found() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    newPrepaidTopup.setRut(1);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario", CLIENTE_NO_EXISTE.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_user_blacklisted() throws Exception {

    User user = registerUser(UserIdentityStatus.TERRORIST);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(ValidationException nfex) {
      Assert.assertEquals("Cliente en lista negra", CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_CARGAR.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidUser_not_found() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario prepago", CLIENTE_NO_TIENE_PREPAGO.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidUser_disabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(ValidationException nfex) {
      Assert.assertEquals("el usuario prepago esta bloqueado", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidCard_locked_hard() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard10(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidCard_expired() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard10(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_validate_cdt() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    //se debe establecer la primera carga mayor a 3000 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(BigDecimal.valueOf(numberUtils.random(3119, 10000)));

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    String messageId = prepaidTopup.getMessageId();

    if (CamelFactory.getInstance().isCamelRunning()) {
      Assert.assertNotNull("Debe tener messageId dado que camel si se encuentra en ejecucion", messageId);

      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
      ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());

      Assert.assertNotNull("debe tener un objeto de cdt", remoteTopup.getData().getCdtTransaction10());
      Assert.assertNotNull("debe tener un id de cdt", remoteTopup.getData().getCdtTransaction10().getExternalTransactionId());

    } else {
      Assert.assertNull("No debe tener messageId dado que camel no se encuentra en ejecucion", messageId);
    }
  }

  @Test
  public void topupUserBalance_validate_prepaidMovement() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    //se debe establecer la primera carga mayor a 3000 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(BigDecimal.valueOf(numberUtils.random(3000, 10000)));

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    String messageId = prepaidTopup.getMessageId();

    if (CamelFactory.getInstance().isCamelRunning()) {
      Assert.assertNotNull("Debe tener messageId dado que camel si se encuentra en ejecucion", messageId);

      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
      ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());

      Assert.assertNotNull("debe tener un objeto de prepaidMovement", remoteTopup.getData().getPrepaidMovement10());
      Assert.assertTrue("debe tener un id de prepaidMovement", remoteTopup.getData().getPrepaidMovement10().getId() > 0);

    } else {
      Assert.assertNull("No debe tener messageId dado que camel no se encuentra en ejecucion", messageId);
    }
  }

  @Test
  public void topupUserBalance_ok_first_topup_true_by_level_1() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    {
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3119));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());
      Assert.assertTrue("debe ser primera carga", resp.isFirstTopup());
    }

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    System.out.println(prepaidCard10);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    System.out.println(prepaidBalance10);
    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision(119) - comision de apertura (990))", 3000L, prepaidBalance10.getBalance().getValue().longValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision(0) - comision de apertura (990))", 3119L, prepaidBalance10.getBalance().getValue().longValue());
        break;
    }
  }

  @Test
  public void topupUserBalance_ok_first_topup_false_by_level_2() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    {
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3119));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());
      Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());
    }

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    System.out.println(prepaidCard10);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    System.out.println(prepaidBalance10);

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (119) - comision de apertura (0))", BigDecimal.valueOf(3000), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision (0) - comision de apertura (0))", BigDecimal.valueOf(3119), prepaidBalance10.getBalance().getValue());
        break;
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_topup_min() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }

    user.setNameStatus(NameStatus.VERIFIED);

    updateUser(user);

    //enesima carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_topup_max_level_1() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_topup_max_level_2() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(500001));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga web", Integer.valueOf(108201), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(100600));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga pos", Integer.valueOf(108202), vex.getCode());
      }
    }
  }
}
