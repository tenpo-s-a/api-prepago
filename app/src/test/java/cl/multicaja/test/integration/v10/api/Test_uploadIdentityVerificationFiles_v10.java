package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserFile;
import cl.multicaja.users.model.v10.UserStatus;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.core.model.Errors.CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO;

/**
 * @author abarazarte
 **/
public class Test_uploadIdentityVerificationFiles_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userIdMc
   * @param files
   * @return
   */
  private HttpResponse uploadFiles(Long userIdMc, Map<String, UserFile> files) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/identity/files", userIdMc), toJson(files));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private UserFile f = new UserFile();

  /*
  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_params_null() throws Exception {

    final Integer codErrorParamNull = PARAMETRO_FALTANTE_$VALUE.getValue();

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(null, simulationNew);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.WEB);

      HttpResponse respHttp = withdrawalSimulation(Long.MAX_VALUE, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 404", 404, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
    }
    {
      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(null);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));
      amount.setCurrencyCode(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
  }

  */
  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_user_not_found() throws Exception {

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    HttpResponse respHttp = uploadFiles(Long.MAX_VALUE, files);

    Assert.assertEquals("status 404", 404, respHttp.getStatus());
    NotFoundException vex = respHttp.toObject(NotFoundException.class);

    Assert.assertEquals("debe ser error de userMc null", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
  }

  /*
  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_user_disabled() throws Exception {

    User user = registerUser(UserStatus.DISABLED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_user_deleted() throws Exception {

    User user = registerUser(UserStatus.DELETED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_user_locked() throws Exception {

    User user = registerUser(UserStatus.LOCKED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_user_preregistered() throws Exception {

    User user = registerUser(UserStatus.PREREGISTERED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_prepaid_user_not_found() throws Exception {

    User user = registerUser();

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 404", 404, respHttp.getStatus());
    NotFoundException vex = respHttp.toObject(NotFoundException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
  }

  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_prepaid_user_disabled() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUser10(prepaidUser);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  */
}
