package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_PendingCard10 extends TestBaseUnitAsync {

  /********************
   * Test flujo alta rapida
   * @throws Exception
   *****/
  @Test
  public void pendingEmissionCard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Deberia tener una PrepaidCard ProcessorUserId", remoteTopup.getData().getPrepaidCard10().getProcessorUserId());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_CREATE_CARD_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_CREATE_CARD_REQ;

    Assert.assertEquals("debe ser primer intento procesado", 1, lastProcessorMetadata.getRetry());
    Assert.assertTrue("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  /********************
   * Test flujo obtener tarjeta
   * @throws Exception
   *****/
  @Test
  public void pendingCreateCard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    /******************************
     * Validacion de datos Tarjeta
     ******************************/
    Assert.assertNotNull("Deberia tener una PrepaidCard ProcessorUserId", remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNotNull("Deberia tener PAN", remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Deberia tener PAN Encriptado", remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNotNull("Deberia tener Expire Date", remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertEquals("Status Igual a",PrepaidCardStatus.PENDING, remoteTopup.getData().getPrepaidCard10().getStatus());
    Assert.assertNotNull("Deberia Tener Nombre",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNotNull("Deberia codigo de producto",remoteTopup.getData().getPrepaidCard10().getProducto());
    Assert.assertNotNull("Deberia numero unico de cliente",remoteTopup.getData().getPrepaidCard10().getNumeroUnico());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_TOPUP_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_TOPUP_REQ;

    Assert.assertEquals("debe ser primer intento procesado", 1, lastProcessorMetadata.getRetry());
    Assert.assertTrue("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  /********************
   * Test directo de alta rapida
   * @throws Exception
   *****/
  @Test
  public void pendingEmissionCardUnit() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingEmissionCard(prepaidTopup, user, prepaidUser, cdtTransaction, prepaidMovement,0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener un contrato",remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNull("Pan Debe ser Nulo",remoteTopup.getData().getPrepaidCard10().getPan());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_CREATE_CARD_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_CREATE_CARD_REQ;

    Assert.assertEquals("debe ser primer intento procesado", 1, lastProcessorMetadata.getRetry());
    Assert.assertTrue("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  /********************
   * Test directo de obtener y crear tarjeta
   * @throws Exception
   *****/
  @Test
  public void pendingCreateCardUnit() throws Exception {

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

    String messageId = sendPendingCreateCard(prepaidTopup, user, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 0);

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
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_TOPUP_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_TOPUP_REQ;

    Assert.assertEquals("debe ser primer intento procesado", 1, lastProcessorMetadata.getRetry());
    Assert.assertTrue("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  /********************
   * Test directo de alta rapida (TIME OUT)
   * @throws Exception
   *****/
  @Test
  public void pendingEmissionCardUnitTimeOut() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingEmissionCard(prepaidTopup, user, prepaidUser, cdtTransaction, prepaidMovement,4);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNull("La tarjeta debe ser Nula", remoteTopup.getData().getPrepaidCard10());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING);
    Assert.assertNull("Deberia tener una tarjeta", dbPrepaidCard);

    //verifica que la ultima cola por la cual paso el mensaje sea ERROR_EMISSION_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.ERROR_EMISSION_REQ;

    Assert.assertEquals("debe ser primer intento", 0, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  /********************
   * Test directo de obtener y crear tarjeta
   * @throws Exception
   *****/
  @Test
  public void pendingCreateCardUnitTimeOut() throws Exception {

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

    String messageId = sendPendingCreateCard(prepaidTopup, user, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 4);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CREATE_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener un contrato",remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNull("Debe contener getPan",remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNull("Debe contener getNameOnCard",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNull("Debe contener getExpiration",remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertNull("Debe contener getEncryptedPan",remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNull("Deberia contener codigo de producto",remoteTopup.getData().getPrepaidCard10().getProducto());
    Assert.assertNull("Deberia contener numero unico de cliente",remoteTopup.getData().getPrepaidCard10().getNumeroUnico());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean10().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea ERROR_CREATE_CARD_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.ERROR_CREATE_CARD_REQ;

    Assert.assertEquals("debe ser primer intento", 0, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }


}
