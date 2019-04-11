package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.ejb.v10.AccountEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static cl.multicaja.core.model.Errors.*;
import static org.junit.Assert.*;

/**
 * @autor vutreras
 */
public class Test_AccountEJBBean10_getBalance extends TestBaseUnit {

  private PrepaidBalanceInfo10 newBalance() {
    return new PrepaidBalanceInfo10(152, 152,
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)));
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_accountId_null() throws Exception {
    try {
      getAccountEJBBean10().getBalance(null, null);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error accountId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void getBalance_account_notFound() throws Exception {
    try {
      getAccountEJBBean10().getBalance(null, Long.MAX_VALUE);
    } catch (ValidationException ex) {
      assertEquals("Debe retornar error cuenta no existe", CUENTA_NO_EXISTE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void getPrepaidUserBalance_not_ok() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = createRandomAccount(prepaidUser10);

    //dado que no se dio de alta el cliente, al intentar buscar el saldo en tecnocom debe dar error
    try {

      getAccountEJBBean10().getBalance(null, account.getId());

      fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      assertEquals("debe ser error de validacion", SALDO_NO_DISPONIBLE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test
  public void getPrepaidUserBalance_from_tecnocom() throws Exception {

    PrepaidUser10 prepaidUser10 =buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(account.getAccountNumber() , prepaidCard10, impfac);

    assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    AccountEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS = 3000;

    NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac);
    NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
    NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      assertFalse("No debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    Thread.sleep(AccountEJBBean10.BALANCE_CACHE_EXPIRATION_MILLISECONDS + 1000);

    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      assertTrue("Debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }

    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      assertFalse("No debe ser actualizado desde tecnocom", prepaidBalance10.isUpdated());
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_prepaidUser_null() throws Exception {
    Account account = new Account();
    try {
      getAccountEJBBean10().getBalance(null, null, account);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_prepaidUser_documentNumber_null() throws Exception {
    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    Account account = new Account();
    try {
      getAccountEJBBean10().getBalance(null, prepaidUser10, account);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_prepaidUser_documentNumber_empty() throws Exception {
    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    prepaidUser10.setDocumentNumber("");
    Account account = new Account();
    try {
      getAccountEJBBean10().getBalance(null, prepaidUser10, account);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_account_null() throws Exception {
    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    try {
      getAccountEJBBean10().getBalance(null, prepaidUser10, null);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_account_id_null() throws Exception {
    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    Account account = new Account();
    try {
      getAccountEJBBean10().getBalance(null, prepaidUser10, account);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_account_accountNumber_null() throws Exception {
    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    try {
      getAccountEJBBean10().getBalance(null, prepaidUser10, null);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_account_accountNumber_empty() throws Exception {
    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("");
    try {
      getAccountEJBBean10().getBalance(null, prepaidUser10, null);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test
  public void getBalance_ok() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account.getAccountNumber());
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setProcessorUserId(account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    assertTrue("Saldo debe ser empty o null", StringUtils.isAllBlank(account.getBalanceInfo()));
    assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, prepaidUser10, account);

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
    }

    final PrepaidBalanceInfo10 newBalance = newBalance();

    //actualizar saldo
    {
      getAccountEJBBean10().updateBalance( account.getId(), newBalance);

      account = getAccountEJBBean10().findById(account.getId());

      assertEquals("Saldo debe ser igual", newBalance, JsonUtils.getJsonParser().fromJson(account.getBalanceInfo(), PrepaidBalanceInfo10.class));
      assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", account.getExpireBalance() > Instant.now().toEpochMilli());
    }

    //obtener nuevo salo
    {

      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, prepaidUser10, account);

      BigDecimal balanceValue = BigDecimal.valueOf(newBalance.getSaldisconp().longValue() - newBalance.getSalautconp().longValue());

      if(balanceValue.compareTo(BigDecimal.ZERO) < 0) {
        balanceValue = balanceValue.multiply(BigDecimal.valueOf(-1));
      }

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(balanceValue);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
    }
  }
}
