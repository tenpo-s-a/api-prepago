package cl.multicaja.test.integration.v10.async;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;


public class Test_createTicketFreshDesckErrorQueue10 extends TestBaseUnitAsync {
  private static TecnocomServiceHelper tc;

  @BeforeClass
  public static void startTecnocom(){
    tc = TecnocomServiceHelper.getInstance();
  }

  @Ignore
  @Test
  public void testAlgo() throws Exception {
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    ReprocesQueue reprocesQueue = new ReprocesQueue();
    reprocesQueue.setIdQueue("VSPTirsGmuqFmvTnenaS");
    reprocesQueue.setLastQueue(QueuesNameType.TOPUP);
    String messageId = getPrepaidEJBBean10().reprocessQueue(null, reprocesQueue);

  }

  @Ignore
  @Test
  public void testReinjectTopup() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    //Se setea para que de error de conexion!

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 2);
    Thread.sleep(2000);

    System.out.println("TICKET CREADO");
  }
  //Verificado envia todos los dastos

  @Ignore
  @Test
  public void testReinjectAltaCliente() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingEmissionCard(prepaidTopup, user, prepaidUser, cdtTransaction, prepaidMovement,2);
    Thread.sleep(2000);
    System.out.println("TICKET CREADO");
  }
  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectCreateCard() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

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


    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingCreateCard(prepaidTopup, user, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 2);
    Thread.sleep(2000);
    System.out.println("TICKET CREADO");
  }

  //Verificado envia todos los dastos
  @Test
  public void testReinjectSendMailCard() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

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

    PrepaidTopup10 topup = buildPrepaidTopup10(user);
    topup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    System.out.println("TICKET CREADO");
  }

  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectTopupReverse() throws Exception{

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    System.out.println(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    //Error TimeOut
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,2);
    Thread.sleep(2000);

    System.out.println("TICKET CREADO");
  }
  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectWithdrawReversal() throws Exception{
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.PROCESS_OK);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse = createPrepaidMovement10(reverse);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
    String messageId = sendPendingWithdrawReversal(withdraw10, user,prepaidUser, reverse, 2);
    System.out.println("TICKET CREADO");
  }
  //Verificado envia todos los dastos
  //@Ignore
  @Test
  public void testReinjectIssuanFee() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
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
      prepaidCard.getPan(),
      prepaidCard.getProcessorUserId().substring(4, 8),
      prepaidCard.getProcessorUserId().substring(12),
      123,
      123,
      152,
      null,
      PrepaidMovementStatus.PROCESS_OK);

    System.out.println("prepaidMovement: " + prepaidMovement);
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
    String messageId = sendPendingCardIssuanceFee(user, prepaidTopup, prepaidMovement, prepaidCard, 2);
    Thread.sleep(2000);
    System.out.println("TICKET CREADO");
  }

}
