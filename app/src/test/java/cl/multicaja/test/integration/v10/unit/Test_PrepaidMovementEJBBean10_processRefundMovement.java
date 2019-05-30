package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_TIENE_PREPAGO;
import static cl.multicaja.core.model.Errors.TRANSACCION_ERROR_GENERICO_$VALUE;
import static cl.multicaja.prepaid.model.v10.BusinessStatusType.REFUND_OK;
import static cl.multicaja.prepaid.model.v10.BusinessStatusType.TO_REFUND;
import static cl.multicaja.prepaid.model.v10.CdtTransactionType.*;
import static cl.multicaja.prepaid.model.v10.NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementStatus.REJECTED;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementType.TOPUP;

public class Test_PrepaidMovementEJBBean10_processRefundMovement  extends TestBaseUnit {

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
    Assert.assertEquals("Movimiento es Carga Confirmada",CARGA_WEB_CONF,cdtTransaction.getTransactionType());

    //Iniciar reversa en CDT
    cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Reversa de Carga",CdtTransactionType.REVERSA_CARGA,cdtTransaction.getTransactionType());

    getPrepaidEJBBean10().processRefundMovement(prepaidUser.getId(),prepaidMovement10.getId());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementTest.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED,
      dbMovement.getEstado());

    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK,
      dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtMovement = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(),CdtTransactionType.REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+CdtTransactionType.REVERSA_CARGA_CONF.getName(),
      CdtTransactionType.REVERSA_CARGA_CONF, cdtMovement.getTransactionType());

    Assert.assertEquals("Id de Transacción coincide en movimiento y cdt", prepaidMovementTest.getIdTxExterno(), cdtMovement.getExternalTransactionId());


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

    getPrepaidEJBBean10().processRefundMovement(prepaidUser.getId(),prepaidMovement10.getId());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementTest.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED,
      dbMovement.getEstado());

    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK,
      dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtMovement = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(),CdtTransactionType.REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+CdtTransactionType.REVERSA_CARGA_CONF.getName(),
      CdtTransactionType.REVERSA_CARGA_CONF, cdtMovement.getTransactionType());

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

    getPrepaidEJBBean10().processRefundMovement(prepaidUser.getId(),prepaidMovement10.getId());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED, dbMovement.getEstado());
    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK, dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtMovement = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(),CdtTransactionType.REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+REVERSA_CARGA_CONF.getName(), REVERSA_CARGA_CONF, cdtMovement.getTransactionType());

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

    getPrepaidEJBBean10().processRefundMovement(prepaidUser.getId(),prepaidMovement10.getId());

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED, dbMovement.getEstado());
    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(), REFUND_OK, dbMovement.getEstadoNegocio());

    CdtTransaction10 cdtMovement = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,
      prepaidMovement10.getIdTxExterno(), REVERSA_CARGA_CONF);

    Assert.assertEquals("Se cumple la condición: "+REVERSA_CARGA_CONF.getName(), REVERSA_CARGA_CONF, cdtMovement.getTransactionType());

    Assert.assertEquals("Id de Transacción coincide en movimiento y cdt", prepaidMovement10.getIdTxExterno(), cdtMovement.getExternalTransactionId());
  }

  @Test
  public void refund_status_on_movement_with_status_not_to_refund() throws  Exception {

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
    prepaidMovementEJBBean10.updatePrepaidBusinessStatus(null, prepaidMovement10.getId(), BusinessStatusType.CONFIRMED);

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

    try{
      getPrepaidEJBBean10().processRefundMovement(prepaidUser.getId(), prepaidMovement10.getId());
    } catch (NotFoundException ex) {
      Assert.assertEquals("Movimiento no esta en status TO_REFUND", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), ex.getCode());
    }
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
    //cdtTransaction.setIndSimulacion(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard10, cdtTransaction);

    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    try{
      getPrepaidEJBBean10().processRefundMovement(Long.MAX_VALUE,prepaidMovement10.getId());
    } catch (NotFoundException ex) {
      Assert.assertEquals("Cliente no tiene prepago", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
    }
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

    try{
      getPrepaidEJBBean10().processRefundMovement(prepaidUser.getId(), Long.MAX_VALUE);
    } catch (NotFoundException ex) {
      Assert.assertEquals("Cliente no tiene prepago", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), ex.getCode());
    }
  }

}
