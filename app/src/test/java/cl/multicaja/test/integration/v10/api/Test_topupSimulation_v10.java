package cl.multicaja.test.integration.v10.api;


import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.SimulationNew10;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_topupSimulation_v10 extends TestBaseUnitApi {

  /*
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


  //TODO: Verificar si esto se seguira usando
  @Test
  public void topupSimulation_not_ok_by_params_null() throws Exception {
    /*
    final Integer codErrorParamNull = PARAMETRO_FALTANTE_$VALUE.getValue();

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);

      HttpResponse respHttp = topupSimulation(null, simulationNew);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(null);

      HttpResponse respHttp = topupSimulation(Long.MAX_VALUE, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 404", 404, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
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
    */
  }


  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void topupSimulation_ok_WEB() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    // Se hace la primera carga
    topupUserBalance(prepaidUser10.getUuid(), BigDecimal.valueOf(3000));
    PrepaidCard10 card = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tenener una tarjeta activa", card);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    System.out.println("Calcular carga WEB: " + simulationNew);

    HttpResponse respHttp = topupSimulation(prepaidUser10.getUserIdMc(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationTopupGroup10 resp = respHttp.toObject(SimulationTopupGroup10.class);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(BigDecimal.ZERO);

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupWeb().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupWeb().getEed());
    Assert.assertNotNull("debe tener indicador de si es primera carga o no", resp.getSimulationTopupWeb().getFirstTopup());
  }
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void topupSimulation_ok_firstTopup_WEB() throws Exception {

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

    SimulationTopupGroup10 resp = respHttp.toObject(SimulationTopupGroup10.class);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_WEB_FEE_AMOUNT());

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupWeb().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", BigDecimal.valueOf(3000), resp.getSimulationTopupWeb().getAmountToPay().getValue());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupWeb().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupWeb().getEed());
    Assert.assertNotNull("debe tener indicador de si es primera carga o no", resp.getSimulationTopupWeb().getFirstTopup());
  }
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void topupSimulation_ok_POS() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    // Se hace la primera carga
    topupUserBalance(prepaidUser10.getUuid(), BigDecimal.valueOf(3000));
    PrepaidCard10 card = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tenener una tarjeta activa", card);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);

    HttpResponse respHttp = topupSimulation(prepaidUser10.getUserIdMc(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationTopupGroup10 resp = respHttp.toObject(SimulationTopupGroup10.class);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(getCalculationsHelper().calculateFee(simulationNew.getAmount().getValue(), BigDecimal.valueOf(0.5)));

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));
    //prepaidUser10 = getPrepaidUserEJBBean10().getUserLevel(user,prepaidUser10);
    System.out.println(calculatedFee.getValue()+"  "+resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getSimulationTopupPOS().getAmountToPay());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupPOS().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupPOS().getEed());
    Assert.assertNotNull("debe tener indicador de si es primera carga o no", resp.getSimulationTopupWeb().getFirstTopup());
  }
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void topupSimulation_ok_firstTopup_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    System.out.println("Calcular carga POS: " + simulationNew);

    HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationTopupGroup10 resp = respHttp.toObject(SimulationTopupGroup10.class);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.calculateFee(simulationNew.getAmount().getValue(), calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE()));

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()));

    prepaidUser10 = getPrepaidUserEJBBean10().getUserLevel(user,prepaidUser10);
    System.out.println(calculatedFee.getValue()+"  "+resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getSimulationTopupPOS().getFee());
    Assert.assertEquals("debe ser monto a pagar + comision + comision apertura", BigDecimal.valueOf(3119), resp.getSimulationTopupPOS().getAmountToPay().getValue());

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupPOS().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupPOS().getEed());
    Assert.assertNotNull("debe tener indicador de si es primera carga o no", resp.getSimulationTopupWeb().getFirstTopup());
  }
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void topupSimulation_not_ok_exceeds_balance() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    System.out.println(prepaidUser10.getUserLevel());

    // se hace una carga
    topupUserBalance(prepaidUser10.getUuid(), BigDecimal.valueOf(450000)); //se agrega saldo de 450.000 en tecnocom

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
      HttpResponse respHttp = topupSimulation(prepaidUser10.getUserIdMc(), simulationNew);

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
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void topupSimulation_ok_POS_USERLevel1()  throws Exception {

    User user = registerUserFirstTopup();

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

    SimulationTopupGroup10 resp = respHttp.toObject(SimulationTopupGroup10.class);

    System.out.println("respuesta calculo: " + resp);

    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculationsHelper.calculateFee(simulationNew.getAmount().getValue(), calculationsHelper.getCalculatorParameter10().getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE()));
    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee.getValue()).add(new BigDecimal(0)));

    NewAmountAndCurrency10 calculatedPca = new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amount.getValue()));
    NewAmountAndCurrency10 calculatedEee = new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amount.getValue()), CodigoMoneda.USA_USD);

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getSimulationTopupPOS().getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getSimulationTopupPOS().getEed());
    Assert.assertEquals("Debe ser 0 comision de apertura", new BigDecimal(0), resp.getSimulationTopupPOS().getOpeningFee().getValue());
    Assert.assertEquals("debe ser monto a pagar + comision + 990 Monto Apertura", calculatedAmount.getValue(), resp.getSimulationTopupPOS().getAmountToPay().getValue());
    Assert.assertNotNull("debe tener indicador de si es primera carga o no", resp.getSimulationTopupPOS().getFirstTopup());
  }
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void topupSimulation_not_ok_by_first_topup_max_amount() throws Exception {

    User user = registerUserFirstTopup();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(50001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_PRIMERA_CARGA.getValue(), vex.getCode());
  }
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
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

      HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      ValidationException vex = respHttp.toObject(ValidationException.class);

      Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_ES_MENOR_AL_MINIMO_DE_CARGA.getValue(), vex.getCode());
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

      HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      ValidationException vex = respHttp.toObject(ValidationException.class);

      Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_ES_MENOR_AL_MINIMO_DE_CARGA.getValue(), vex.getCode());
    }
  }
   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
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

    HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());
    SimulationTopupGroup10 group = respHttp.toObject(SimulationTopupGroup10.class);

    Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_CARGA_WEB.getValue(), group.getSimulationTopupWeb().getCode());
    Assert.assertNotNull("debe tener indicador de si es primera carga o no",group.getSimulationTopupWeb().getFirstTopup());
  }

   */

  /*
  //TODO: Verificar si esto se seguira usando
  @Ignore
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
    HttpResponse respHttp = topupSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());
    SimulationTopupGroup10 group = respHttp.toObject(SimulationTopupGroup10.class);

    Assert.assertEquals("debe ser error de supera saldo", LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_CARGA_POS.getValue(), group.getSimulationTopupPOS().getCode());
    Assert.assertNotNull("debe tener indicador de si es primera carga o no",group.getSimulationTopupPOS().getFirstTopup());
  }
   */
}
