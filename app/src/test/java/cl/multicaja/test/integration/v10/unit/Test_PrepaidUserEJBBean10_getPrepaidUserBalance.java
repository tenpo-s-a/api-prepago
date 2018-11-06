package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor vutreras
 */
public class Test_PrepaidUserEJBBean10_getPrepaidUserBalance extends TestBaseUnit {

  private PrepaidBalanceInfo10 newBalance() {
    return new PrepaidBalanceInfo10(152, 152,
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)));
  }

  @Test
  public void getPrepaidUserBalance_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    {
      PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
    }

    final PrepaidBalanceInfo10 newBalance = newBalance();

    //actualizar saldo
    {
      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), newBalance);

      prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

      Assert.assertEquals("Saldo debe ser igual", newBalance, prepaidUser10.getBalance());
      Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", prepaidUser10.getBalanceExpiration() > System.currentTimeMillis());
    }

    //obtener nuevo salo
    {

      PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      BigDecimal balanceValue = BigDecimal.valueOf(newBalance.getSaldisconp().longValue() - newBalance.getSalautconp().longValue());

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(balanceValue);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
    }
  }

  @Test
  public void getPrepaidUserBalance_not_ok() throws Exception {

    User user = registerUser();

    try {

      getPrepaidUserEJBBean10().getPrepaidUserBalance(null, null);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    //no debe existir el usuario
    try {

      getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId() + 1);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(NotFoundException nex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_NO_EXISTE.getValue(), nex.getCode());
    }

    //aun no tiene prepago
    try {

      getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(NotFoundException nex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_NO_TIENE_PREPAGO.getValue(), nex.getCode());
    }

    //ahora el usuario es prepago
    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //ahora tiene prepago pero aun no se ha creado tarjeta, debe dar error de tarjeta primera carga pendiente
    try {

      getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_PRIMERA_CARGA_PENDIENTE.getValue(), vex.getCode());
    }

    //ahora se crea la tarjeta para que pase la validacion anterior
    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //dado que no se dio de alta el cliente, al intentar buscar el saldo en tecnocom debe dar error
    try {

      getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", SALDO_NO_DISPONIBLE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void getPrepaidUserBalance_from_tecnocom() throws Exception {

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

    PrepaidUserEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS = 3000;

    NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac);
    NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
    NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

    {
      PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    {
      PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertFalse("No debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    Thread.sleep(PrepaidUserEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS + 1000);

    {
      PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    {
      PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

      Assert.assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      Assert.assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      Assert.assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      Assert.assertFalse("No debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }
  }

  @Test
  public void validations_pcaMain_pcaSecondary() throws Exception {

    {
      BigDecimal value = BigDecimal.valueOf(0);

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(value);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      Assert.assertEquals("debe ser igual", value, balance.getValue());
      Assert.assertEquals("debe ser 0", BigDecimal.valueOf(0), pcaMain.getValue());
      Assert.assertEquals("debe ser 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.CEILING), pcaSecondary.getValue());
    }

    {
      BigDecimal value = BigDecimal.valueOf(-1);

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(value);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      Assert.assertEquals("debe ser igual", value, balance.getValue());
      Assert.assertEquals("debe ser 0", BigDecimal.valueOf(0), pcaMain.getValue());
      Assert.assertEquals("debe ser 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.CEILING), pcaSecondary.getValue());
    }
  }
}
