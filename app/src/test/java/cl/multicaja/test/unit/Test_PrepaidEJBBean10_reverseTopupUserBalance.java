package cl.multicaja.test.unit;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.*;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.AccountEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.ejb.v11.PrepaidMovementEJBBean11;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.constants.TipoFactura;
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
import java.util.UUID;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class Test_PrepaidEJBBean10_reverseTopupUserBalance {


  @Spy
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;


  @Spy
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  @Spy
  private PrepaidMovementEJBBean11 prepaidMovementEJBBean11;

  @Spy
  private AccountEJBBean10 accountEJBBean10;

  @Spy
  private ParametersUtil parametersUtil;

  @Spy
  private PrepaidTopupDelegate10 delegate;

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
      prepaidEJBBean10.reverseTopupUserBalance(headers, null,null,true);
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
      prepaidEJBBean10.reverseTopupUserBalance(headers, UUID.randomUUID().toString(), reverseRequest,true);
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
      prepaidEJBBean10.reverseTopupUserBalance(headers,UUID.randomUUID().toString(), reverseRequest,true);
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
      prepaidEJBBean10.reverseTopupUserBalance(headers, UUID.randomUUID().toString(),reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "amount.currency_code", String.valueOf(ex.getData()[0].getValue()));
    }
  }

  @Test
  public void reverseRequestMerchantCodeNull() throws Exception {
    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(0);

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers,UUID.randomUUID().toString(), reverseRequest,true);
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
      prepaidEJBBean10.reverseTopupUserBalance(headers, UUID.randomUUID().toString(),reverseRequest,true);
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
      prepaidEJBBean10.reverseTopupUserBalance(headers,UUID.randomUUID().toString(), reverseRequest,true);
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
      prepaidEJBBean10.reverseTopupUserBalance(headers,UUID.randomUUID().toString(), reverseRequest,true);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error amount.value null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      Assert.assertEquals("Debe tener detalle de error: request", 1, ex.getData().length);
      Assert.assertEquals("Debe tener detalle de error: request", "transaction_id", String.valueOf(ex.getData()[0].getValue()));
    }
  }


  @Test
  public void prepaidUserNull() throws Exception {

    String uuid = UUID.randomUUID().toString();
    Mockito.doReturn(null).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers,uuid, reverseRequest,true);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser null", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserDisabled() throws Exception {

    String uuid = UUID.randomUUID().toString();
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setUuid(uuid);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers, uuid);

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try{
      prepaidEJBBean10.reverseTopupUserBalance(headers,uuid, reverseRequest,true);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser disabled", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void originalTopupAlreadyReversed() throws Exception {
    String uuid = UUID.randomUUID().toString();
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
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");


    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);
    originalTopup.setFechaCreacion(Timestamp.from(ZonedDateTime.now().toInstant()));

    PrepaidMovement10 reverse = new PrepaidMovement10();

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers, uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    // Reverse
    Mockito.doReturn(reverse).when(prepaidMovementEJBBean11).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
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
      prepaidEJBBean10.reverseTopupUserBalance(headers,uuid, reverseRequest,true);
    } catch(ReverseAlreadyReceivedException ex) {
      Assert.assertEquals("Debe retornar error de reversa ya recibida", REVERSA_RECIBIDA_PREVIAMENTE.getValue(), ex.getCode());
    }

    Mockito.verify(prepaidMovementEJBBean11, Mockito.times(1)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean11, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
  }

  @Test
  public void originalTopupNull() throws Exception {
    String uuid = UUID.randomUUID().toString();

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setUuid(uuid);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);

    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 reverse = new PrepaidMovement10();
    reverse.setId(Long.MAX_VALUE);

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers, uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);



    /*
      PrepaidMovement
      1. Reversa
      2. Carga original
     */
    Mockito.doReturn(null)
    .doReturn(null)
      .when(prepaidMovementEJBBean11).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean11).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.doNothing().when(prepaidMovementEJBBean11).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));

    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try {
      prepaidEJBBean10.reverseTopupUserBalance(headers, uuid,reverseRequest,true);
    } catch (ReverseOriginalMovementNotFoundException ex) {
      Assert.assertEquals("Debe retornar error de movimiento original no recibido", REVERSA_MOVIMIENTO_ORIGINAL_NO_RECIBIDO.getValue(), ex.getCode());
    }

    Mockito.verify(prepaidMovementEJBBean11, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean11, Mockito.times(1)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(prepaidMovementEJBBean11, Mockito.times(1)).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));
  }

  @Test
  public void originalTopupAmountMismatch() throws Exception {
    String uuid = UUID.randomUUID().toString();

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setUuid(uuid);
    prepaidUser.setDocumentNumber("11111");


    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);

    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);

    PrepaidMovement10 reverse = new PrepaidMovement10();
    reverse.setId(Long.MAX_VALUE);

    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers,uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);


    Mockito.doReturn(null)
      .doReturn(originalTopup)
      .when(prepaidMovementEJBBean11).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    Mockito.doReturn(reverse).when(prepaidMovementEJBBean11).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.doNothing().when(prepaidMovementEJBBean11).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));


    NewPrepaidTopup10 reverseRequest = new NewPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.ZERO);
    reverseRequest.setAmount(amount);
    reverseRequest.setRut(Integer.MAX_VALUE);
    reverseRequest.setMerchantCode("1234567890");
    reverseRequest.setMerchantName("Test");
    reverseRequest.setMerchantCategory(1);
    reverseRequest.setTransactionId("0987654321");

    try {
      prepaidEJBBean10.reverseTopupUserBalance(headers, uuid, reverseRequest,true);
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error de monto no concuerda", REVERSA_INFORMACION_NO_CONCUERDA.getValue(), ex.getCode());
    }

    Mockito.verify(prepaidMovementEJBBean11, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
    Mockito.verify(prepaidMovementEJBBean11, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(prepaidMovementEJBBean11, Mockito.never()).updatePrepaidMovementStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(PrepaidMovementStatus.class));

  }

  @Test
  public void originalTopupReverseTimeExpired() throws Exception {

    String uuid = UUID.randomUUID().toString();

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setUuid(uuid);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);

    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");

    PrepaidMovement10 originalTopup = new PrepaidMovement10();
    originalTopup.setMonto(BigDecimal.TEN);

    // fecha creacion
    originalTopup.setFechaCreacion(Timestamp.from(ZonedDateTime.now().minusHours(24).minusSeconds(1).toInstant()));

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers, uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    Mockito.doReturn(null)
      .doReturn(originalTopup)
      .when(prepaidMovementEJBBean11).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
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
      prepaidEJBBean10.reverseTopupUserBalance(headers, uuid, reverseRequest,true);
      Assert.fail("Sould not be here");
    } catch (ReverseTimeExpiredException ex) {
      Assert.assertEquals("Deberia tener error de transaccion", REVERSA_TIEMPO_EXPIRADO.getValue(), ex.getCode());
      Mockito.verify(prepaidMovementEJBBean11, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
        Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));
      Mockito.verify(prepaidMovementEJBBean11, Mockito.never()).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    }
  }


  @Test
  public void reverseTopup() throws Exception {
    String uuid = UUID.randomUUID().toString();
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setUuid(uuid);
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

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setUuid(UUID.randomUUID().toString());

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setUserId(Long.MAX_VALUE);
    account.setUuid(UUID.randomUUID().toString());

    PrepaidMovement10 reverseMovement = new PrepaidMovement10();
    reverseMovement.setId(Long.MAX_VALUE);

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers, uuid);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    /*
      PrepaidMovement
      1. Busca movimiento de reversa
      2. Busca movimineto de carga original
     */
    Mockito.doReturn(null)
      .doReturn(originalTopup).when(prepaidMovementEJBBean11).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    Mockito.doReturn(reverseMovement).when(prepaidMovementEJBBean11).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));

    Mockito.doReturn(reverseMovement).when(prepaidMovementEJBBean11).getPrepaidMovementById(reverseMovement.getId());

    Mockito.doReturn("0987").when(parametersUtil).getString("api-prepaid", "cod_entidad", "v10");

    Mockito.doReturn("123456789")
      .when(delegate).sendPendingTopupReverse(Mockito.any(), Mockito.any(), Mockito.any(PrepaidUser10.class), Mockito.any());

    Mockito.doNothing().when(prepaidMovementEJBBean11).publishTransactionReversedEvent(Mockito.any(),
      Mockito.any(), Mockito.any(),
      Mockito.any(),
      Mockito.any(),
      Mockito.any());

    Mockito.doReturn(account).when(accountEJBBean10).findByUserId(Long.MAX_VALUE);

    prepaidEJBBean10.reverseTopupUserBalance(headers,uuid, reverseRequest,true);

    // Se verifica que se llamaron los metodos
    Mockito.verify(prepaidMovementEJBBean11, Mockito.times(2)).getPrepaidMovementForReverse(Mockito.anyLong(), Mockito.anyString(),
      Mockito.any(PrepaidMovementType.class), Mockito.any(TipoFactura.class));

    Mockito.verify(prepaidMovementEJBBean11, Mockito.times(1)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
    Mockito.verify(delegate, Mockito.times(1)).sendPendingTopupReverse(Mockito.any(), Mockito.any(), Mockito.any(PrepaidUser10.class), Mockito.any());
  }

}
