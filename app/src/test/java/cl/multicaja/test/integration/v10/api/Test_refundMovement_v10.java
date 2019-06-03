
package cl.multicaja.test.integration.v10.api;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.prepaid.model.v10.BusinessStatusType.REFUND_OK;
import static cl.multicaja.prepaid.model.v10.BusinessStatusType.TO_REFUND;
import static cl.multicaja.prepaid.model.v10.CdtTransactionType.*;
import static cl.multicaja.prepaid.model.v10.NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementStatus.REJECTED;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementType.TOPUP;

public class Test_refundMovement_v10 extends TestBaseUnitApi {

  private HttpResponse confirmRefund(Long prepaidUserId, Long movementId) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/transactions/%s/refund", prepaidUserId, movementId), null);
    System.out.println("RESP HTTP: " + respHttp);
    return respHttp;
  }

  @Test
  public void refund_status_on_movement_carga_web() throws  Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));
    prepaidTopup.setFirstTopup(false);
    prepaidTopup.setMerchantCode(WEB_MERCHANT_CODE);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    //init Asserts
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null,prepaidMovement10.getId(),PrepaidMovementStatus.REJECTED);
    getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovement10.getId(), BusinessStatusType.TO_REFUND);

    PrepaidMovement10 prepaidMovementTest = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    Assert.assertEquals("Estado técnico Rejected", REJECTED, prepaidMovementTest.getEstado());
    Assert.assertEquals("Estado de negocio Refund", TO_REFUND, prepaidMovementTest.getEstadoNegocio());

    //Confirmar el retiro en CDT
    cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Carga Confirmada",CARGA_WEB_CONF,cdtTransaction.getTransactionType());

    //Iniciar reversa en CDT
    cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Reversa de Carga",CdtTransactionType.REVERSA_CARGA,cdtTransaction.getTransactionType());

    HttpResponse httpResponse = confirmRefund(prepaidUser.getId(), prepaidMovementTest.getId());
    Assert.assertEquals("Refund exitoso",202, httpResponse.getStatus());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementTest.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED, dbMovement.getEstado());
    
    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(), REFUND_OK, dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtTransaction10 = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(),CdtTransactionType.REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+CdtTransactionType.REVERSA_CARGA_CONF.getName(),
      CdtTransactionType.REVERSA_CARGA_CONF, cdtTransaction10.getTransactionType());

    Assert.assertEquals("Id de Transacción coincide en movimiento y cdt", prepaidMovementTest.getIdTxExterno(), cdtTransaction10.getExternalTransactionId());

  }

  @Test
  public void refund_status_on_movement_carga_pos() throws  Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));
    prepaidTopup.setFirstTopup(false);
    prepaidTopup.setMerchantCode(TransactionOriginType.POS.toString());

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard10, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    //init Asserts
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null,prepaidMovement10.getId(),PrepaidMovementStatus.REJECTED);
    getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovement10.getId(), BusinessStatusType.TO_REFUND);

    PrepaidMovement10 prepaidMovementTest = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    Assert.assertEquals("Estado técnico Rejected", REJECTED, prepaidMovementTest.getEstado());
    Assert.assertEquals("Estado de negocio Refund", TO_REFUND, prepaidMovementTest.getEstadoNegocio());

    //Confirmar el retiro en CDT
    cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Carga Confirmada",CARGA_POS_CONF,cdtTransaction.getTransactionType());

    //Iniciar reversa en CDT
    cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Reversa de Carga",CdtTransactionType.REVERSA_CARGA,cdtTransaction.getTransactionType());

    HttpResponse httpResponse = confirmRefund(prepaidUser.getId(), prepaidMovementTest.getId());
    Assert.assertEquals("Refund exitoso",202, httpResponse.getStatus());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementTest.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED, dbMovement.getEstado());

    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(), REFUND_OK, dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtMovement = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(),CdtTransactionType.REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+CdtTransactionType.REVERSA_CARGA_CONF.getName(), CdtTransactionType.REVERSA_CARGA_CONF, cdtMovement.getTransactionType());

    Assert.assertEquals("Id de Transacción coincide en movimiento y cdt", prepaidMovementTest.getIdTxExterno(), cdtMovement.getExternalTransactionId());

  }

  @Test
  public void refund_status_on_movement_primera_carga_web() throws  Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));
    prepaidTopup.setFirstTopup(true);
    prepaidTopup.setMerchantCode(TransactionOriginType.WEB.toString());


    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);
    //cdtTransaction.setIndSimulacion(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard10, cdtTransaction);

    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    // Enviar movimiento a REFUND
    PrepaidMovementEJBBean10 prepaidMovementEJBBean10 = getPrepaidMovementEJBBean10();
    prepaidMovementEJBBean10.updatePrepaidBusinessStatus(null, prepaidMovement10.getId(), BusinessStatusType.TO_REFUND);

    CdtEJBBean10 cdtEJBBean10 = getCdtEJBBean10();

    // Confirmar el topup en el CDT
    cdtTransaction = cdtEJBBean10.buscaMovimientoByIdExterno(null, prepaidMovement10.getIdTxExterno());

    CdtTransactionType reverseTransactionType = cdtTransaction.getCdtTransactionTypeReverse();
    cdtTransaction.setTransactionType(cdtTransaction.getCdtTransactionTypeConfirm());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction.setTransactionReference(cdtTransaction.getId());
    cdtTransaction = cdtEJBBean10.addCdtTransaction(null, cdtTransaction);

    // Iniciar reversa en CDT
    cdtTransaction.setTransactionType(reverseTransactionType);
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction = cdtEJBBean10.addCdtTransaction(null, cdtTransaction);

    HttpResponse httpResponse = confirmRefund(prepaidUser.getId(), prepaidMovement10.getId());
    Assert.assertEquals("Refund exitoso",202, httpResponse.getStatus());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED,
      dbMovement.getEstado());
    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK,
      dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtMovement = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(),CdtTransactionType.REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+REVERSA_CARGA_CONF.getName(),
      REVERSA_CARGA_CONF, cdtMovement.getTransactionType());

    Assert.assertEquals("Id de Transacción coincide en movimiento y cdt", prepaidMovement10.getIdTxExterno(), cdtMovement.getExternalTransactionId());

  }

  @Test
  public void refund_status_on_movement_primera_carga_pos() throws  Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));
    prepaidTopup.setFirstTopup(true);
    prepaidTopup.setMerchantCode(TransactionOriginType.POS.toString());

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard10, cdtTransaction);

    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    // Enviar movimiento a REFUND
    PrepaidMovementEJBBean10 prepaidMovementEJBBean10 = getPrepaidMovementEJBBean10();
    prepaidMovementEJBBean10.updatePrepaidBusinessStatus(null, prepaidMovement10.getId(), BusinessStatusType.TO_REFUND);

    CdtEJBBean10 cdtEJBBean10 = getCdtEJBBean10();

    // Confirmar el topup en el CDT
    cdtTransaction = cdtEJBBean10.buscaMovimientoByIdExterno(null, prepaidMovement10.getIdTxExterno());

    CdtTransactionType reverseTransactionType = cdtTransaction.getCdtTransactionTypeReverse();
    cdtTransaction.setTransactionType(cdtTransaction.getCdtTransactionTypeConfirm());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction.setTransactionReference(cdtTransaction.getId());
    cdtTransaction = cdtEJBBean10.addCdtTransaction(null, cdtTransaction);

    // Iniciar reversa en CDT
    cdtTransaction.setTransactionType(reverseTransactionType);
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction = cdtEJBBean10.addCdtTransaction(null, cdtTransaction);

    HttpResponse httpResponse = confirmRefund(prepaidUser.getId(), prepaidMovement10.getId());
    Assert.assertEquals("Refund exitoso",202, httpResponse.getStatus());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED,
      dbMovement.getEstado());
    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK,
      dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtMovement = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(), REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+REVERSA_CARGA_CONF.getName(),
      REVERSA_CARGA_CONF, cdtMovement.getTransactionType());

    Assert.assertEquals("Id de Transacción coincide en movimiento y cdt", prepaidMovement10.getIdTxExterno(), cdtMovement.getExternalTransactionId());

  }

  @Test
  public void refund_status_on_movement_with_user_not_found() throws  Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard10, cdtTransaction);

    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    HttpResponse httpResponse = confirmRefund(Long.MAX_VALUE,prepaidMovement10.getId());
    Assert.assertEquals("Refund Error",202, httpResponse.getStatus());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertNotEquals("No debe estar en devolucion ok", REFUND_OK, dbMovement.getEstadoNegocio());

  }

  @Test
  public void refund_status_on_movement_with_movement_not_found() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);
    //cdtTransaction.setIndSimulacion(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard10, cdtTransaction);

    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    HttpResponse httpResponse = confirmRefund(prepaidUser.getId(), Long.MAX_VALUE);
    Assert.assertEquals("Refund Error",202, httpResponse.getStatus());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertNotEquals("No debe estar en devolucion ok", REFUND_OK, dbMovement.getEstadoNegocio());
  }

}

