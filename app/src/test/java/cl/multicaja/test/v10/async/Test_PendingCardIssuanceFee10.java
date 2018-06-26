package cl.multicaja.test.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.util.List;

/**
 * @autor abarazarte
 */
public class Test_PendingCardIssuanceFee10 extends TestBaseUnitAsync {

  @Test
  public void pendingCardIssuanceFee_PrepaidTopupNull() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    String messageId = sendPendingCardIssuanceFee(null, prepaidMovement, prepaidCard, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);

    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE));
  }

  @Test
  public void pendingCardIssuanceFee_PrepaidCardNull() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE));

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidCardStatusActive() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE));

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidMovementNull() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, null, prepaidCard, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING));
    Assert.assertNull("Deberia tener una tarjeta", getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE));
  }

  /**
   * Es una primera carga
   */
  @Test
  public void pendingCardIssuanceFee() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);
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
      123,
      123,
      152,
      PrepaidMovementStatus.PROCESS_OK);

    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, prepaidCard, 0);

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

  /**
   * Deberia estar en cola de error
   */
  @Test
  public void pendingCardIssuanceFee_ClientDoesNotExistsInTecnocom() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCard10(prepaidCard);
    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, prepaidCard, 0);

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
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status PROCESS_OK", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, dbMovements.get(0).getEstado());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, prepaidCard.getId());
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

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCard10(prepaidCard);
    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, prepaidCard, 3);

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
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    // Busca el movimiento en la BD
    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status PROCESS_OK", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, dbMovements.get(0).getEstado());

    // Busca la Tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, prepaidCard.getId());
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
