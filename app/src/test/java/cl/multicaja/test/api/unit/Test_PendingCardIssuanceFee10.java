package cl.multicaja.test.api.unit;

import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @autor abarazarte
 */
public class Test_PendingCardIssuanceFee10 extends TestBaseRouteUnit {

  @Test
  public void pendingCardIssuanceFee_PrepaidTopupNull() {

    PrepaidCard10 prepaidCard = new PrepaidCard10();

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    String messageId = RandomStringUtils.randomAlphanumeric(5);

    PrepaidTopupDataRoute10 prepaidTopupDataRoute10 = getPrepaidTopupDataRoute10(null, prepaidMovement, prepaidCard);

    sendMessage(messageId, prepaidTopupDataRoute10);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
  }

  @Test
  public void pendingCardIssuanceFee_FirstTopupFalse() {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    PrepaidCard10 prepaidCard = new PrepaidCard10();
    PrepaidTopup10 prepaidTopup10 = new PrepaidTopup10();
    prepaidTopup10.setFirstTopup(Boolean.FALSE);

    String messageId = RandomStringUtils.randomAlphanumeric(5);

    PrepaidTopupDataRoute10 prepaidTopupDataRoute10 = getPrepaidTopupDataRoute10(prepaidTopup10, prepaidMovement, prepaidCard);

    sendMessage(messageId, prepaidTopupDataRoute10);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidCardNull() {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    PrepaidTopup10 prepaidTopup10 = new PrepaidTopup10();
    prepaidTopup10.setFirstTopup(Boolean.TRUE);

    String messageId = RandomStringUtils.randomAlphanumeric(5);

    PrepaidTopupDataRoute10 prepaidTopupDataRoute10 = getPrepaidTopupDataRoute10(prepaidTopup10, prepaidMovement, null);

    sendMessage(messageId, prepaidTopupDataRoute10);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidMovementNull() {

    PrepaidCard10 prepaidCard = new PrepaidCard10();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    String messageId = RandomStringUtils.randomAlphanumeric(5);

    PrepaidTopupDataRoute10 prepaidTopupDataRoute10 = getPrepaidTopupDataRoute10(prepaidTopup, null, prepaidCard);

    sendMessage(messageId, prepaidTopupDataRoute10);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

  }

  /**
   * Es una primera carga
   */
  @Test
  public void pendingCardIssuanceFee() throws Exception {

    User user = registerUser();

    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_2(RandomStringUtils.randomAlphabetic(5,10));

    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);
    prepaidUser = createPrepaidUser(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());

    DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(prepaidCard.getProcessorUserId());
    prepaidCard.setPan(datosTarjetaDTO.getPan());
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));

    prepaidCard = createPrepaidCard(prepaidCard);
    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement(prepaidMovement);

    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
      prepaidMovement.getId(),
      123,
      123,
      152,
      PrepaidMovementStatus.PROCESS_OK);

    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = RandomStringUtils.randomAlphanumeric(5);
    prepaidTopup.setMessageId(messageId);

    PrepaidTopupDataRoute10 prepaidTopupDataRoute10 = getPrepaidTopupDataRoute10(prepaidTopup, prepaidMovement, prepaidCard);

    sendMessage(messageId, prepaidTopupDataRoute10);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    PrepaidMovement10 prepaidMovement10 = remoteTopup.getData().getPrepaidMovement10();
    Assert.assertNotNull("Deberia tener un Movimiento de carga", prepaidMovement10);
    Assert.assertNotEquals("El movimiento debe ser procesado", Long.valueOf(0), prepaidMovement10.getNumextcta());
    Assert.assertNotEquals("El movimiento debe ser procesado", Long.valueOf(0), prepaidMovement10.getNummovext());
    Assert.assertNotEquals("El movimiento debe ser procesado", Long.valueOf(0), prepaidMovement10.getClamone());
    PrepaidMovement10 issuanceFeeMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia tener un Movimiento de cobro de comision de emision", issuanceFeeMovement);
    Assert.assertNotEquals("El movimiento debe ser procesado", Long.valueOf(0), issuanceFeeMovement.getNumextcta());
    Assert.assertNotEquals("El movimiento debe ser procesado", Long.valueOf(0), issuanceFeeMovement.getNummovext());
    Assert.assertNotEquals("El movimiento debe ser procesado", Long.valueOf(0), issuanceFeeMovement.getClamone());

  }

  /**
   * Deberia estar en cola de error
   */
  @Test
  public void pendingCardIssuanceFee_ClientDoesNotExistsInTecnocom() throws Exception {

    User user = registerUser();

    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_2(RandomStringUtils.randomAlphabetic(5,10));

    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);
    prepaidUser = createPrepaidUser(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);
    prepaidCard = createPrepaidCard(prepaidCard);
    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement(prepaidMovement);
    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = RandomStringUtils.randomAlphanumeric(5);
    prepaidTopup.setMessageId(messageId);


    PrepaidTopupDataRoute10 prepaidTopupDataRoute10 = getPrepaidTopupDataRoute10(prepaidTopup, prepaidMovement, prepaidCard);

    sendMessage(messageId, prepaidTopupDataRoute10);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
    remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", remoteTopup);
  }

  private final Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ);

  private  void sendMessage(String messageId, PrepaidTopupDataRoute10 prepaidTopupDataRoute10){
    camelFactory.createJMSMessenger().putMessage(qReq, messageId,  new RequestRoute<>(prepaidTopupDataRoute10));
  }

  private PrepaidTopupDataRoute10 getPrepaidTopupDataRoute10(PrepaidTopup10 prepaidTopup, PrepaidMovement10 prepaidMovement, PrepaidCard10 prepaidCard) {
    PrepaidTopupDataRoute10 prepaidTopupDataRoute10 = new PrepaidTopupDataRoute10(prepaidTopup, null, null, prepaidMovement);
    prepaidTopupDataRoute10.setPrepaidCard10(prepaidCard);
    return prepaidTopupDataRoute10;
  }
}
