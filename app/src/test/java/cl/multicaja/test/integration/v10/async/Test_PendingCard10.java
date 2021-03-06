package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @autor vutreras
 */

// SE IGNORA YA QUE AHORA EL PROCESO ES SINCRONO!!!!!!!!
@Ignore
@SuppressWarnings("unchecked")
public class Test_PendingCard10 extends TestBaseUnitAsync {



  /********************
   * Test flujo alta rapida
   * @throws Exception
   *****/
  @Test
  public void pendingEmissionCard() throws Exception {


    //CREA USUARIO
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
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


    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    Account account = createRandomAccount(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement, account, 0);
    //Thread.sleep(2000);

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
    Assert.assertNotNull("Deberia tener PAN", remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Deberia tener PAN Encriptado", remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNotNull("Deberia tener Expire Date", remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertEquals("Status Igual a",PrepaidCardStatus.PENDING, remoteTopup.getData().getPrepaidCard10().getStatus());
    Assert.assertNotNull("Deberia Tener Nombre",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNotNull("Deberia codigo de producto",remoteTopup.getData().getPrepaidCard10().getProducto());
    Assert.assertNotNull("Deberia numero unico de cliente",remoteTopup.getData().getPrepaidCard10().getNumeroUnico());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());
    Assert.assertNotNull("Deberia tener hash del pan", dbPrepaidCard.getHashedPan());
    Assert.assertFalse("Deberia tener hash del pan", StringUtils.isAllBlank(dbPrepaidCard.getHashedPan()));

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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingEmissionCard(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement,0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertEquals("Pan Debe ser Nulo","",remoteTopup.getData().getPrepaidCard10().getPan());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);


    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    String messageId = sendPendingCreateCard(prepaidTopup, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, account, 0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener getPan",remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Debe contener getNameOnCard",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNotNull("Debe contener getEncryptedPan",remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNotNull("Deberia contener codigo de producto",remoteTopup.getData().getPrepaidCard10().getProducto());
    Assert.assertNotNull("Deberia contener numero unico de cliente",remoteTopup.getData().getPrepaidCard10().getNumeroUnico());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
    Assert.assertNotNull("Deberia tener una tarjeta", dbPrepaidCard);
    Assert.assertEquals("Deberia tener una tarjeta en status PENDING", PrepaidCardStatus.PENDING, dbPrepaidCard.getStatus());
    Assert.assertNotNull("Deberia tener hash del pan", dbPrepaidCard.getHashedPan());
    Assert.assertFalse("Deberia tener hash del pan", StringUtils.isAllBlank(dbPrepaidCard.getHashedPan()));

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

    //CREA USUARIO
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement, null, 4);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNull("La tarjeta debe ser Nula", remoteTopup.getData().getPrepaidCard10());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, remoteTopup.getData().getAccount().getId(), PrepaidCardStatus.PENDING);
    Assert.assertNull("No deberia tener una tarjeta", dbPrepaidCard);

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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10.setPan("");
    prepaidCard10.setNameOnCard("");
    prepaidCard10.setExpiration(0);
    prepaidCard10.setHashedPan("");
    prepaidCard10.setEncryptedPan("");
    prepaidCard10.setProducto("");
    prepaidCard10.setNumeroUnico("");
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    String messageId = sendPendingCreateCard(prepaidTopup, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, account, 4);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CREATE_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertEquals("No debe contener getPan","",remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertEquals("No debe contener getNameOnCard","",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertEquals("No debe contener getExpiration",0 , remoteTopup.getData().getPrepaidCard10().getExpiration().intValue());
    Assert.assertEquals("No debe contener getEncryptedPan","",remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertEquals("No deberia contener codigo de producto","",remoteTopup.getData().getPrepaidCard10().getProducto());
    Assert.assertEquals("No deberia contener numero unico de cliente","",remoteTopup.getData().getPrepaidCard10().getNumeroUnico());

    // Busca la tarjeta en la BD
    PrepaidCard10 dbPrepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, remoteTopup.getData().getPrepaidCard10().getId());
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
