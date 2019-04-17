package cl.multicaja.test.integration.v10.async;


import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;


/**
 * Estos test de topupSimulation requieren del proceso asincrono dado que realizan cargas antes de validar
 *
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_topupSimulation extends TestBaseUnitAsync {

  @Ignore
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

  @Ignore
  @Test
  public void topupSimulation_ok_WEB() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga
    doTopup(prepaidUser10, 3119, getRandomNumericString(15));

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3119));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getUserIdMc(), simulationNew);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_WEB_FEE_AMOUNT());

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupWeb().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupWeb().getEed());
    Assert.assertFalse("no debe ser primera carga", resp.getSimulationTopupWeb().getFirstTopup());
  }
  @Ignore
  @Test
  public void topupSimulation_ok_firstTopup_WEB() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3119));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_WEB_FEE_AMOUNT());

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision + comision de apertura", BigDecimal.valueOf(3119), resp.getSimulationTopupWeb().getAmountToPay().getValue());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupWeb().getEed());
    Assert.assertTrue("no debe ser primera carga", resp.getSimulationTopupWeb().getFirstTopup());
  }

  @Ignore
  @Test
  public void topupSimulation_ok_POS() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga
    doTopup(prepaidUser10, 3119, getRandomNumericString(15));

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3119));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getUserIdMc(), simulationNew);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.calculateFee(simulationNew.getAmount().getValue(), calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE()));

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupPOS().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupPOS().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupPOS().getEed());
    Assert.assertFalse("no debe ser primera carga", resp.getSimulationTopupPOS().getFirstTopup());
  }
  @Ignore
  @Test
  public void topupSimulation_ok_firstTopup_POS() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.calculateFee(simulationNew.getAmount().getValue(), calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE()));
    System.out.println("calculatedFee: " + calculatedFee.getValue());
    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));
    System.out.println("calculatedAmount: " + calculatedAmount.getValue());

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision + comision de apertura", BigDecimal.valueOf(3119), resp.getSimulationTopupPOS().getAmountToPay().getValue());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupPOS().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupPOS().getEed());
    Assert.assertTrue("no debe ser primera carga", resp.getSimulationTopupPOS().getFirstTopup());
  }
  @Ignore
  @Test
  public void topupSimulation_ok_first_topup() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3119));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(getPercentage().getCALCULATOR_TOPUP_WEB_FEE_AMOUNT());

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(getPercentage().getOPENING_FEE()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupWeb().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupWeb().getEed());
    Assert.assertNotNull("debe tener comision de apertura", resp.getSimulationTopupWeb().getOpeningFee());
    Assert.assertEquals("debe tener comision de apertura", getPercentage().getOPENING_FEE(), resp.getSimulationTopupWeb().getOpeningFee().getValue());
  }
  @Ignore
  @Test
  public void topupSimulation_not_ok_by_first_topup_max_amount() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(50001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);
    try {
      getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);
      Assert.fail("no debe pasar por aca");
    } catch(ValidationException vex) {
      System.out.println(vex);
      Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_PRIMERA_CARGA.getValue(), vex.getCode());
    } catch(BadRequestException | NotFoundException ex){
      throw ex;
    }
  }
  @Ignore
  @Test
  public void topupSimulation_not_ok_by_min_amount() throws Exception {
    //WEB
    {
      PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
      prepaidUser10 = createPrepaidUserV2(prepaidUser10);

      AltaClienteDTO altaClienteDTO = registerInTecnocomV2(prepaidUser10);

      Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);
      prepaidCard10 = createPrepaidCard10(prepaidCard10);

      BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

      //InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

      //Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(2999));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.WEB);

      System.out.println("Calcular carga WEB: " + simulationNew);
      try {
        getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);
        Assert.fail("no debe pasar por aca");
      } catch(ValidationException vex) {
        System.out.println(vex);
        Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_ES_MENOR_AL_MINIMO_DE_CARGA.getValue(), vex.getCode());
      }
    }
    //POS
    {
      PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
      prepaidUser10 = createPrepaidUserV2(prepaidUser10);

      AltaClienteDTO altaClienteDTO = registerInTecnocomV2(prepaidUser10);

      Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

      prepaidCard10 = createPrepaidCard10(prepaidCard10);

      BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

      //InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

      //Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(2999));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.POS);

      System.out.println("Calcular carga POS: " + simulationNew);
      try {
        getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);
        Assert.fail("no debe pasar por aca");
      } catch(ValidationException vex) {
        System.out.println(vex);
        Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_ES_MENOR_AL_MINIMO_DE_CARGA.getValue(), vex.getCode());
      }
    }
  }

  @Ignore
  @Test
  public void topupSimulation_not_ok_by_max_amount_web() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocomV2(prepaidUser10);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    //InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    //Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(500001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);
    SimulationTopupGroup10 group = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);
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

  @Ignore
  @Test
  public void topupSimulation_not_ok_by_max_amount_pos() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocomV2(prepaidUser10);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    //InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    //Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(101586));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);
    SimulationTopupGroup10 group = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);
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
  @Ignore
  @Test
  public void topupSimulation_not_ok_by_exceeds_balance() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidCard10 prepaidCard10;
    //primera carga
    {
      doTopup(prepaidUser10, 3119, NewPrepaidTopup10.WEB_MERCHANT_CODE);

      prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
      Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    }

    BigDecimal impfac = BigDecimal.valueOf(400000); //se agrega saldo de 400.000 en tecnocom

    //InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    //Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta cargar 100.001, debe dar error dado que el maximo es 500.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(100001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB); //da lo mismo si es WEB o POS, para los 2 casos es la misma validacion

    try {

      //debe lanzar excepcion de supera saldo, dado que intenta cargar 100.001 que sumado al saldo inicial de 400.000
      //supera el maximo de 500.000
      getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de supera saldo", SALDO_SUPERARA_LOS_$$VALUE.getValue(), vex.getCode());
    }
  }

  @Ignore
  @Test
  public void topupSimulation_not_ok_by_cdt_limit() throws Exception {

    Map<String, Object> headers = new HashMap<>();
    headers.put("forceRefreshBalance", Boolean.TRUE);

    String merchantCodeWEB = NewPrepaidTopup10.WEB_MERCHANT_CODE;
    String password = "1235";
    User user = registerUser(password);
    user = updateUserPassword(user, password);
    UserAccount bankAccount = createBankAccount(user);
    System.out.println(String.format("Creada bank accout con id: %d", bankAccount.getId()));

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.setMerchantCode(merchantCodeWEB); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());
    }

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    System.out.println(prepaidCard10);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    System.out.println(prepaidBalance10);

    Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision de apertura (0))", 3000, prepaidBalance10.getBalance().getValue().longValue());

    long sumBalance = prepaidBalance10.getBalance().getValue().longValue();
    {
      //se cargan 400.000 mil pesos, enesimas cargas
      Integer amount = 100000;
      for (int j = 1; j <= 4; j++) {

        System.out.println("------------------------------------ Carga " + j + " ---------------------------------------------");

        doTopup(prepaidUser10, amount, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

        Thread.sleep(1000);

        prepaidBalance10 = getAccountEJBBean10().getBalance(headers, account.getId());

        System.out.println(prepaidBalance10);

        sumBalance += amount;

        Assert.assertEquals("El saldo del usuario debe estar actualizado", sumBalance, prepaidBalance10.getBalance().getValue().longValue());

        System.out.println("---------------------------------------------------------------------------------------");
      }

      // Se retiran 400000
      {
        doWithdraw(user, password, 400000L, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE, bankAccount.getId());
        sumBalance -= 400100;
      }
    }
    {
      //se cargan 400.000 mil pesos, enesimas cargas
      Integer amount = 100000;
      for (int j = 5; j <= 8; j++) {

        System.out.println("------------------------------------ Carga " + j + " ---------------------------------------------");

        doTopup(prepaidUser10, amount, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

        Thread.sleep(1000);

        prepaidBalance10 = getAccountEJBBean10().getBalance(headers, account.getId());

        System.out.println(prepaidBalance10);

        sumBalance += amount;

        Assert.assertEquals("El saldo del usuario debe estar actualizado", sumBalance, prepaidBalance10.getBalance().getValue().longValue());

        System.out.println("---------------------------------------------------------------------------------------");
      }
      // Se retiran 400000
      {
        doWithdraw(user, password, 400000L, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE, bankAccount.getId());
        sumBalance -= 400100;
      }
    }
    {
      //se cargan 100.000 mil pesos, enesimas cargas
      Integer amount = 100000;
      for (int j = 9; j < 10; j++) {

        System.out.println("------------------------------------ Carga " + j + " ---------------------------------------------");

        doTopup(prepaidUser10, amount, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

        Thread.sleep(1000);

        prepaidBalance10 = getAccountEJBBean10().getBalance(headers, account.getId());

        System.out.println(prepaidBalance10);

        sumBalance += amount;

        Assert.assertEquals("El saldo del usuario debe estar actualizado", sumBalance, prepaidBalance10.getBalance().getValue().longValue());

        System.out.println("---------------------------------------------------------------------------------------");
      }
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

  private void doTopup(PrepaidUser10 prepaidUser10,Integer amount, String merchantCode) throws Exception {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.setMerchantCode(merchantCode);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(amount));
      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

      Assert.assertNotNull("debe tener un id", resp.getId());
  }

  private void doWithdraw(User user, String password, Long amount, String merchantCode) throws Exception {
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(amount));
    try {
      //TODO: Esto despues debera tener idUsuario
      //getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw,true);
    } catch (Exception vex) {
      Assert.fail("No debe pasar por aca");
    }
  }

  //TODO: Revisar si esto se usara
  private void doWithdraw(User user, String password, Long amount, String merchantCode, Long bankAccountId) throws Exception {
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user,password);
    prepaidWithdraw.setMerchantCode(merchantCode);
    //prepaidWithdraw.setBankAccountId(bankAccountId);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(amount));
    try{
      //TODO: Esto despues debera tener idUsuario
      //getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw,true);
    } catch (Exception vex) {
      Assert.fail("No debe pasar por aca");
    }
  }

  /**
   * Simulacion en USD
   */
  @Ignore
  @Test
  public void topupSimulation_usd_ok_POS() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga
    doTopup(prepaidUser10, 3119, NewPrepaidTopup10.WEB_MERCHANT_CODE);

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(5));
    amount.setCurrencyCode(CodigoMoneda.USA_USD);

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.calculateFee(BigDecimal.valueOf(3540), calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE()));

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(BigDecimal.valueOf(3540).add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupPOS().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(BigDecimal.valueOf(3228.97));

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupPOS().getPca());
    Assert.assertEquals("debe ser el eed calculado", amount, resp.getSimulationTopupPOS().getEed());
    Assert.assertFalse("no debe ser primera carga", resp.getSimulationTopupPOS().getFirstTopup());

    Assert.assertNotNull("debe tener monto inicial en pesos", resp.getSimulationTopupWeb().getInitialAmount());
    Assert.assertEquals("debe tener monto inicial en pesos", CodigoMoneda.CHILE_CLP, resp.getSimulationTopupWeb().getInitialAmount().getCurrencyCode());
    Assert.assertEquals("debe tener monto inicial en pesos", BigDecimal.valueOf(3540), resp.getSimulationTopupWeb().getInitialAmount().getValue());
  }
  @Ignore
  @Test
  public void topupSimulation_usd_ok_WEB() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga
    doTopup(prepaidUser10, 3119, NewPrepaidTopup10.WEB_MERCHANT_CODE);

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(5));
    amount.setCurrencyCode(CodigoMoneda.USA_USD);

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    SimulationTopupGroup10 resp = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_WEB_FEE_AMOUNT());

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(BigDecimal.valueOf(3540).add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupWeb().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(BigDecimal.valueOf(3228.97));

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", amount, resp.getSimulationTopupWeb().getEed());
    Assert.assertFalse("no debe ser primera carga", resp.getSimulationTopupWeb().getFirstTopup());

    Assert.assertNotNull("debe tener monto inicial en pesos", resp.getSimulationTopupWeb().getInitialAmount());
    Assert.assertEquals("debe tener monto inicial en pesos", CodigoMoneda.CHILE_CLP, resp.getSimulationTopupWeb().getInitialAmount().getCurrencyCode());
    Assert.assertEquals("debe tener monto inicial en pesos", BigDecimal.valueOf(3540), resp.getSimulationTopupWeb().getInitialAmount().getValue());
  }

  @Ignore
  @Test
  public void topupSimulation_usd_not_ok_by_max_amount_web() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account= buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    //InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    //Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(775));
    amount.setCurrencyCode(CodigoMoneda.USA_USD);

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);
    SimulationTopupGroup10 group = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getUserIdMc(), simulationNew);
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
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS().getInitialAmount());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupPOS().getInitialAmount().getValue());
  }
  @Ignore
  @Test
  public void topupSimulation_usd_not_ok_by_max_amount_pos() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocomV2(prepaidUser10);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    //InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    //Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(155));
    amount.setCurrencyCode(CodigoMoneda.USA_USD);

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);
    SimulationTopupGroup10 group = getPrepaidEJBBean10().topupSimulationGroup(null, prepaidUser10.getId(), simulationNew);
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
    Assert.assertNotNull("Deberia tener la info en 0", group.getSimulationTopupPOS().getInitialAmount());
    Assert.assertEquals("Deberia tener la info en 0", BigDecimal.valueOf(0), group.getSimulationTopupPOS().getInitialAmount().getValue());

  }
}
