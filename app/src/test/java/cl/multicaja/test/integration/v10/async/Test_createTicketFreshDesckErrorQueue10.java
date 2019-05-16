package cl.multicaja.test.integration.v10.async;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;


//FIXME: estos test no tienen ningun assert
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

    // SE CREA USUARIO PREPAGO
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    //SE CREA CUENTA
    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    //SE CREA TARJETA
    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    //Se setea para que de error de conexion!

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement, account, 2);
    Thread.sleep(2000);

    System.out.println("TICKET CREADO");
  }

  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectAltaCliente() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingEmissionCard(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement,2);
    Thread.sleep(2000);
    System.out.println("TICKET CREADO");
  }

  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectCreateCard() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);


    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(prepaidUser.getName(), prepaidUser.getLastName(), "",prepaidUser.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser.getId(), getRandomString(15));

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingCreateCard(prepaidTopup,prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, account, 2);
    Thread.sleep(2000);
    System.out.println("TICKET CREADO");
  }

  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectTopupReverse() throws Exception{

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    System.out.println(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    //Error TimeOut
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,2);
    Thread.sleep(2000);

    System.out.println("TICKET CREADO");
  }

  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectWithdrawReversal() throws Exception{
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement11(prepaidUser, withdraw10);
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
    String messageId = sendPendingWithdrawReversal(withdraw10,prepaidUser, reverse, 2);
    System.out.println("TICKET CREADO");
  }

  //Verificado envia todos los dastos
  @Ignore
  @Test
  public void testReinjectIssuanFee() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    System.out.println("prepaidUser: " + prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    prepaidCard = createPrepaidCardV2(prepaidCard);
    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
      prepaidMovement.getId(),
      prepaidCard.getPan(),
      account.getAccountNumber().substring(4, 8),
      account.getAccountNumber().substring(12),
      123,
      123,
      152,
      null,
      PrepaidMovementStatus.PROCESS_OK);

    System.out.println("prepaidMovement: " + prepaidMovement);
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard, account, 2);
    Thread.sleep(2000);
    System.out.println("TICKET CREADO");
  }

}
