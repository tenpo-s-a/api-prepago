package cl.multicaja.test.unit;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.AccountEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.ejb.v11.PrepaidMovementEJBBean11;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.IvaType;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.tecnocom.model.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.TARJETA_ERROR_GENERICO_$VALUE;

/**
 * @author abarazarte
 **/
@RunWith(MockitoJUnitRunner.class)
public class Test_PrepaidEJBBean10_withdrawUserBalance {

  @Spy
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @Spy
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @Spy
  private PrepaidMovementEJBBean11 prepaidMovementEJBBean11;

  @Spy
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  @Spy
  private CdtEJBBean10 cdtEJBBean10;

  @Spy
  private AccountEJBBean10 accountEJBBean10;
  @Spy
  private TecnocomService tecnocomService;

  @Spy
  private EncryptUtil encryptUtil;

  @Spy
  private PrepaidTopupDelegate10 delegate;

  @Spy
  private ParametersUtil parametersUtil;

  @Spy
  private CalculatorParameter10 calculatorParameter10;

  @Spy
  private TecnocomServiceHelper tecnocomServiceHelper;

  @Spy
  @InjectMocks
  private PrepaidEJBBean10 prepaidEJBBean10;

  private static Map<String, Object> headers;


  @BeforeClass
  public static void setup() {
    headers = new HashMap<>();
    headers.put(Constants.HEADER_USER_TIMEZONE, "America/Santiago");
  }

  @Test
  public void reverseWithdrawByTecnocomTimeoutResponse() throws Exception {

    String uuid = RandomStringUtils.random(10);
    //Prepaid user
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setId(Long.MAX_VALUE);
    prepaidUser.setUserIdMc(Long.MAX_VALUE);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setUuid(uuid);

    Account account = new Account();
    account.setId(Long.MAX_VALUE);
    account.setAccountNumber("1234567890");
    account.setUuid("sfljaskflklasjfkljas");
    account.setUserId(Long.MAX_VALUE);


    //PrepaidCard
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setId(Long.MAX_VALUE);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10.setProcessorUserId("1");
    prepaidCard10.setEncryptedPan("1234567890");
    prepaidCard10.setPan("1234567890");


    PrepaidMovement10 originalWithdraw = new PrepaidMovement10();
    originalWithdraw.setId(Long.MAX_VALUE);
    originalWithdraw.setClamon(CodigoMoneda.CHILE_CLP);
    originalWithdraw.setMonto(BigDecimal.TEN);
    originalWithdraw.setFechaCreacion(Timestamp.from(ZonedDateTime.now().toInstant()));

    // Request
    NewPrepaidWithdraw10 withdrawRequest = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.TEN);
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    withdrawRequest.setAmount(amount);
    withdrawRequest.setRut(Integer.MAX_VALUE);
    withdrawRequest.setPassword("1234");
    withdrawRequest.setMerchantCode("1234567890");
    withdrawRequest.setMerchantName("Test");
    withdrawRequest.setMerchantCategory(1);
    withdrawRequest.setTransactionId("0987654321");

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(withdrawRequest);
    withdraw10.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    withdraw10.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    //Cdt transaction
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setNumError("0");

