package cl.multicaja.test.unit;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.Rut;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserIdentityStatus;
import cl.multicaja.users.model.v10.UserStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.core.model.Errors.CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO;

/**
 * @author abarazarte
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class Test_PrepaidEJBBean10_reverseTopupUserBalance {

  @Spy
  private UsersEJBBean10 usersEJBBean10;

  @Spy
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @Spy
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @Spy
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @Spy
  @InjectMocks
  private PrepaidEJBBean10 prepaidEJBBean10;

  private static Map<String, Object> headers;

  @BeforeClass
  public static void setup() {
    headers = new HashMap<>();
    headers.put(Constants.HEADER_USER_TIMEZONE, "America/Santiago");
  }

  @Test
  public void reverseRequestNull() throws Exception {
    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error request null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "topupRequest", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestAmountNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "amount", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestAmountValueNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    reverseRequest.setAmount(amount);
    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "amount.value", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestAmountCurrencyCodeNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "amount.currency_code", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestRutNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "rut", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestMerchantCodeNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "merchant_code", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestMerchantNameNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);
    reverseRequest.setMerchantCode("1234567890");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "merchant_name", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestMerchantCategoryNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "merchant_category", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestTransactionIdNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "transaction_id", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void userMcNull() throws Exception {

    Mockito.doReturn(null).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error user null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcDisabled() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DISABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcLocked() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.LOCKED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc locked", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcDeleted() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DELETED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc deleted", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcPreregistered() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.PREREGISTERED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc preregistered", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcBlacklisted() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DISABLED);
    user.setIdentityStatus(UserIdentityStatus.TERRORIST);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserNull() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(Integer.MAX_VALUE);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.setId(Long.MAX_VALUE);
    user.setIdentityStatus(UserIdentityStatus.NORMAL);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);
    Mockito.doReturn(null).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(headers, Long.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser null", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserDisabled() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(Integer.MAX_VALUE);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.setId(Long.MAX_VALUE);
    user.setIdentityStatus(UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(headers, Long.MAX_VALUE);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser disabled", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void originalTopupAlreadyReversed() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(Integer.MAX_VALUE);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.setId(Long.MAX_VALUE);
    user.setIdentityStatus(UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);
    originalTopup.setFechaCreacion(Timestamp.from(ZonedDateTime.now().toInstant()));

    PrepaidMovement10 reverse = new PrepaidMovement10();

    // UserMc
    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(headers, Long.MAX_VALUE);

    // PrepaidCard
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    // Reverse
    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.TEN);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");


    prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);

    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
  }

  @Test
  public void originalTopupNull() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(Integer.MAX_VALUE);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.setId(Long.MAX_VALUE);
    user.setIdentityStatus(UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    PrepaidMovement10 reverse = new PrepaidMovement10();
    reverse.setId(Long.MAX_VALUE);

    // UserMc
    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(headers, Long.MAX_VALUE);

    // PrepaidCard
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    /*
      PrepaidMovement
      1. Reversa
      2. Carga original
     */
    Mockito.doReturn(null)
    .doReturn(null)
      .when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.doNothing().when(prepaidMovementEJBBean10).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);

    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));
  }

  @Test
  public void originalTopupAmountMismatch() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(Integer.MAX_VALUE);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.setId(Long.MAX_VALUE);
    user.setIdentityStatus(UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);

    PrepaidMovement10 reverse = new PrepaidMovement10();
    reverse.setId(Long.MAX_VALUE);

    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(headers, Long.MAX_VALUE);

    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    Mockito.doReturn(null)
      .doReturn(originalTopup)
      .when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.doNothing().when(prepaidMovementEJBBean10).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));


    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");


    prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);

    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));

  }

  @Test
  public void originalTopupReverseTimeExpired() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(Integer.MAX_VALUE);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.setId(Long.MAX_VALUE);
    user.setIdentityStatus(UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);

    // fecha creacion
    originalTopup.setFechaCreacion(Timestamp.from(ZonedDateTime.now().minusHours(24).minusSeconds(1).toInstant()));

    // UserMc
    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(headers, Long.MAX_VALUE);

    // PrepaidCard
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    Mockito.doReturn(null)
      .doReturn(originalTopup)
      .when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.TEN);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try {
      prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);
      Assert.fail("Sould not be here");
    } catch (BaseException ex) {
      Assert.assertEquals("Deberia tener error de transaccion", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), ex.getCode());
      Mockito.verify(prepaidMovementEJBBean10, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
        Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
      Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    }
  }

  @Test
  public void reverseTopup() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(Integer.MAX_VALUE);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);
    user.setId(Long.MAX_VALUE);
    user.setIdentityStatus(UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);
    originalTopup.setFechaCreacion(Timestamp.from(ZonedDateTime.now().toInstant()));


    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.TEN);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setTransactionReference(1234L);
    cdtTransaction.setExternalTransactionId(reverseRequest.getTransactionId());

    // UserMc
    Mockito.doReturn(user).when(usersEJBBean10).getUserByRut(headers, Integer.MAX_VALUE);

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(headers, Long.MAX_VALUE);

    // PrepaidCard
    Mockito.doReturn(null).when(prepaidCardEJBBean10).getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    /*
      PrepaidMovement
      1. Busca movimiento de reversa
      2. Busca movimineto de carga original
     */
    Mockito.doReturn(null)
      .doReturn(originalTopup).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    Mockito.doReturn(null).when(prepaidMovementEJBBean10).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));

    prepaidEJBBean10.reverseTopupUserBalance(headers, reverseRequest);

    // Se verifica que se llamaron los metodos
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidCardEJBBean10, Mockito.times(1)).getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
  }
}
