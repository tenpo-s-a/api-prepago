package cl.multicaja.test.v10.async;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.NameStatus;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * Estos test de withdrawalSimulation requieren del proceso asincrono dado que realizan cargas antes de validar
 *
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_withdrawalSimulation extends TestBaseUnitAsync {

  @Test
  public void withdrawalSimulation_not_ok_by_params_null() throws Exception {

    final Integer codErrorParamNull = PARAMETRO_FALTANTE_$VALUE.getValue();

    // userId null
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().withdrawalSimulation(null, null, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }

    // paymentMethod null
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(null);

      try {

        getPrepaidEJBBean10().withdrawalSimulation(null, Long.MAX_VALUE, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    // amount null
    {
      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(null);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().withdrawalSimulation(null, 1L, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    // amount.value null
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().withdrawalSimulation(null, 1L, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    // amount.currencyCode null
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));
      amount.setCurrencyCode(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().withdrawalSimulation(null, 1L, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_not_found() throws Exception {

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, Long.MAX_VALUE, simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(NotFoundException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_disabled() throws Exception {

    User user = registerUser(UserStatus.DISABLED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_deleted() throws Exception {

    User user = registerUser(UserStatus.DELETED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_locked() throws Exception {

    User user = registerUser(UserStatus.LOCKED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_preregistered() throws Exception {

    User user = registerUser(UserStatus.PREREGISTERED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_prepaid_user_not_found() throws Exception {

    User user = registerUser();

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(NotFoundException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_prepaid_user_disabled() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
  prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUser10(prepaidUser);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_ok_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    SimulationWithdrawal10 resp = getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT);

    Assert.assertEquals("deben ser las mismas comisiones", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", amount.getValue().add(calculatedFee.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void withdrawalSimulation_ok_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    SimulationWithdrawal10 resp = getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculateFee(simulationNew.getAmount().getValue(), CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE));

    Assert.assertEquals("deben ser las mismas comisiones", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", amount.getValue().add(calculatedFee.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_insufficient_balance_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 10.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(10000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    try {

      //debe lanzar excepcion de saldo insuficiente dado que intenta retirar 10.000 al cual se le agrega la comision de
      //retiro WEB  y eso supera el saldo inicial de 10.000
      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", SALDO_INSUFICIENTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_insufficient_balance_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 10.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(10000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    try {

      //debe lanzar excepcion de saldo insuficiente dado que intenta retirar 10.000 al cual se le agrega la comision de
      //retiro POS  y eso supera el saldo inicial de 10.000
      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", SALDO_INSUFICIENTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_min_amount() throws Exception {
    //WEB
    {
      User user = registerUser();
      updateUser(user);

      PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

      prepaidUser10 = createPrepaidUser10(prepaidUser10);

      AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

      Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

      prepaidCard10 = createPrepaidCard10(prepaidCard10);

      BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

      InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

      Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(999));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.WEB);

      System.out.println("Calcular carga WEB: " + simulationNew);
      try {
        getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);
        Assert.fail("no debe pasar por aca");
      } catch(ValidationException vex) {
        System.out.println(vex);
        Assert.assertEquals("debe ser error de supera saldo", EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS.getValue(), vex.getCode());
      }
    }
    //POS
    {
      User user = registerUser();
      updateUser(user);

      PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

      prepaidUser10 = createPrepaidUser10(prepaidUser10);

      AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

      Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

      prepaidCard10 = createPrepaidCard10(prepaidCard10);

      BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

      InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

      Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(999));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.POS);

      System.out.println("Calcular carga POS: " + simulationNew);
      try {
        getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);
        Assert.fail("no debe pasar por aca");
      } catch(ValidationException vex) {
        System.out.println(vex);
        Assert.assertEquals("debe ser error de supera saldo", EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS.getValue(), vex.getCode());
      }
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_max_amount_web() throws Exception {
    User user = registerUser();
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(500001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);
    try {
      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);
    } catch (ValidationException ex) {
      Assert.assertEquals("debe ser error de supera saldo", EL_RETIRO_SUPERA_EL_MONTO_MAXIMO_DE_UN_RETIRO_WEB.getValue(), ex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_max_amount_pos() throws Exception {
    User user = registerUser();
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(100001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);
    try {
      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);
    } catch (ValidationException ex) {
      Assert.assertEquals("debe ser error de supera saldo", EL_RETIRO_SUPERA_EL_MONTO_MAXIMO_DE_UN_RETIRO_POS.getValue(), ex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_monthly_limit() throws Exception {

    Map<String, Object> headers = new HashMap<>();
    headers.put("forceRefreshBalance", Boolean.TRUE);

    String merchantCodeWEB = NewPrepaidTopup10.WEB_MERCHANT_CODE;

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    //se actualiza el usuario a nivel 1 para poder realizar la 1ra carga
    user.setNameStatus(NameStatus.UNVERIFIED);
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(merchantCodeWEB); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());
    }

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    System.out.println(prepaidCard10);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(headers, user.getId());

    System.out.println(prepaidBalance10);

    Assert.assertEquals("El saldo del usuario debe ser 2010 pesos (carga inicial - comision de apertura (990))", 2010L, prepaidBalance10.getBalance().getValue().longValue());

    //se actualiza al usuario a nivel 2
    user.setNameStatus(NameStatus.VERIFIED);
    updateUser(user);

    long sumBalance = prepaidBalance10.getBalance().getValue().longValue();

    //se cargan 900.000 mil pesos, enesimas cargas
    for(int j = 1; j <= 10; j++) {

      System.out.println("------------------------------------ Carga " + j +" ---------------------------------------------");

      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(merchantCodeWEB); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(j == 10 ? 97000 : 100000));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());

      Thread.sleep(1000);

      prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(headers, user.getId());

      System.out.println(prepaidBalance10);

      sumBalance+= prepaidTopup10.getAmount().getValue().longValue();

      Assert.assertEquals("El saldo del usuario debe estar actualizado", sumBalance, prepaidBalance10.getBalance().getValue().longValue());

      System.out.println("---------------------------------------------------------------------------------------");
    }

    prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(headers, user.getId());
    sumBalance = prepaidBalance10.getBalance().getValue().longValue();

    //se retiran 900.000 mil pesos
    for(int j = 1; j <= 9; j++) {

      System.out.println("------------------------------------ Retiro " + j +" ---------------------------------------------");

      NewPrepaidWithdraw10 prepaidWithdraw10 = buildNewPrepaidWithdraw10(user, password);
      prepaidWithdraw10.setMerchantCode(merchantCodeWEB); //carga WEB
      prepaidWithdraw10.getAmount().setValue(BigDecimal.valueOf(100000));

      PrepaidWithdraw10 resp = getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw10);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());

      Thread.sleep(1000);

      prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(headers, user.getId());

      System.out.println(prepaidBalance10);

      sumBalance-= prepaidWithdraw10.getAmount().getValue().longValue();
      sumBalance -= 100;

      Assert.assertEquals("El saldo del usuario debe estar actualizado", sumBalance, prepaidBalance10.getBalance().getValue().longValue());

      System.out.println("---------------------------------------------------------------------------------------");
    }

    //se intenta simular retiro de 150.000, debe dar error de cdt
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(150000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB); //da lo mismo si es WEB o POS, para los 2 casos es la misma validacion

    System.out.println("Calcular retiro WEB: " + simulationNew);

    try {

      //debe lanzar excepcion de validacion del CDT, dado que intenta retirar 150.000 que sumado a los retiros anteriores de
      //900.000 supera el maximo de 1.000.000 mensual
      getPrepaidEJBBean10().withdrawalSimulation(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      System.out.println(vex);
      Assert.assertEquals("debe ser error de supera saldo", EL_RETIRO_SUPERA_EL_MONTO_MAXIMO_DE_RETIROS_MENSUALES.getValue(), vex.getCode());
    }
  }
}
