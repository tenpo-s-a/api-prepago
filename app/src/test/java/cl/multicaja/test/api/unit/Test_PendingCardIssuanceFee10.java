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

/**
 * @autor abarazarte
 */
public class Test_PendingCardIssuanceFee10 extends TestBaseRouteUnit {

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
    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = sendTopup(prepaidTopup, user, prepaidMovement);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Deberia tener un PrepaidMovement", remoteTopup.getData().getPrepaidMovement10());

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);
    remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de cobro de emision", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Deberia tener un Movimiento de carga", remoteTopup.getData().getPrepaidMovement10());
    Assert.assertNotNull("Deberia tener un Movimiento de cobro de comision de emision", remoteTopup.getData().getIssuanceFeeMovement10());

  }

  /**
   * No es una primera carga
   */
  @Test
  public void shouldNotHavePendingCardIssuanceFee_IsNotFirstTopup() throws Exception {

    User user = registerUser();

    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_2(RandomStringUtils.randomAlphabetic(5,10));

    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);
    prepaidUser = createPrepaidUser(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);
    prepaidCard.setProcessorUserId("");

    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());

    DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(prepaidCard.getProcessorUserId());
    prepaidCard.setPan(datosTarjetaDTO.getPan());
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));

    prepaidCard = createPrepaidCard(prepaidCard);
    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);
    prepaidTopup.setFirstTopup(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement(prepaidMovement);
    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = sendTopup(prepaidTopup, user, prepaidMovement);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Deberia tener un PrepaidMovement", remoteTopup.getData().getPrepaidMovement10());

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ);
    remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un mensaje en la cola de cobro de emision", remoteTopup);
  }

  /**
   * Deberia estar en cola de error
   */
  @Test
  public void shouldNotHavePendingCardIssuanceFee_ClientDoesNotExistsInTecnocom() throws Exception {

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
    prepaidTopup.setFirstTopup(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement(prepaidMovement);
    System.out.println("prepaidMovement: " + prepaidMovement);

    String messageId = sendTopup(prepaidTopup, user, prepaidMovement);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No Deberia existir un mensaje en la cosa de cobro de emision", remoteTopup);

    /*
    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
    remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de error de cobro de emision", remoteTopup);
    */
  }
}
