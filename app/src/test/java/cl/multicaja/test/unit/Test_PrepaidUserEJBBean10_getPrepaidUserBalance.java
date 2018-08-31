package cl.multicaja.test.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import cl.multicaja.tecnocom.model.response.Response;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class Test_PrepaidUserEJBBean10_getPrepaidUserBalance {

  @Spy
  UserClient userClient;

  @Spy
  PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @Spy
  PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @Spy
  TecnocomService tecnocomService;

  @InjectMocks
  @Spy
  private PrepaidUserEJBBean10 prepaidUserEJB10;

  @Test(expected = BadRequestException.class)
  public void userIdMcNull() throws Exception {
    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void userMcNull() throws Exception {
    Mockito.doReturn(null).when(userClient).getUserById(null, Long.MAX_VALUE);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error user null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcDisabled() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DISABLED);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcLocked() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.LOCKED);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc locked", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcDeleted() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DELETED);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc deleted", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcPreregistered() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.PREREGISTERED);


    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc preregistered", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void prepaidUserNull() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(null).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser null", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void prepaidUserDisabled() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser disabled", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  /*
    Usuario sin balance consultado previamente
   */

  //TODO: verificar falla
  @Ignore
  @Test
  public void prepaidCardPending() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(prepaidCard).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidCard pending", TARJETA_PRIMERA_CARGA_EN_PROCESO.getValue(), ex.getCode());
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void firstTopupPending() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error first topup pending", TARJETA_PRIMERA_CARGA_PENDIENTE.getValue(), ex.getCode());
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void firstTopupInProcess() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidMovement).when(prepaidMovementEJBBean10).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error first topup in process", TARJETA_PRIMERA_CARGA_EN_PROCESO.getValue(), ex.getCode());
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void consultaSaldoNull() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setRut(11111111);

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(null).when(tecnocomService).consultaSaldo("1", "111111111", TipoDocumento.RUT);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error saldo no disponible", SALDO_NO_DISPONIBLE_$VALUE.getValue(), ex.getCode());
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void consultaSaldoError() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setRut(11111111);

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("200");
    response.getRunServiceResponse().getReturn().setDescRetorno("");

    ConsultaSaldoDTO dto = new ConsultaSaldoDTO(response);

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);

    try{
      prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error saldo no disponible", SALDO_NO_DISPONIBLE_$VALUE.getValue(), ex.getCode());
    }
  }

  //TODO: verificar falla
  @Ignore
  @Test
  public void consultaSaldoOk() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setRut(11111111);

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");

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

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    Mockito.doNothing().when(prepaidUserEJB10).updatePrepaidUserBalance(Mockito.any(), Mockito.isA(Long.class), Mockito.isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);

      Mockito.verify(prepaidCardEJBBean10, Mockito.times(1)).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
      Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);
      Mockito.verify(tecnocomService, Mockito.times(1)).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      Assert.assertNotNull("Deberia retornar el balance", balance);
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      Assert.fail("should not be here");
    }
  }

  /*
    Usuario con balance expirado.
    balanceExpiration = 0
   */
  //TODO: verificar falla
  @Ignore
  @Test
  public void balanceExpiration0() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setRut(11111111);

    PrepaidBalanceInfo10 balanceInfo10 = new PrepaidBalanceInfo10();
    balanceInfo10.setClamonp(0);
    balanceInfo10.setClamons(0);
    balanceInfo10.setConprod("");
    balanceInfo10.setProducto("");
    balanceInfo10.setSubprodu("");

    prepaidUser.setBalance(balanceInfo10);
    prepaidUser.setBalanceExpiration(0L);

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");

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

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    Mockito.doNothing().when(prepaidUserEJB10).updatePrepaidUserBalance(Mockito.any(), Mockito.isA(Long.class), Mockito.isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);

      Mockito.verify(prepaidCardEJBBean10, Mockito.times(1)).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
      Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);
      Mockito.verify(tecnocomService, Mockito.times(1)).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      Assert.assertNotNull("Deberia retornar el balance", balance);
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      Assert.fail("should not be here");
    }
  }

  /*
    Usuario con balance expirado.
    System.currentTimeMillis() >= balanceExpiration
   */
  //TODO: verificar falla
  @Ignore
  @Test
  public void balanceExpired() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setRut(11111111);

    PrepaidBalanceInfo10 balanceInfo10 = new PrepaidBalanceInfo10();
    balanceInfo10.setClamonp(0);
    balanceInfo10.setClamons(0);
    balanceInfo10.setConprod("");
    balanceInfo10.setProducto("");
    balanceInfo10.setSubprodu("");

    prepaidUser.setBalance(balanceInfo10);
    prepaidUser.setBalanceExpiration(System.currentTimeMillis() - 1);

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");

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

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    Mockito.doNothing().when(prepaidUserEJB10).updatePrepaidUserBalance(Mockito.any(), Mockito.isA(Long.class), Mockito.isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);

      Mockito.verify(prepaidCardEJBBean10, Mockito.times(1)).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
      Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);
      Mockito.verify(tecnocomService, Mockito.times(1)).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      Assert.assertNotNull("Deberia retornar el balance", balance);
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      Assert.fail("should not be here");
    }
  }

  /*
    Usuario con balance cacheado.
   */
  //TODO: verificar falla
  @Ignore
  @Test
  public void balanceCached() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setRut(11111111);

    PrepaidBalanceInfo10 balanceInfo10 = new PrepaidBalanceInfo10();
    balanceInfo10.setClamonp(0);
    balanceInfo10.setClamons(0);
    balanceInfo10.setConprod("");
    balanceInfo10.setProducto("");
    balanceInfo10.setSubprodu("");

    prepaidUser.setBalance(balanceInfo10);
    prepaidUser.setBalanceExpiration(System.currentTimeMillis() + 10000);

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");

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

    Mockito.doReturn(user).when(userClient).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJB10).getPrepaidUserByRut(null, 11111111);
    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(dto).when(tecnocomService).consultaSaldo("1", "11111111", TipoDocumento.RUT);
    Mockito.doNothing().when(prepaidUserEJB10).updatePrepaidUserBalance(Mockito.any(), Mockito.isA(Long.class), Mockito.isA(PrepaidBalanceInfo10.class));

    try{
      PrepaidBalance10 balance = prepaidUserEJB10.getPrepaidUserBalance(null, Long.MAX_VALUE);

      Mockito.verify(prepaidCardEJBBean10, Mockito.never()).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
      Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);
      Mockito.verify(tecnocomService, Mockito.never()).consultaSaldo("1", "11111111", TipoDocumento.RUT);

      Assert.assertNotNull("Deberia retornar el balance", balance);
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getBalance().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0), balance.getPcaMain().getValue());
      Assert.assertEquals("Debe ser saldo 0", BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), balance.getPcaSecondary().getValue());

    } catch (Exception ex) {
      Assert.fail("should not be here");
    }
  }
}
