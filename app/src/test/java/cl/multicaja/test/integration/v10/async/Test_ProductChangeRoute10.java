package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.prepaid.async.v10.model.PrepaidProductChangeData10;
import cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.tecnocom.constants.TipoAlta;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @author abarazarte
 **/
public class Test_ProductChangeRoute10 extends TestBaseUnitAsync {

  @Test
  public void productChangeRetryCount4() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    String messageId = sendPendingProductChange(prepaidUser, prepaidCard, tipoAlta,4);

    //se verifica que el mensaje haya sido procesado
    Queue qResp = camelFactory.createJMSQueue(ProductChangeRoute10.ERROR_PRODUCT_CHANGE_RESP);
    ExchangeData<PrepaidProductChangeData10> data = (ExchangeData<PrepaidProductChangeData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un reverse", data);
    Assert.assertNotNull("Deberia existir un reverse", data.getData());

    ProcessorMetadata lastProcessorMetadata = data.getLastProcessorMetadata();
    String endpoint = ProductChangeRoute10.ERROR_PRODUCT_CHANGE_REQ;

    Assert.assertEquals("debe ser intento 5", 5, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  @Test
  public void productChange_AlreadyChanged() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    String messageId = sendPendingProductChange(prepaidUser, prepaidCard, tipoAlta,0);

    //se verifica que el mensaje haya sido procesado
    Queue qResp = camelFactory.createJMSQueue(ProductChangeRoute10.PENDING_PRODUCT_CHANGE_RESP);
    ExchangeData<PrepaidProductChangeData10> data = (ExchangeData<PrepaidProductChangeData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje", data);
    Assert.assertNotNull("Deberia existir un mensaje", data.getData());

    Assert.assertEquals("Debe fallar contener el mensaje de error", "MPA0928 - EL NUEVO PRODUCTO DEBE SER DIFERENTE AL ANTERIOR", data.getData().getMsjError());

  }


}
