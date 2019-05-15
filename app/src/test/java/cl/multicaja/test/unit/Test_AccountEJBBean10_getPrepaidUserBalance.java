package cl.multicaja.test.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.ejb.v10.AccountEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import cl.multicaja.tecnocom.model.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import static cl.multicaja.core.model.Errors.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author abarazarte
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class Test_AccountEJBBean10_getPrepaidUserBalance {

  @Spy
  PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @Spy
  TecnocomService tecnocomService;

  @InjectMocks
  @Spy
  private AccountEJBBean10 accountEJBBean10;

  @Test(expected = BadRequestException.class)
  public void accountIdNull() throws Exception {
    try{
      accountEJBBean10.getBalance(null, null);
      fail("should not be here");
    } catch (BadRequestException ex) {
      assertEquals("Debe retornar error accountId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = NotFoundException.class)
  public void accountNull() throws Exception {

    doReturn(null).when(accountEJBBean10).findById(Long.MAX_VALUE);

    try{
      accountEJBBean10.getBalance(null, Long.MAX_VALUE);
      fail("should not be here");
    } catch (NotFoundException ex) {
      assertEquals("Debe retornar error cuenta no existe", CUENTA_NO_EXISTE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = NotFoundException.class)
  public void prepaidUserNull() throws Exception {

    Account account = new Account();
    account.setUserId(Long.MAX_VALUE);

    doReturn(account).when(accountEJBBean10).findById(Long.MAX_VALUE);

    try{
      accountEJBBean10.getBalance(null, Long.MAX_VALUE);
      fail("should not be here");
    } catch (NotFoundException ex) {
      assertEquals("Debe retornar error prepaidUser null", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
      throw  ex;
    }
  }

  /*
    Usuario sin balance consultado previamente
   */

  @Test(expected = ValidationException.class)
  public void consultaSaldoNull() throws Exception {

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setUserId(Long.MAX_VALUE);
    account.setAccountNumber("1");

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setDocumentNumber("11111111");

    doReturn(account).when(accountEJBBean10).findById(Long.MAX_VALUE);
    doReturn(prepaidUser).when(prepaidUserEJBBean10).findById(null, Long.MAX_VALUE);
    doReturn(null).when(tecnocomService).consultaSaldo("1", "111111111", TipoDocumento.RUT);

    try{
      accountEJBBean10.getBalance(null, Long.MAX_VALUE);
      fail("should not be here");
    } catch (ValidationException ex) {
      assertEquals("Debe retornar error saldo no disponible", SALDO_NO_DISPONIBLE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test(expected = ValidationException.class)
  public void consultaSaldoError() throws Exception {

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setUserId(Long.MAX_VALUE);
    account.setAccountNumber("1");

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setDocumentNumber("11111111");

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("200");
    response.getRunServiceResponse().getReturn().setDescRetorno("");

    ConsultaSaldoDTO dto = new ConsultaSaldoDTO(response);

    doReturn(account).when(accountEJBBean10).findById(Long.MAX_VALUE);
    doReturn(prepaidUser).when(prepaidUserEJBBean10).findById(null, Long.MAX_VALUE);
    doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);

    try{
      accountEJBBean10.getBalance(null, Long.MAX_VALUE);
      fail("should not be here");
    } catch (ValidationException ex) {
      assertEquals("Debe retornar error saldo no disponible", SALDO_NO_DISPONIBLE_$VALUE.getValue(), ex.getCode());
      throw ex;
    }
  }

  @Test
  public void consultaSaldoOk() throws Exception {

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setUserId(Long.MAX_VALUE);
    account.setAccountNumber("1");

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setDocumentNumber("11111111");

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("000");
    response.getRunServiceResponse().getReturn().getContratos().setClamonp(0);
    response.getRunServiceResponse().getReturn().getContratos().setClamons(0);
    response.getRunServiceResponse().getReturn().getContratos().setConprod("");
    response.getRunServiceResponse().getReturn().getContratos().setProducto("");
    response.getRunServiceResponse().getReturn().getContratos().setSalautconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSalautcons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldisconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldiscons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSubprodu("");

    ConsultaSaldoDTO dto = new ConsultaSaldoDTO(response);

    doReturn(account).when(accountEJBBean10).findById(Long.MAX_VALUE);
    doReturn(prepaidUser).when(prepaidUserEJBBean10).findById(null, Long.MAX_VALUE);
    doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    doNothing().when(accountEJBBean10).updateBalance(isA(Long.class), isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = accountEJBBean10.getBalance(null, Long.MAX_VALUE);

      verify(accountEJBBean10, times(1)).findById(Long.MAX_VALUE);
      verify(prepaidUserEJBBean10, times(1)).findById(null, Long.MAX_VALUE);
      verify(tecnocomService, times(1)).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      assertNotNull("Deberia retornar el balance", balance);
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      fail("should not be here");
    }
  }

  /*
    Usuario con balance expirado.
    balanceExpiration = 0
   */
  @Test
  public void balanceExpiration0() throws Exception {

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setUserId(Long.MAX_VALUE);
    account.setAccountNumber("1");

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setDocumentNumber("11111111");

    PrepaidBalanceInfo10 balanceInfo10 = new PrepaidBalanceInfo10();
    balanceInfo10.setClamonp(0);
    balanceInfo10.setClamons(0);
    balanceInfo10.setConprod("");
    balanceInfo10.setProducto("");
    balanceInfo10.setSubprodu("");

    account.setBalanceInfo(JsonUtils.getJsonParser().toJson(balanceInfo10));
    account.setExpireBalance(0L);

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("000");
    response.getRunServiceResponse().getReturn().setDescRetorno("Operacion Exitosa");
    response.getRunServiceResponse().getReturn().getContratos().setClamonp(0);
    response.getRunServiceResponse().getReturn().getContratos().setClamons(0);
    response.getRunServiceResponse().getReturn().getContratos().setConprod("");
    response.getRunServiceResponse().getReturn().getContratos().setProducto("");
    response.getRunServiceResponse().getReturn().getContratos().setSalautconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSalautcons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldisconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldiscons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSubprodu("");

    ConsultaSaldoDTO dto = new ConsultaSaldoDTO(response);

    doReturn(account).when(accountEJBBean10).findById(Long.MAX_VALUE);
    doReturn(prepaidUser).when(prepaidUserEJBBean10).findById(null, Long.MAX_VALUE);
    doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    doNothing().when(accountEJBBean10).updateBalance(isA(Long.class), isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = accountEJBBean10.getBalance(null, Long.MAX_VALUE);

      verify(accountEJBBean10, times(1)).findById(Long.MAX_VALUE);
      verify(prepaidUserEJBBean10, times(1)).findById(null, Long.MAX_VALUE);
      verify(tecnocomService, times(1)).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      assertNotNull("Deberia retornar el balance", balance);
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      fail("should not be here");
    }
  }

  /*
    Usuario con balance expirado.
    System.currentTimeMillis() >= balanceExpiration
   */
  @Test
  public void balanceExpired() throws Exception {

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setUserId(Long.MAX_VALUE);
    account.setAccountNumber("1");

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setDocumentNumber("11111111");

    PrepaidBalanceInfo10 balanceInfo10 = new PrepaidBalanceInfo10();
    balanceInfo10.setClamonp(0);
    balanceInfo10.setClamons(0);
    balanceInfo10.setConprod("");
    balanceInfo10.setProducto("");
    balanceInfo10.setSubprodu("");

    account.setBalanceInfo(JsonUtils.getJsonParser().toJson(balanceInfo10));
    account.setExpireBalance(Instant.now().toEpochMilli() - 1);

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("000");
    response.getRunServiceResponse().getReturn().getContratos().setClamonp(0);
    response.getRunServiceResponse().getReturn().getContratos().setClamons(0);
    response.getRunServiceResponse().getReturn().getContratos().setConprod("");
    response.getRunServiceResponse().getReturn().getContratos().setProducto("");
    response.getRunServiceResponse().getReturn().getContratos().setSalautconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSalautcons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldisconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldiscons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSubprodu("");

    ConsultaSaldoDTO dto = new ConsultaSaldoDTO(response);

    doReturn(account).when(accountEJBBean10).findById(Long.MAX_VALUE);
    doReturn(prepaidUser).when(prepaidUserEJBBean10).findById(null, Long.MAX_VALUE);
    doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    doNothing().when(accountEJBBean10).updateBalance(isA(Long.class), isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = accountEJBBean10.getBalance(null, Long.MAX_VALUE);

      verify(accountEJBBean10, times(1)).findById(Long.MAX_VALUE);
      verify(prepaidUserEJBBean10, times(1)).findById(null, Long.MAX_VALUE);
      verify(tecnocomService, times(1)).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      assertNotNull("Deberia retornar el balance", balance);
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      fail("should not be here");
    }
  }

  /*
    Usuario con balance cacheado.
   */
  @Test
  public void balanceCached() throws Exception {

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setUserId(Long.MAX_VALUE);
    account.setAccountNumber("1");

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setDocumentNumber("11111111");

    PrepaidBalanceInfo10 balanceInfo10 = new PrepaidBalanceInfo10();
    balanceInfo10.setClamonp(0);
    balanceInfo10.setClamons(0);
    balanceInfo10.setConprod("");
    balanceInfo10.setProducto("");
    balanceInfo10.setSubprodu("");

    account.setBalanceInfo(JsonUtils.getJsonParser().toJson(balanceInfo10));
    account.setExpireBalance(Instant.now().plusMillis(10000).toEpochMilli());

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("000");
    response.getRunServiceResponse().getReturn().getContratos().setClamonp(0);
    response.getRunServiceResponse().getReturn().getContratos().setClamons(0);
    response.getRunServiceResponse().getReturn().getContratos().setConprod("");
    response.getRunServiceResponse().getReturn().getContratos().setProducto("");
    response.getRunServiceResponse().getReturn().getContratos().setSalautconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSalautcons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldisconp(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSaldiscons(BigDecimal.ZERO);
    response.getRunServiceResponse().getReturn().getContratos().setSubprodu("");

    ConsultaSaldoDTO dto = new ConsultaSaldoDTO(response);

    doReturn(account).when(accountEJBBean10).findById(Long.MAX_VALUE);
    doReturn(prepaidUser).when(prepaidUserEJBBean10).findById(null, Long.MAX_VALUE);
    doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    doNothing().when(accountEJBBean10).updateBalance(isA(Long.class), isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = accountEJBBean10.getBalance(null, Long.MAX_VALUE);

      verify(accountEJBBean10, times(1)).findById(Long.MAX_VALUE);
      verify(prepaidUserEJBBean10, times(1)).findById(null, Long.MAX_VALUE);
      verify(tecnocomService, never()).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      assertNotNull("Deberia retornar el balance", balance);
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      fail("should not be here");
    }
  }
}
