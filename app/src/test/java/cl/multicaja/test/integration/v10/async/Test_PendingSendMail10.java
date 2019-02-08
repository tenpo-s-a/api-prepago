package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.users.model.Email;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.util.Date;

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
    prepaidCard10.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    PrepaidTopup10 topup = buildPrepaidTopup10(user);
    topup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    String messageId = sendPendingSendMail(user,prepaidUser ,prepaidCard10, topup,0);
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

  @Ignore
  @Test
  public void sendMail_FailedWithdraw() throws Exception {
    User user = registerUser();
    user.setName("Juan");
    user.setLastname_1("Perez");

    Rut rut = new Rut();
    rut.setValue(1234567);
    rut.setDv("8");
    user.setRut(rut);

    Email email = new Email();
    email.setValue("hola@p.com");
    user.setEmail(email);

    UserAccount userAccount = new UserAccount();
    userAccount.setAccountNumber("0123456789");
    userAccount.setBankName("El banco de los pobres");

    PrepaidTopup10 prepaidTopup10 = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal(10000));
    prepaidTopup10.setAmount(amount);
    prepaidTopup10.setTotal(amount);

    PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();
    prepaidMovement10.setMonto(new BigDecimal(5000));
    prepaidMovement10.setFecfac(new Date());
    prepaidMovement10.setCodcom("CodigoComercio");
    prepaidMovement10.setId(38L);

    // Testeando que los mails lleguen correctamente
    getMailDelegate().sendWithdrawSuccessMail(user, prepaidMovement10, userAccount);
    //getMailDelegate().sendWithdrawFailedMail(user, prepaidMovement10);
    //getMailDelegate().sendTopupMail(prepaidTopup10, user, prepaidMovement10);

    Thread.sleep(2000);
  }
}
