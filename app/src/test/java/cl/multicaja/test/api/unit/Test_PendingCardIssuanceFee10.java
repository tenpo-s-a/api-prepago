package cl.multicaja.test.api.unit;

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
import java.util.List;

/**
 * @autor abarazarte
 */
public class Test_PendingCardIssuanceFee10 extends TestBaseRouteUnit {

  @Test
  public void pendingCardIssuanceFee_PrepaidTopupNull() throws Exception {

    User user = registerUser();

    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_2(RandomStringUtils.randomAlphabetic(5,10));

    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);
    prepaidUser = createPrepaidUser(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    String messageId = sendPendingCardIssuanceFee(null, prepaidMovement, prepaidCard, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
  }

  @Test
  public void pendingCardIssuanceFee_FirstTopupFalse() throws Exception {

    User user = registerUser();

    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_2(RandomStringUtils.randomAlphabetic(5,10));

    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);
    prepaidUser = createPrepaidUser(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    PrepaidCard10 prepaidCard = new PrepaidCard10();
    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.FALSE);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, prepaidCard, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidCardNull() throws Exception {

    User user = registerUser();

    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_2(RandomStringUtils.randomAlphabetic(5,10));

    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);
    prepaidUser = createPrepaidUser(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);

  }

  @Test
  public void pendingCardIssuanceFee_PrepaidMovementNull() throws Exception {

    User user = registerUser();

    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_2(RandomStringUtils.randomAlphabetic(5,10));

    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);
    prepaidUser = createPrepaidUser(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = new PrepaidCard10();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setFirstTopup(Boolean.TRUE);

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, null, prepaidCard, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertNull("No debe tener un movimiento de comision", dbMovements);
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

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, prepaidCard, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);

    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
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

    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceFeeMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, dbMovements.get(0).getEstado());

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

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, prepaidCard, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    PrepaidMovement10 issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
    remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", remoteTopup);
    issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status PROCESS_OK", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, dbMovements.get(0).getEstado());
  }

  @Test
  public void pendingCardIssuanceFee_RetryCount4() throws Exception {

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

    String messageId = sendPendingCardIssuanceFee(prepaidTopup, prepaidMovement, prepaidCard, 3);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);

    PrepaidMovement10 issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
    remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", remoteTopup);

    issuanceMovement = remoteTopup.getData().getIssuanceFeeMovement10();
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", issuanceMovement);
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, issuanceMovement.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    List<PrepaidMovement10> dbMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUserAndTipoMovimiento( prepaidUser.getId(), PrepaidMovementType.ISSUANCE_FEE);

    Assert.assertTrue("Debe tener un movimiento de comision", dbMovements.size() > 0);
    Assert.assertEquals("Debe tener un movimiento de comision", issuanceMovement.getId(), dbMovements.get(0).getId());
    Assert.assertEquals("Debe tener un movimiento de comision con status PROCESS_OK", PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE, dbMovements.get(0).getEstado());
  }
}
