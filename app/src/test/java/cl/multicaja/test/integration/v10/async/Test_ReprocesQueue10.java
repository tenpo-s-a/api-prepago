package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.util.List;

public class Test_ReprocesQueue10 extends TestBaseUnitAsync {
  private static TecnocomServiceHelper tc;

  @BeforeClass
  public static void startTecnocom(){
     tc = TecnocomServiceHelper.getInstance();
  }


  @Test
  public void testPendingTopupInErrorQueue() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    //Se setea para que de error de conexion!

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 2);
    Thread.sleep(2000);
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup en la cola de error topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup en la cola de error topup", remoteTopup.getData());

  }

  @Test
  public void testReinjectTopup() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    //Se setea para que de error de conexion!

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 2);
    Thread.sleep(2000);
    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.TOPUP);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(3000);
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();
    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementResp.getEstado());
  }

  @Test
  public void testReinjectAltaCliente() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingEmissionCard(prepaidTopup, user, prepaidUser, cdtTransaction, prepaidMovement,2);
    Thread.sleep(2000);
    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.PENDING_EMISSION);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(2000);
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener un contrato",remoteTopup.getData().getPrepaidCard10().getProcessorUserId());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

  }

  @Test
  public void testReinjectCreateCard() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCard10(prepaidCard10);


    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingCreateCard(prepaidTopup, user, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 2);
    Thread.sleep(2000);

    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.CREATE_CARD);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(2000);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener un contrato",remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNotNull("Debe contener getPan",remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Debe contener getNameOnCard",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNotNull("Debe contener getExpiration",remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertNotNull("Debe contener getEncryptedPan",remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNotNull("Deberia contener codigo de producto",remoteTopup.getData().getPrepaidCard10().getProducto());
    Assert.assertNotNull("Deberia contener numero unico de cliente",remoteTopup.getData().getPrepaidCard10().getNumeroUnico());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

  }

  @Test
  public void testReinjectSendMailCard() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    System.out.println("User Rut: "+prepaidUser.getRut());
    System.out.println("User Mail: "+user.getEmail());

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);

    DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(prepaidCard10.getProcessorUserId());
    prepaidCard10.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
    prepaidCard10.setEncryptedPan(encryptUtil.encrypt(datosTarjetaDTO.getPan()));
    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    PrepaidTopup10 topup = buildPrepaidTopup10(user);
    topup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingSendMail(user,prepaidUser ,prepaidCard10, topup,2);
    Thread.sleep(3000);

    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.SEND_MAIL);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(2000);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MAIL_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remote = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Debe retornar una respuesta",remote);
    Assert.assertNotNull("Debe contener una tarjeta",remote.getData().getPrepaidCard10());
  }

  @Test
  public void testReinjectTopupReverse() throws Exception{

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    System.out.println(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    //Error TimeOut
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,2);
    Thread.sleep(2000);

    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.REVERSE_TOPUP);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(2000);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());

  }

  @Test
  public void testReinjectWithdrawReversal() throws Exception{
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.PROCESS_OK);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse = createPrepaidMovement10(reverse);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
    String messageId = sendPendingWithdrawReversal(withdraw10, user,prepaidUser, reverse, 2);
    Thread.sleep(2000);
    {
      // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
      //Se setea para que de error de conexion!
      tc.getTecnocomService().setAutomaticError(false);
      tc.getTecnocomService().setRetorno(null);

      ReprocesQueue reprocesQueue = new ReprocesQueue();
      reprocesQueue.setIdQueue(messageId);
      reprocesQueue.setLastQueue(QueuesNameType.REVERSE_WITHDRAWAL);
      messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
      Thread.sleep(2000);

      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 issuanceMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", issuanceMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, issuanceMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia tener status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, originalDb.getEstado());
      Assert.assertEquals("Deberia tener businessStatus REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
    }
  }

  @Test
  public void testReinjectIssuanFee() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());

    DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(prepaidCard.getProcessorUserId());
    prepaidCard.setPan(datosTarjetaDTO.getPan());
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard = createPrepaidCard10(prepaidCard);
    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
      prepaidMovement.getId(),
      prepaidCard.getPan(),
      prepaidCard.getProcessorUserId().substring(4, 8),
      prepaidCard.getProcessorUserId().substring(12),
      123,
      123,
      152,
      null,
      PrepaidMovementStatus.PROCESS_OK);

    System.out.println("prepaidMovement: " + prepaidMovement);
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
    String messageId = sendPendingCardIssuanceFee(user, prepaidTopup, prepaidMovement, prepaidCard, 2);
    Thread.sleep(2000);
    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.ISSUANCE_FEE);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(2000);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 issuanceFeeMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia tener un Movimiento de cobro de comision de emision", issuanceFeeMovement);
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, issuanceFeeMovement.getEstado());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceFeeMovement.getNumextcta());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceFeeMovement.getNummovext());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceFeeMovement.getClamone());

    PrepaidCard10 prepaidCard10 = remoteTopup.getData().getPrepaidCard10();
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Deberia tener una tarjeta en status ACTIVE", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceFeeMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, dbMovements.get(0).getEstado());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, prepaidCard.getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status ACTIVE", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_SEND_MAIL_CARD_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_SEND_MAIL_CARD_REQ;

    Assert.assertEquals("debe ser primer intento procesado", 1, lastProcessorMetadata.getRetry());
    Assert.assertTrue("debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

}
