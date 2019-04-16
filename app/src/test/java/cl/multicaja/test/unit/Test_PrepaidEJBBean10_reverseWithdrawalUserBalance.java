package cl.multicaja.test.unit;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.*;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.apache.commons.lang3.RandomStringUtils;
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

/**
 * @author abarazarte
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class Test_PrepaidEJBBean10_reverseWithdrawalUserBalance {

  @Spy
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @Spy
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;


  @Spy
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  @Spy
  private PrepaidTopupDelegate10 delegate;

  @Spy
  private ParametersUtil parametersUtil;

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
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, "AA",null,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error request null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "withdrawRequest", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestAmountNull() throws Exception {
    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, "AA", reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "amount", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestAmountValueNull() throws Exception {
    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    reverseRequest.setAmount(amount);
    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers,"AA",  reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "amount.value", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestAmountCurrencyCodeNull() throws Exception {
    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers,"AA",  reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "amount.currency_code", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestMerchantCodeNull() throws Exception {
    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);
    reverseRequest.setPassword("1234");

    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers,"AA",  reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "merchant_code", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestMerchantNameNull() throws Exception {
    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");

    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers,"AA",  reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "merchant_name", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestMerchantCategoryNull() throws Exception {
    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");

    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers,"AA",  reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "merchant_category", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestTransactionIdNull() throws Exception {

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);

    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers,"AA",  reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "transaction_id", String.valueOf(ex.getData()[0].getValue()));
    }
  }


  @Test
  public void prepaidUserNull() throws Exception {


    String uuid =  RandomStringUtils.random(10);

    Mockito.doReturn(null).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, uuid,  reverseRequest,true);
      Assert.fail("should not be here");

    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser null", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserDisabled() throws Exception {

    String uuid =  RandomStringUtils.random(10);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(1L);
    prepaidUser.setUuid(uuid);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);


    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, uuid, reverseRequest,true);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser disabled", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }


  @Test
  public void originalWithdrawAlreadyReversed() throws Exception {

    String uuid =  RandomStringUtils.random(10);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setUuid(uuid);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);


    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 originalWithdraw = new PrepaidMovement10();
    originalWithdraw.setMonto(BigDecimal.TEN);
    originalWithdraw.setFechaCreacion(Timestamp.from(ZonedDateTime.now().toInstant()));
    originalWithdraw.setIdPrepaidUser(Long.MAX_VALUE);
    originalWithdraw.setIdTxExterno("0987654321");

    PrepaidMovement10 reverse = new PrepaidMovement10();

    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    Mockito.doReturn(originalWithdraw).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321", PrepaidMovementType.WITHDRAW,
      TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321", PrepaidMovementType.WITHDRAW,
      TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.TEN);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try {
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, uuid, reverseRequest,true);
    } catch(ReverseAlreadyReceivedException ex) {
      Assert.assertEquals("Debe retornar error de reversa ya recibida", REVERSA_RECIBIDA_PREVIAMENTE.getValue(), ex.getCode());
    }

    Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
  }

  @Test
  public void originalWithdrawNull() throws Exception {

    String uuid =  RandomStringUtils.random(10);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setUuid(uuid);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);


    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 reverse = new PrepaidMovement10();
    reverse.setId(Long.MAX_VALUE);

    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    // PrepaidMovement - Revesa Retiro
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    // PrepaidMovement - Retiro
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.doNothing().when(prepaidMovementEJBBean10).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));

    Mockito.doReturn("0987").when(parametersUtil).getString("api-prepaid", "cod_entidad", "v10");

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try {
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, uuid, reverseRequest,true);
    } catch (ReverseOriginalMovementNotFoundException ex) {
      Assert.assertEquals("Debe retornar error de movimiento original no recibido", REVERSA_MOVIMIENTO_ORIGINAL_NO_RECIBIDO.getValue(), ex.getCode());
    }

    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));
  }

  @Test
  public void originalWithdrawAmountMismatch() throws Exception {
    String uuid =  RandomStringUtils.random(10);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setUuid(uuid);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);


    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);

    PrepaidMovement10 reverse = new PrepaidMovement10();
    reverse.setId(Long.MAX_VALUE);


    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);


    // PrepaidMovement - Revesa Retiro
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    // PrepaidMovement - Retiro
    Mockito.doReturn(originalTopup).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.doNothing().when(prepaidMovementEJBBean10).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));

    Mockito.doReturn("0987").when(parametersUtil).getString("api-prepaid", "cod_entidad", "v10");

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try {
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, uuid, reverseRequest,true);
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error de monto no concuerda", REVERSA_INFORMACION_NO_CONCUERDA.getValue(), ex.getCode());
    }

    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));
  }

  @Test
  public void originalWithdrawReverseTimeExpired() throws Exception {
    String uuid =  RandomStringUtils.random(10);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setUuid(uuid);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);


    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);
    // fecha creacion
    originalTopup.setFechaCreacion(Timestamp.from(ZonedDateTime.now().minusHours(24).minusSeconds(1).toInstant()));

    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    // PrepaidMovement - Reversa
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    //PrepaidMovement - Retiro
    Mockito.doReturn(originalTopup).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.TEN);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try {
      prepaidEJBBean10.reverseWithdrawUserBalance(headers, uuid, reverseRequest,true);
      Assert.fail("Sould not be here");
    } catch (ReverseTimeExpiredException ex) {
      Assert.assertEquals("Deberia tener error de transaccion", REVERSA_TIEMPO_EXPIRADO.getValue(), ex.getCode());
      Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
        PrepaidMovementType.WITHDRAW, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);
      Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
        PrepaidMovementType.WITHDRAW, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);
      Mockito.verify(prepaidMovementEJBBean10, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    }
  }

  @Test
  public void reverseWithdraw() throws Exception {
    String uuid =  RandomStringUtils.random(10);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setUuid(uuid);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);


    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);
    originalTopup.setFechaCreacion(Timestamp.from(ZonedDateTime.now().toInstant()));

    NewPrepaidWithdraw10 reverseRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.TEN);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setPassword("1234");
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setTransactionReference(1234L);
    cdtTransaction.setExternalTransactionId(reverseRequest.getTransactionId());

    PrepaidMovement10 reverse = new PrepaidMovement10();
    reverse.setId(Long.MAX_VALUE);


    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    // PrepaidMovement - Reversa
    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    // PrepaidMovement - Retiro
    Mockito.doReturn(originalTopup).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean10).getPrepaidMovementById(Long.MAX_VALUE);

    Mockito.doReturn("0987").when(parametersUtil).getString("api-prepaid", "cod_entidad", "v10");

    Mockito.doReturn("123456789")
      .when(delegate).sendPendingWithdrawReversal(Mockito.any(), Mockito.any(), Mockito.any());

    prepaidEJBBean10.reverseWithdrawUserBalance(headers, uuid, reverseRequest,true);

    // Se verifica que se llamaron los metodos
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).getPrepaidMovementForReverse(Long.MAX_VALUE, "0987654321",
      PrepaidMovementType.WITHDRAW, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);
    Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(delegate, Mockito.times(1)).sendPendingWithdrawReversal(Mockito.any(), Mockito.any(), Mockito.any());

  }

}
