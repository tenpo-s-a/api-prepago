package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.util.List;

public class Test_ReprocesQueue10 extends TestBaseUnitAsync {
  private static TecnocomServiceHelper tc;

  @BeforeClass
  public static void startTecnocom(){
     tc = TecnocomServiceHelper.getInstance();
  }

  @AfterClass
  public static void disableAutomaticErrorTC(){

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  @Test
  public void testPendingTopupInErrorQueue() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard = createPrepaidCardV2(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement, account, 2);
    Thread.sleep(2000);
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(30000, 60000).getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup en la cola de error topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup en la cola de error topup", remoteTopup.getData());

  }

  @Test
  public void testReinjectTopup() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement = createPrepaidMovement11(prepaidMovement);
    //Se setea para que de error de conexion!

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement, account,2);
    System.out.println(messageId);
    Thread.sleep(3000);
    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    // Se setea para que no de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.TOPUP);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(3000);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(15000, 60000).getMessage(qResp, messageId);
    System.out.println("Encontre un remoteTopup: " + remoteTopup);

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

  //FIXME: Corregir despues
  @Ignore
  @Test
  public void testReinjectAltaCliente() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement.setCardId(0L);

    prepaidMovement = createPrepaidMovement11(prepaidMovement);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingEmissionCard(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement,2);
    Thread.sleep(3000);
    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.PENDING_EMISSION);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(6000);
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status ACTIVE", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

  }

  @Test
  public void testReinjectCreateCard() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(prepaidUser.getId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);




    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);


    String messageId = sendPendingCreateCard(prepaidTopup, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, account, 2);

    Thread.sleep(3000);

    // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue(messageId);
    reprocesQueue.setLastQueue(QueuesNameType.CREATE_CARD);
    messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
    Thread.sleep(5000);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener getPan",remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Debe contener getNameOnCard",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNotNull("Debe contener getEncryptedPan",remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNotNull("Deberia contener numero unico de cliente",remoteTopup.getData().getPrepaidCard10().getNumeroUnico());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

  }

  @Test
  public void testReinjectTopupReverse() throws Exception{

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard10, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    System.out.println(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement11(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement11(prepaidReverseMovement);

    //Error TimeOut
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard10, prepaidUser, prepaidReverseMovement,2);
    System.out.println(messageId);
    Thread.sleep(3000);

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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement11(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.PROCESS_OK);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw.setCardId(prepaidCard10.getId());
    originalWithdraw = createPrepaidMovement11(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement11(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse = createPrepaidMovement11(reverse);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
    String messageId = sendPendingWithdrawReversal(withdraw10,prepaidUser, reverse, 2);

    Thread.sleep(3000);
    {
      // Vuelve a reinjectar en la cola y verifica que se ejecute correctamente.
      //Se setea para que de error de conexion!
      tc.getTecnocomService().setAutomaticError(false);
      tc.getTecnocomService().setRetorno(null);

      ReprocesQueue reprocesQueue = new ReprocesQueue();
      reprocesQueue.setIdQueue(messageId);
      reprocesQueue.setLastQueue(QueuesNameType.REVERSE_WITHDRAWAL);
      messageId = getPrepaidEJBBean10().reprocessQueue(null,reprocesQueue);
      Thread.sleep(3000);

      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger(15000, 60000).getMessage(qResp, messageId);

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


    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    System.out.println("prepaidCard: " + prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
      prepaidMovement.getId(),
      prepaidCard10.getPan(),
      account.getAccountNumber().substring(4, 8),
      account.getAccountNumber().substring(12),
      123,
      123,
      152,
      null,
      PrepaidMovementStatus.PROCESS_OK);

    System.out.println("prepaidMovement: " + prepaidMovement);
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard10, account, 2);
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

    PrepaidCard10 prepaidCard2 = remoteTopup.getData().getPrepaidCard10();
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard2);
    Assert.assertEquals("Deberia tener una tarjeta en status ACTIVE", PrepaidCardStatus.ACTIVE, prepaidCard2.getStatus());

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceFeeMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, dbMovements.get(0).getEstado());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard2.getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status ACTIVE", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

  }

}