    // PrepaidUser
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).findByExtId(headers, uuid);

    // Account
    Mockito.doReturn(account).when(accountEJBBean10).findByUserId(Long.MAX_VALUE);

    Mockito.doReturn(prepaidCard10).when(prepaidCardEJBBean11).getByUserIdAndStatus(null, prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);

    //Cdt withdraw
    Mockito.doReturn(cdtTransaction).when(cdtEJBBean10).addCdtTransaction(Mockito.any(), Mockito.any());

    //Encrypt util
    Mockito.doReturn(prepaidCard10.getPan()).when(encryptUtil).decrypt(Mockito.anyString());

    PrepaidMovement10 withdrawMovement = new PrepaidMovement10();
    withdrawMovement.setId(Long.MAX_VALUE);
    withdrawMovement.setClamon(CodigoMoneda.CHILE_CLP);
    withdrawMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    withdrawMovement.setTipofac(TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA);
    withdrawMovement.setImpfac(BigDecimal.TEN);
    withdrawMovement.setCodcom("1234");
    withdrawMovement.setCodact(1);

    PrepaidMovement10 withdrawReverseMovement = new PrepaidMovement10();
    withdrawReverseMovement.setId(Long.MAX_VALUE);
    withdrawReverseMovement.setClamon(CodigoMoneda.CHILE_CLP);
    withdrawReverseMovement.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
    withdrawReverseMovement.setTipofac(TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);
    withdrawReverseMovement.setImpfac(BigDecimal.TEN);
    withdrawReverseMovement.setCodcom("1234");
    withdrawReverseMovement.setCodact(1);

    Mockito.doReturn(withdrawMovement)
      .doReturn(withdrawReverseMovement)
      .when(prepaidEJBBean10).buildPrepaidMovement(Mockito.any(), Mockito.any(PrepaidUser10.class), Mockito.any(), Mockito.any());

    Mockito.doReturn(withdrawMovement)
      .doReturn(withdrawReverseMovement)
      .when(prepaidMovementEJBBean10).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));

    Mockito.doReturn(withdrawMovement)
      .doReturn(withdrawReverseMovement)
      .when(prepaidMovementEJBBean10).getPrepaidMovementById(Long.MAX_VALUE);

    Mockito.doNothing()
      .when(prepaidMovementEJBBean11).addPrepaidMovementFeeList(Mockito.any());

    Mockito.doReturn(withdraw10)
      .when(prepaidEJBBean10).calculateFeeAndTotal(Mockito.any(IPrepaidTransaction10.class), Mockito.anyList());

    Response response = new Response();
    response.getRunServiceResponse().getReturn().setRetorno("1020");
    response.getRunServiceResponse().getReturn().setDescRetorno("");
    InclusionMovimientosDTO dtoWithdraw = new InclusionMovimientosDTO(response);
    Mockito.doReturn(dtoWithdraw).when(tecnocomServiceHelper).withdraw(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    Mockito.doReturn("123456789")
      .when(delegate).sendPendingWithdrawReversal(Mockito.any(), Mockito.any(), Mockito.any());

    Mockito.doNothing()
      .when(prepaidMovementEJBBean10).updatePrepaidMovementStatus(Mockito.any(), Mockito.any(), Mockito.any());

    Mockito.doReturn(null).when(prepaidMovementEJBBean10).getPrepaidMovementForReverse(Mockito.anyLong(),
      Mockito.anyString(), Mockito.any(PrepaidMovementType.class),
      Mockito.any(TipoFactura.class));

    CalculatorParameter10 calculatorParameter10 = Mockito.mock(CalculatorParameter10.class);
    Mockito.doReturn(new BigDecimal(100)).when(calculatorParameter10).getWITHDRAW_POS_FEE_AMOUNT();
    Mockito.doReturn(new BigDecimal(0)).when(calculatorParameter10).getWITHDRAW_POS_FEE_PERCENTAGE();
    Mockito.doReturn(IvaType.IVA_INCLUDED).when(calculatorParameter10).getWITHDRAW_POS_FEE_IVA_TYPE();
    Mockito.doReturn(1.19).when(calculatorParameter10).getIVA();
    Mockito.doReturn(calculatorParameter10).when(prepaidEJBBean10).getPercentage();

    CalculationsHelper calculationsHelperMock = Mockito.mock(CalculationsHelper.class);
    Mockito.doReturn(calculatorParameter10).when(calculationsHelperMock).getCalculatorParameter10();
    Mockito.doReturn(calculationsHelperMock).when(prepaidEJBBean10).getCalculationsHelper();

    try{
      prepaidEJBBean10.withdrawUserBalance(headers,prepaidUser.getUuid(), withdrawRequest,true);
    } catch (RunTimeValidationException vex) {
      // Se verifica que se llamaron los metodos
      Mockito.verify(prepaidMovementEJBBean10, Mockito.times(2)).addPrepaidMovement(Mockito.any(), Mockito.any(PrepaidMovement10.class));
      Mockito.verify(delegate, Mockito.times(1)).sendPendingWithdrawReversal(Mockito.any(), Mockito.any(), Mockito.any());
      Mockito.verify(prepaidMovementEJBBean10, Mockito.times(1)).updatePrepaidMovementStatus(headers, Long.MAX_VALUE, PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);

      Assert.assertEquals("Debe ser error de tarjeta generico", TARJETA_ERROR_GENERICO_$VALUE.getValue(), vex.getCode());
    }
  }

}
