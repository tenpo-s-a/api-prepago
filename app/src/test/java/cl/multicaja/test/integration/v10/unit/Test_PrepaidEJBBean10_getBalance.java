package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Test_PrepaidEJBBean10_getBalance extends TestBaseUnit {

  private PrepaidBalanceInfo10 newBalance() {
    return new PrepaidBalanceInfo10(152, 152,
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)));
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_userUuid_null() throws Exception {
    try {
      getPrepaidEJBBean10().getAccountBalance(null, null, "");
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_userUuid_empty() throws Exception {
    try {
      getPrepaidEJBBean10().getAccountBalance(null, "", "");
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_accountUuid_null() throws Exception {
    try {
      getPrepaidEJBBean10().getAccountBalance(null, UUID.randomUUID().toString(), null);
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void getBalance_accountUuid_empty() throws Exception {

    try {
      getPrepaidEJBBean10().getAccountBalance(null, UUID.randomUUID().toString(), "");
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error cuenta no existe", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test
  public void getBalance_ok() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser10,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    assertTrue("Saldo debe ser empty o null", StringUtils.isAllBlank(account.getBalanceInfo()));
    assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidUser10.getUuid(), prepaidCard10, impfac);

    assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    {
      PrepaidBalance10 prepaidBalance10 = getPrepaidEJBBean10().getAccountBalance(null, prepaidUser10.getUuid(), account.getUuid());

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

      PrepaidBalance10 prepaidBalance10 = getPrepaidEJBBean10().getAccountBalance(null, prepaidUser10.getUuid(), account.getUuid());

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
