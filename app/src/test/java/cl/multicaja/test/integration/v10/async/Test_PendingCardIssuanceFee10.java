package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.time.Instant;
import java.util.List;

/**
 * @autor abarazarte
 */

public class Test_PendingCardIssuanceFee10 extends TestBaseUnitAsync {

  @Test
  public void pendingCardIssuanceFee_PrepaidTopupNull() throws Exception {


    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
   // prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    String messageId = sendPendingCardIssuanceFee(prepaidUser,null, prepaidMovement, prepaidCard10, account, 0);
    Thread.sleep(2000);
    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);

    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.ACTIVE));
  }

  @Test
  public void pendingCardIssuanceFee_PrepaidCardNull() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());


    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, null, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.ACTIVE));

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidCardStatusActive() throws Exception {

    PrepaidUser10 prepaidUser =  buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser.getId(), getRandomString(15));
    prepaidCard.setAccountId(account.getId());

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
    Assert.assertNull("No deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("No deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.ACTIVE));

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidMovementNull() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();

    Account account = getAccountEJBBean10().insertAccount(prepaidUser.getId(), getRandomString(15));

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, null, prepaidCard, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE));
  }

  @Test
  public void pendingCardIssuanceFee_AccountNull() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = createRandomAccount(prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, null, prepaidCard, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
  }

  @Test
  public void pendingCardIssuanceFee_AccountUuidNull() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();

    Account account = new Account();

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, null, prepaidCard, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE));
  }

  @Test
  public void pendingCardIssuanceFee_AccountUuidEmpty() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();

    Account account = new Account();
    account.setUuid("");

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, null, prepaidCard, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
  }

  @Test
  public void pendingCardIssuanceFee_UserNull() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = createRandomAccount(prepaidUser);


    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();

    String messageId = sendPendingCardIssuanceFee(null, prepaidTopup, null, prepaidCard, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
  }
  
  @Test
  public void pendingCardIssuanceFee_UserId0() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = createRandomAccount(prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();


    prepaidUser.setId(0L);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, null, prepaidCard, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
  }

  /**
   * Es una primera carga
   */
  @Test
  public void pendingCardIssuanceFee() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement11(prepaidMovement);

    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
      prepaidMovement.getId(),
      prepaidCard.getPan(),
      account.getAccountNumber().substring(4, 8),
      account.getAccountNumber().substring(12),
      123,
      123,
      152,
      null,
      PrepaidMovementStatus.PROCESS_OK);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard, account, 0);
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
    Assert.assertEquals("Debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, issuanceFeeMovement.getEstado());
    Assert.assertEquals("Debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, issuanceFeeMovement.getEstadoNegocio());
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
    Assert.assertEquals("Debe tener un movimiento de comision con status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, dbMovements.get(0).getEstado());
    Assert.assertEquals("Debe tener un movimiento de comision con estado de negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, dbMovements.get(0).getEstadoNegocio());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard.getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status ACTIVE", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

  }

  @Test
  public void pendingCardIssuanceFee_expireBalanceCache() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());
    
    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement11(prepaidMovement);

    getPrepaidMovementEJBBean11().updatePrepaidMovement(null,
      prepaidMovement.getId(),
      prepaidCard.getPan(),
      account.getAccountNumber().substring(4, 8),
      account.getAccountNumber().substring(12),
      123,
      123,
      152,
      null,
      PrepaidMovementStatus.PROCESS_OK);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 issuanceFeeMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia tener un Movimiento de cobro de comision de emision", issuanceFeeMovement);
    Assert.assertEquals("Debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, issuanceFeeMovement.getEstado());
    Assert.assertEquals("Debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, issuanceFeeMovement.getEstadoNegocio());
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
    Assert.assertEquals("Debe tener un movimiento de comision con status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, dbMovements.get(0).getEstado());
    Assert.assertEquals("Debe tener un movimiento de comision con estado de negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, dbMovements.get(0).getEstadoNegocio());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard.getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status ACTIVE", PrepaidCardStatus.ACTIVE, dbPrepaidCard.getStatus());

    account = getAccountEJBBean10().findByUserId(prepaidUser.getId());

    Assert.assertNotNull(account);
    Assert.assertTrue(account.getExpireBalance() > 0L && account.getExpireBalance() <= Instant.now().toEpochMilli());
  }

  /**
   * Deberia estar en cola de error
   */
  @Test
  public void pendingCardIssuanceFee_ClientDoesNotExistsInTecnocom() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = createRandomAccount(prepaidUser);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard11(prepaidUser,account.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement11(prepaidMovement);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard10, account, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    PrepaidMovement10 issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("Debetener status -> ERROR_IN_PROCESS_CARD_ISSUANCE_FEE", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("Debe tener estado negocio -> REJECTED", BusinessStatusType.REJECTED, issuanceMovement.getEstadoNegocio());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
    remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", remoteTopup);
    issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("Debetener status -> ERROR_IN_PROCESS_CARD_ISSUANCE_FEE", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("Debe tener estado negocio -> REJECTED", BusinessStatusType.REJECTED, issuanceMovement.getEstadoNegocio());

    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean11().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status -> ERROR_IN_PROCESS_CARD_ISSUANCE_FEE", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, dbMovements.get(0).getEstado());
    Assert.assertEquals("Debe tener un movimiento de comision con estado negocio ->  REJECTED", BusinessStatusType.REJECTED, dbMovements.get(0).getEstadoNegocio());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard10.getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_SEND_MAIL_CARD_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_REQ;

    Assert.assertEquals("debe ser primer intento", 0, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  @Test
  public void pendingCardIssuanceFee_RetryCount4() throws Exception {


    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = createRandomAccount(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard11(prepaidUser,account.getId());
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard, account, 3);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    PrepaidMovement10 issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
    remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", remoteTopup);

    issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("Debetener status -> ERROR_IN_PROCESS_CARD_ISSUANCE_FEE", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("Debe tener estado negocio -> IN_PROCESS", BusinessStatusType.IN_PROCESS, issuanceMovement.getEstadoNegocio());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status -> ERROR_IN_PROCESS_CARD_ISSUANCE_FEE", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, dbMovements.get(0).getEstado());
    Assert.assertEquals("Debe tener un movimiento de comision con estado negocio ->  IN_PROCESS", BusinessStatusType.IN_PROCESS, dbMovements.get(0).getEstadoNegocio());

    // Busca la Tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard.getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_SEND_MAIL_CARD_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_REQ;

    Assert.assertEquals("debe ser primer intento", 0, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }
}
