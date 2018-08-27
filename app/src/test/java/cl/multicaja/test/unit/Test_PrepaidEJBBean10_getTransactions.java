package cl.multicaja.test.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.ConsultaMovimientosDTO;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import cl.multicaja.tecnocom.dto.MovimientosDTO;
import cl.multicaja.tecnocom.model.response.Response;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.Rut;
import cl.multicaja.users.model.v10.Timestamps;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.core.model.Errors.TARJETA_PRIMERA_CARGA_EN_PROCESO;
import static cl.multicaja.core.model.Errors.TARJETA_PRIMERA_CARGA_PENDIENTE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Test_PrepaidEJBBean10_getTransactions {

  @Spy
  UsersEJBBean10 usersEJBBean10;

  @Spy
  PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @Spy
  PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @Spy
  PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @Spy
  TecnocomService tecnocomService;

  @InjectMocks
  @Spy
  PrepaidEJBBean10 prepaidEJBBean10;

  @Test(expected = BadRequestException.class)
  public void userIdMcNull() throws Exception {
    try{
      prepaidEJBBean10.getTransactions(null, null,null,null,0);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test
  public void userMcDisabled() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DISABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);

    try{
      prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcLocked() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.LOCKED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);

    try{
      prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc locked", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcDeleted() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DELETED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);

    try{
     prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc deleted", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcPreregistered() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.PREREGISTERED);


    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);

    try{
     prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc preregistered", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserNull() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(null).when(prepaidUserEJBBean10).getPrepaidUserById(null, 11111111L);

    try{
     prepaidEJBBean10.getTransactions(null, 11111111L,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserDisabled() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(Mockito.any(), Mockito.anyLong());

    try{
     prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser disabled", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  /*
    Usuario sin balance consultado previamente
   */

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

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(Mockito.any(), Mockito.anyLong());
    Mockito.doReturn(prepaidCard).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);

    try{
     prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidCard pending", TARJETA_PRIMERA_CARGA_EN_PROCESO.getValue(), ex.getCode());
    }
  }

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

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(Mockito.any(), Mockito.anyLong());
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);

    try{
     prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error first topup pending", TARJETA_PRIMERA_CARGA_PENDIENTE.getValue(), ex.getCode());
    }
  }

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

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(Mockito.any(), Mockito.anyLong());
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(null, Long.MAX_VALUE);
    Mockito.doReturn(prepaidMovement).when(prepaidMovementEJBBean10).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);

    try{
     prepaidEJBBean10.getTransactions(null, Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error first topup in process", TARJETA_PRIMERA_CARGA_EN_PROCESO.getValue(), ex.getCode());
    }

  }

  @Test
  public void getTransactionOk() throws Exception {
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

    Timestamps timestamps = new Timestamps();
    timestamps.setCreatedAt(new Timestamp(new Date().getTime()));
    prepaidCard10.setTimestamps(timestamps);

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("000");
    response.getRunServiceResponse().getReturn().setTotalRegistros(2L);
    ConsultaMovimientosDTO dto = new ConsultaMovimientosDTO(response);
    List<MovimientosDTO> movimientosDTOS = new ArrayList<>();

    HashMap<String,Object> fecFac = new HashMap<>();
    fecFac.put("valueDate","17-07-2018");
    fecFac.put("format","dd-MM-yyyy");

    MovimientosDTO movimientosDTO = new MovimientosDTO();
    movimientosDTO.setTipofac(3001);
    movimientosDTO.setIndnorcor(0);
    movimientosDTO.setImporte(new BigDecimal(4500));
    movimientosDTO.setDestipfac("Carga");
    movimientosDTO.setFecfac(fecFac);
    movimientosDTO.setIndnorcor(0);
    movimientosDTOS.add(movimientosDTO);

    movimientosDTO = new MovimientosDTO();
    movimientosDTO.setTipofac(3002);
    movimientosDTO.setIndnorcor(0);
    movimientosDTO.setImporte(new BigDecimal(4500));
    movimientosDTO.setDestipfac("Retiro");
    movimientosDTO.setFecfac(fecFac);
    movimientosDTOS.add(movimientosDTO);

    movimientosDTO = new MovimientosDTO();
    movimientosDTO.setTipofac(3003);
    movimientosDTO.setIndnorcor(0);
    movimientosDTO.setImporte(new BigDecimal(4500));
    movimientosDTO.setDestipfac("Compra");
    movimientosDTOS.add(movimientosDTO);
    movimientosDTO.setFecfac(fecFac);
    dto.setMovimientos(movimientosDTOS);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(new HashMap<>(), Long.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(new HashMap<>(), Long.MAX_VALUE);
    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean10).getLastPrepaidCardByUserId(new HashMap<>(),Long.MAX_VALUE);
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long.MAX_VALUE, PrepaidMovementStatus.PENDING, PrepaidMovementStatus.IN_PROCESS);
    Mockito.doReturn(dto).when(tecnocomService).consultaMovimientos(Mockito.any(), Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());

    try {
      List<PrepaidTransaction10> prepaidTransaction10 = prepaidEJBBean10.getTransactions(new HashMap<>(), Long.MAX_VALUE,"","",Integer.MAX_VALUE);
      Assert.assertNotNull("Deberia retornar el listado de transacciones", prepaidTransaction10);
      Assert.assertEquals("Deberian ser 3",3, prepaidTransaction10.size());
    } catch (Exception ex) {
      ex.printStackTrace();
      Assert.fail("should not be here "+ex);
    }

  }
}