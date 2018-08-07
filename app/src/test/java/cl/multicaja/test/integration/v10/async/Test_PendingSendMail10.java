package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.Email;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

public class Test_PendingSendMail10 extends TestBaseUnitAsync {


  @Test
  public void pendingSendMailOk() throws Exception {

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

    String messageId = sendPendingSendMail(user,prepaidUser ,prepaidCard10,0);
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MAIL_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remote = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Debe retornar una respuesta",remote);
    Assert.assertNotNull("Debe contener una tarjeta",remote.getData().getPrepaidCard10());
  }

  @Test
  public void pendingSendMailError() throws Exception {
    User user = registerUser();
    Email email = new Email();
    email.setValue("prueba-error@mail.com");
    user.setEmail(email);

    System.out.println("User Rut: " +user.getRut() + "-" + user.getRut().getDv());
    System.out.println("User Mail: "+user.getEmail());

    String messageId = sendPendingSendMailError(user,0);
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_SEND_MAIL_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remote = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Debe retornar una respuesta", remote);
  }
}
