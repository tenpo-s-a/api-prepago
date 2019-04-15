package cl.multicaja.test.integration.v10.async;


import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

/**
 * Estos test de withdrawalSimulation requieren del proceso asincrono dado que realizan cargas antes de validar
 *
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_withdrawalSimulation extends TestBaseUnitAsync {

  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_prepaid_user_not_found() throws Exception {

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, Long.MAX_VALUE, simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(NotFoundException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
    }
  }
  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_prepaid_user_disabled() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    try {

      getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser.getUserIdMc(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }
  @Ignore
  @Test
  public void withdrawalSimulation_ok_WEB() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    SimulationWithdrawal10 resp = getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(BigDecimal.valueOf(
      getCalculationsHelper().addIva(getPercentage().getCALCULATOR_WITHDRAW_WEB_FEE_AMOUNT()).intValue()
    ));

    // La funcion esta deprecada, ya no se testea sus comisiones
    //Assert.assertEquals("deben ser las mismas comisiones", calculatedFee, resp.getFee());
    //Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", amount.getValue().add(calculatedFee.getValue()), resp.getAmountToDiscount().getValue());
  }
  @Ignore
  @Test
  public void withdrawalSimulation_ok_POS() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    SimulationWithdrawal10 resp = getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(getCalculationsHelper().calculateFee(simulationNew.getAmount().getValue(), getPercentage().getCALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE()));

    Assert.assertEquals("deben ser las mismas comisiones", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", amount.getValue().add(calculatedFee.getValue()), resp.getAmountToDiscount().getValue());
  }
  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_insufficient_balance_WEB() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

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
      getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", SALDO_INSUFICIENTE_$VALUE.getValue(), vex.getCode());
    }
  }
  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_insufficient_balance_POS() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

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
      getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", SALDO_INSUFICIENTE_$VALUE.getValue(), vex.getCode());
    }
  }
  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_min_amount_web() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(499));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);
    try {
      getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);
      Assert.fail("no debe pasar por aca");
    } catch(ValidationException vex) {
      System.out.println(vex);
      Assert.assertEquals("debe ser error de supera saldo", EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS.getValue(), vex.getCode());
    }
  }
  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_min_amount_pos() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(999));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);
    try {
      getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);
      Assert.fail("no debe pasar por aca");
    } catch(ValidationException vex) {
      System.out.println(vex);
      Assert.assertEquals("debe ser error de supera saldo", EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS.getValue(), vex.getCode());
    }
  }
  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_max_amount_web() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(500001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);
    try {
      getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);
    } catch (ValidationException ex) {
      Assert.assertEquals("debe ser error de supera saldo", EL_RETIRO_SUPERA_EL_MONTO_MAXIMO_DE_UN_RETIRO_WEB.getValue(), ex.getCode());
    }
  }
  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_max_amount_pos() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(100001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);
    try {
      getPrepaidEJBBean10().withdrawalSimulation(null, prepaidUser10.getUserIdMc(), simulationNew);
    } catch (ValidationException ex) {
      Assert.assertEquals("debe ser error de supera saldo", EL_RETIRO_SUPERA_EL_MONTO_MAXIMO_DE_UN_RETIRO_POS.getValue(), ex.getCode());
    }
  }

  @Ignore
  @Test
  public void withdrawalSimulation_not_ok_by_monthly_limit() throws Exception {

    Map<String, Object> headers = new HashMap<>();
    headers.put("forceRefreshBalance", Boolean.TRUE);

    String merchantCodeWEB = NewPrepaidTopup10.WEB_MERCHANT_CODE;

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    doTopup(prepaidUser10, 3000, merchantCodeWEB);

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    System.out.println(prepaidCard10);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    System.out.println(prepaidBalance10);

    Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision de apertura (0))", 3000, prepaidBalance10.getBalance().getValue().longValue());

    long sumBalance = prepaidBalance10.getBalance().getValue().longValue();

    {
      //se cargan 500.000 mil pesos, enesimas cargas
      for(int j = 1; j <= 5; j++) {

        System.out.println("------------------------------------ Carga " + j +" ---------------------------------------------");

        Integer amount = j == 5 ? 97000 : 100000;
        doTopup(prepaidUser10, amount, merchantCodeWEB);

        Thread.sleep(1000);

        prepaidBalance10 = getAccountEJBBean10().getBalance(headers, account.getId());

        System.out.println(prepaidBalance10);

        sumBalance += amount;

        Assert.assertEquals("El saldo del usuario debe estar actualizado", BigDecimal.valueOf(sumBalance), prepaidBalance10.getBalance().getValue());

        System.out.println("---------------------------------------------------------------------------------------");
      }

      // se retiran 400.000
      doWirhdraw(user, password,450000L, merchantCodeWEB);
      sumBalance -= 450100;
    }

    {
      //se cargan 400.000 mil pesos, enesimas cargas
      for(int j = 1; j <= 4; j++) {

        System.out.println("------------------------------------ Carga " + j +" ---------------------------------------------");

        Integer amount = 100000;
        doTopup(prepaidUser10, amount, merchantCodeWEB);

        Thread.sleep(1000);

        prepaidBalance10 = getAccountEJBBean10().getBalance(headers, account.getId());

        System.out.println(prepaidBalance10);

        sumBalance += amount;

        Assert.assertEquals("El saldo del usuario debe estar actualizado", BigDecimal.valueOf(sumBalance), prepaidBalance10.getBalance().getValue());

        System.out.println("---------------------------------------------------------------------------------------");
      }

      // se retiran 400.000
      doWirhdraw(user, password,400000L, merchantCodeWEB);
      sumBalance -= 400100;
    }

    // se cargan 450000 directamente solo para simular
    {
      InclusionMovimientosDTO dto = topupInTecnocom(prepaidCard10, BigDecimal.valueOf(450000));
      Assert.assertTrue("Debe ser exitoso", dto.isRetornoExitoso());
    }


    prepaidBalance10 = getAccountEJBBean10().getBalance(headers, account.getId());
    sumBalance = prepaidBalance10.getBalance().getValue().longValue();

    //se retiran 150.000 mil pesos
    for(int j = 1; j <= 1; j++) {

      System.out.println("------------------------------------ Retiro " + j +" ---------------------------------------------");

      doWirhdraw(user, password, 100000L, merchantCodeWEB);

      Thread.sleep(1000);

      prepaidBalance10 = getAccountEJBBean10().getBalance(headers, account.getId());

      System.out.println(prepaidBalance10);

      sumBalance -= 100000;
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

  private void doTopup(PrepaidUser10 user, Integer amount, String merchantCode) throws Exception {
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
    prepaidTopup10.setMerchantCode(merchantCode); //carga WEB
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(amount));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,user.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
  }

  private void doWirhdraw(User user, String password, Long amount, String merchantCode) throws Exception {
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, merchantCode);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(amount));
    try {
      //getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw,true);
    } catch (Exception vex) {
      Assert.fail("No debe pasar por aca");
    }
  }
}
