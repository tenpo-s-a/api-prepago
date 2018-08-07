package cl.multicaja.test.integration.v10.async;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.NameStatus;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * Estos test de topupSimulation requieren del proceso asincrono dado que realizan cargas antes de validar
 *
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_topupSimulation extends TestBaseUnitAsync {

  @Test
  public void topupSimulation_not_ok_by_params_null() throws Exception {

    final Integer codErrorParamNull = PARAMETRO_FALTANTE_$VALUE.getValue();

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().topupSimulationGroup(null, null, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(null);

      try {

        getPrepaidEJBBean10().topupSimulationGroup(null, Long.MAX_VALUE, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(NotFoundException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
      }
    }
    {
      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(null);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().topupSimulationGroup(null, 1L, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().topupSimulationGroup(null, 1L, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));
      amount.setCurrencyCode(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      try {

        getPrepaidEJBBean10().topupSimulationGroup(null, 1L, simulationNew);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(BadRequestException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
  }

  @Test
  public void topupSimulation_ok_WEB() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.UNVERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

    PrepaidTopup10 respTopup = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

    System.out.println("resp:: " + respTopup);

    Assert.assertNotNull("debe tener un id", respTopup.getId());
    Assert.assertTrue("debe ser primera carga", respTopup.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    // actualizo el usuario
    user.setNameStatus(NameStatus.VERIFIED);
    user = updateUser(user);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(CALCULATOR_TOPUP_WEB_FEE_AMOUNT);

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupWeb().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(calculateEed(amount.getValue()), CodigoMoneda.USA_USN);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupWeb().getEed());
  }

  @Test
  public void topupSimulation_ok_POS() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.UNVERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

    PrepaidTopup10 respTopup = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

    System.out.println("resp:: " + respTopup);

    Assert.assertNotNull("debe tener un id", respTopup.getId());
    Assert.assertTrue("debe ser primera carga", respTopup.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    // actualizo el usuario
    user.setNameStatus(NameStatus.VERIFIED);
    user = updateUser(user);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculateFee(simulationNew.getAmount().getValue(), CALCULATOR_TOPUP_POS_FEE_PERCENTAGE));

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupPOS().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(calculateEed(amount.getValue()), CodigoMoneda.USA_USN);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupPOS().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupPOS().getEed());
  }

  @Test
  public void topupSimulation_ok_first_topup() throws Exception {

    User user = registerUserFirstTopup();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    createPrepaidUser10(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(CALCULATOR_TOPUP_WEB_FEE_AMOUNT);

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(OPENING_FEE));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupWeb().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(calculateEed(amount.getValue()), CodigoMoneda.USA_USN);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupWeb().getEed());
    Assert.assertNotNull("debe tener comision de apertura", resp.getSimulationTopupWeb().getOpeningFee());
    Assert.assertEquals("debe tener comision de apertura", OPENING_FEE, resp.getSimulationTopupWeb().getOpeningFee().getValue());
  }

  @Test
  public void topupSimulation_not_ok_by_first_topup_max_amount() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.UNVERIFIED);
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    createPrepaidUser10(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(50001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);
    try {
      getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);
      Assert.fail("no debe pasar por aca");
    } catch(ValidationException vex) {
      System.out.println(vex);
      Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_PRIMERA_CARGA.getValue(), vex.getCode());
    } catch(BadRequestException | NotFoundException ex){
      throw ex;
    }
  }

  @Test
  public void topupSimulation_not_ok_by_min_amount() throws Exception {
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

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(2999));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.WEB);

      System.out.println("Calcular carga WEB: " + simulationNew);
      try {
        getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);
        Assert.fail("no debe pasar por aca");
      } catch(ValidationException vex) {
        System.out.println(vex);
        Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_ES_MENOR_AL_MINIMO_DE_CARGA.getValue(), vex.getCode());
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

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(2999));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.POS);

      System.out.println("Calcular carga POS: " + simulationNew);
      try {
        getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);
        Assert.fail("no debe pasar por aca");
      } catch(ValidationException vex) {
        System.out.println(vex);
        Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_ES_MENOR_AL_MINIMO_DE_CARGA.getValue(), vex.getCode());
      }
    }
  }

  @Test
  public void topupSimulation_not_ok_by_max_amount_web() throws Exception {
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
    SimulationTopupGroup10 group = getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupWeb());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupWeb().getEed());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupWeb().getEed().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupWeb().getFee());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupWeb().getFee().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupWeb().getPca());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupWeb().getPca().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupWeb().getAmountToPay());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupWeb().getAmountToPay().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupWeb().getOpeningFee());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupWeb().getOpeningFee().getValue());
    Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_CARGA_WEB.getValue(), group.getSimulationTopupWeb().getCode());
  }

  @Test
  public void topupSimulation_not_ok_by_max_amount_pos() throws Exception {
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
    SimulationTopupGroup10 group = getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS().getEed());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupPOS().getEed().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS().getFee());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupPOS().getFee().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS().getPca());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupPOS().getPca().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS().getAmountToPay());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupPOS().getAmountToPay().getValue());
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS().getOpeningFee());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupPOS().getOpeningFee().getValue());
    Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_CARGA_POS.getValue(), group.getSimulationTopupPOS().getCode());
  }

  @Test
  public void topupSimulation_not_ok_by_exceeds_balance() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10;
    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());

      prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
      Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    }

    BigDecimal impfac = BigDecimal.valueOf(400000); //se agrega saldo de 400.000 en tecnocom

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta cargar 100.001, debe dar error dado que el maximo es 500.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(100001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB); //da lo mismo si es WEB o POS, para los 2 casos es la misma validacion

    System.out.println("Calcular carga WEB: " + simulationNew);

    try {

      //debe lanzar excepcion de supera saldo, dado que intenta cargar 100.001 que sumado al saldo inicial de 400.000
      //supera el maximo de 500.000
      getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de supera saldo", SALDO_SUPERARA_LOS_$$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void topupSimulation_not_ok_by_cdt_limit() throws Exception {

    Map<String, Object> headers = new HashMap<>();
    headers.put("forceRefreshBalance", Boolean.TRUE);

    String merchantCodeWEB = NewPrepaidTopup10.WEB_MERCHANT_CODE;

    User user = registerUser();

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
    for(int j = 1; j <= 9; j++) {

      System.out.println("------------------------------------ Carga " + j +" ---------------------------------------------");

      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(merchantCodeWEB); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(100000));

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

    //se intenta simular cargar 100.000, debe dar error de cdt
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(100000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB); //da lo mismo si es WEB o POS, para los 2 casos es la misma validacion

    System.out.println("Calcular carga WEB: " + simulationNew);

    try {

      //debe lanzar excepcion de validacion del CDT, dado que intenta cargar 100.000 que sumado a las cargas anteriores de
      //902.010 supera el maximo de 1.000.000 mensual
      getPrepaidEJBBean10().topupSimulationGroup(null, user.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      System.out.println(vex);
      Assert.assertTrue("debe ser error de limite de cdt", vex.getCode() > TRANSACCION_ERROR_GENERICO_$VALUE.getValue() && vex.getCode() < SALDO_SUPERARA_LOS_$$VALUE.getValue());
    }
  }
}
