package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static cl.multicaja.core.model.Errors.CUENTA_NO_EXISTE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static org.junit.Assert.*;

public class Test_AccountEJBBean10_expireBalanceCache extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void expireBalanceCache_accountId_null() throws Exception {
    try {
      getAccountEJBBean10().expireBalanceCache(null);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error accountId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void expireBalanceCache_account_null() throws Exception {
    try {
      getAccountEJBBean10().getBalance(null, Long.MAX_VALUE);
    } catch (ValidationException ex) {
      assertEquals("Debe retornar error cuenta no existe", CUENTA_NO_EXISTE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test
  public void expireBalanceCache_ok() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    assertTrue("Saldo debe ser empty o null", StringUtils.isAllBlank(account.getBalanceInfo()));
    assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(account.getAccountNumber(), prepaidCard10, impfac);

    assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      assertTrue(prepaidBalance10.isUpdated());

      Account acc = getAccountEJBBean10().findById(account.getId());
      assertNotNull(acc);
      assertTrue(acc.getExpireBalance() > Instant.now().toEpochMilli());
    }

    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      assertFalse(prepaidBalance10.isUpdated());

      Account acc = getAccountEJBBean10().findById(account.getId());
      assertNotNull(acc);
      assertTrue(acc.getExpireBalance() > Instant.now().toEpochMilli());
    }

    //expirar el balance
    {
      try {
        getAccountEJBBean10().expireBalanceCache(account.getId());
        Account acc = getAccountEJBBean10().findById(account.getId());
        assertNotNull(acc);
        assertTrue(acc.getExpireBalance() <= Instant.now().toEpochMilli());
      } catch(ValidationException vex) {
        fail("Should not be here");
      }
    }

    //obtener de nuevo el saldo
    {
      PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

      NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(impfac);
      NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
      NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

      assertEquals("Debe ser igual", balance, prepaidBalance10.getBalance());
      assertEquals("Debe ser igual", pcaMain, prepaidBalance10.getPcaMain());
      assertEquals("Debe ser igual", pcaSecondary, prepaidBalance10.getPcaSecondary());
      assertTrue(prepaidBalance10.isUpdated());

      Account acc = getAccountEJBBean10().findById(account.getId());
      assertNotNull(acc);
      assertTrue(acc.getExpireBalance() > Instant.now().toEpochMilli());
    }
  }
}
