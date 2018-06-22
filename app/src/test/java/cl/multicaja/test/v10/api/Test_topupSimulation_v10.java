package cl.multicaja.test.v10.api;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * @autor vutreras
 */
public class Test_topupSimulation_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userIdMc
   * @param simulationNew
   * @return
   */
  private HttpResponse topupSimulation(Long userIdMc, SimulationNew10 simulationNew) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/simulation/topup", userIdMc), toJson(simulationNew));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void topupSimulation_not_ok_by_params_null() throws Exception {

    final Integer codErrorParamNull = PARAMETRO_FALTANTE_$VALUE.getValue();

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = topupSimulation(null, simulationNew);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(null);

      HttpResponse respHttp = topupSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(null);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = topupSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = topupSimulation(1L, simulationNew);

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

      HttpResponse respHttp = topupSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
  }

  @Test
  public void topupSimulation_ok_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);

    HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationTopup10 resp = respHttp.toObject(SimulationTopup10.class);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(CALCULATOR_TOPUP_WEB_FEE_AMOUNT);

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(calculateEed(amount.getValue()), CodigoMoneda.USA_USN);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getEed());
  }

  @Test
  public void topupSimulation_ok_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);

    HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationTopup10 resp = respHttp.toObject(SimulationTopup10.class);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculateFee(simulationNew.getAmount().getValue(), CALCULATOR_TOPUP_POS_FEE_PERCENTAGE));

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(calculateEed(amount.getValue()), CodigoMoneda.USA_USN);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getEed());
  }

  @Test
  public void topupSimulation_not_ok_exceeds_balance() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    // se hace una carga
    topupUserBalance(user, BigDecimal.valueOf(450000)); //se agrega saldo de 450.000 en tecnocom

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    //se intenta cargar 100.001, debe dar error dado que el maximo es 500.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(100001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB); //da lo mismo si es WEB o POS, para los 2 casos es la misma validacion

    System.out.println("Calcular carga WEB: " + simulationNew);

    try {

      //debe lanzar excepcion de supera saldo, dado que intenta cargar 100.001 que sumado al saldo inicial de 400.000
      //supera el maximo de 500.000
      HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());

      ValidationException vex = respHttp.toObject(ValidationException.class);

      if (vex != null) {
        throw vex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de supera saldo", Integer.valueOf(109000), vex.getCode());
    }
  }
}
