package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.ListIterator;

import static cl.multicaja.prepaid.model.v10.BusinessStatusType.REFUND_OK;
import static cl.multicaja.prepaid.model.v10.BusinessStatusType.TO_REFUND;
import static cl.multicaja.prepaid.model.v10.CdtTransactionType.*;
import static cl.multicaja.prepaid.model.v10.CdtTransactionType.REVERSA_PRIMERA_CARGA_CONF;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementStatus.REJECTED;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementType.TOPUP;

public class Test_PrepaidMovementEJBBean10_processRefundMovement  extends TestBaseUnit {

  @Test
  public void refund_status_on_movement_carga() throws  Exception {

    User user = registerUser();

    System.out.println("userRut: "+user.getRut());

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    System.out.println("prepaidUserId: "+prepaidUser.getId());

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
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
    Assert.assertEquals("Movimiento es Primera Carga Confirmada",PRIMERA_CARGA_CONF,cdtTransaction.getTransactionType());

    //Iniciar reversa en CDT
    cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Reversa de Carga",REVERSA_CARGA,cdtTransaction.getTransactionType());

    CdtTransaction10 cdtTransactionTest = getPrepaidMovementEJBBean10().processRefundMovement(prepaidUser.getId(),prepaidMovementTest.getId());
    Assert.assertNotNull("Transaccion exitosa",cdtTransactionTest);

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED,
      getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementTest.getId()).getEstado());

    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK,
      getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementTest.getId()).getEstadoNegocio());

    List<CdtTransaction10> transaction10s = getCdtEJBBean10().buscaListaMovimientoByIdExterno(null,prepaidMovementTest.getIdTxExterno());

    if(transaction10s.size() > 0){

      Assert.assertEquals("4 Transacciones: ",4,transaction10s.size());

      for (ListIterator<CdtTransaction10> iter = transaction10s.listIterator(); iter.hasNext();) {
        cdtTransaction = iter.next();

        if(cdtTransaction.getCdtTransactionTypeConfirm() != null){

          if(cdtTransaction.getCdtTransactionTypeConfirm() == PRIMERA_CARGA_CONF){
            Assert.assertEquals(" Se encuentra con estado "+PRIMERA_CARGA_CONF.getName(),
              PRIMERA_CARGA_CONF,cdtTransaction.getCdtTransactionTypeConfirm());
          }

          if(cdtTransaction.getCdtTransactionTypeConfirm() == REVERSA_PRIMERA_CARGA_CONF){
            Assert.assertEquals(" Se encuentra con estado "+REVERSA_PRIMERA_CARGA_CONF.getName(),
              REVERSA_PRIMERA_CARGA_CONF,cdtTransaction.getCdtTransactionTypeConfirm());
          }

          if(cdtTransaction.getCdtTransactionTypeConfirm() == REVERSA_CARGA_CONF){
            Assert.assertEquals(" Se encuentra con estado "+REVERSA_CARGA_CONF.getName(),
              REVERSA_CARGA_CONF,cdtTransaction.getCdtTransactionTypeConfirm());
          }
        }

      }
    }

  }

  @Test
  public void refund_status_on_movement_primera_carga() throws  Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);
    //cdtTransaction.setIndSimulacion(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

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

    CdtTransaction10 cdtTransactionTest = getPrepaidMovementEJBBean10().processRefundMovement(prepaidUser.getId(),prepaidMovement10.getId());
    Assert.assertNotNull("Transaccion exitosa",cdtTransactionTest);

    //HttpResponse httpResponse = setRefundStatusOnMovement(prepaidUser.getId(), prepaidMovement10.getId());
    //Assert.assertEquals("Refund exitoso",201, httpResponse.getStatus());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED,
      getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId()).getEstado());
    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK,
      getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId()).getEstadoNegocio());

    List<CdtTransaction10> transaction10s = getCdtEJBBean10().buscaListaMovimientoByIdExterno(null,prepaidMovement10.getIdTxExterno());

    if(transaction10s.size() > 0){

      Assert.assertEquals("3 Transacciones: ",3,transaction10s.size());

      for (ListIterator<CdtTransaction10> iter = transaction10s.listIterator(); iter.hasNext();) {
        cdtTransaction = iter.next();

        if(cdtTransaction.getCdtTransactionTypeConfirm() != null){

          if(cdtTransaction.getCdtTransactionTypeConfirm() == PRIMERA_CARGA_CONF){
            Assert.assertEquals(" Se encuentra con estado "+PRIMERA_CARGA_CONF.getName(),
              PRIMERA_CARGA_CONF,cdtTransaction.getCdtTransactionTypeConfirm());
          }

          if(cdtTransaction.getCdtTransactionTypeConfirm() == REVERSA_PRIMERA_CARGA_CONF){
            Assert.assertEquals(" Se encuentra con estado "+REVERSA_PRIMERA_CARGA_CONF.getName(),
              REVERSA_PRIMERA_CARGA_CONF,cdtTransaction.getCdtTransactionTypeConfirm());
          }

        }
      }
    }
  }

  @Test
  public void refund_status_on_movement_with_user_not_found() throws  Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);
    //cdtTransaction.setIndSimulacion(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    Integer _idUser = 1234567890;
    CdtTransaction10 cdtTransactionTest = getPrepaidMovementEJBBean10().processRefundMovement(_idUser.longValue(),prepaidMovement10.getId());
    Assert.assertNull("Transaccion fallida, el idUser:"+_idUser+" no contiene movimientos o transacciones asociadas",cdtTransactionTest);

  }

  @Test
  public void refund_status_on_movement_with_movement_not_found() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);
    //cdtTransaction.setIndSimulacion(Boolean.FALSE);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(REJECTED);
    prepaidMovement10.setTipoMovimiento(TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    Integer _idMovimiento = 123456789;
    CdtTransaction10 cdtTransactionTest = getPrepaidMovementEJBBean10().processRefundMovement(prepaidUser.getId(),_idMovimiento.longValue());
    Assert.assertNull("Transaccion fallida, el idMovimiento:"+_idMovimiento+" no contiene movimientos o transacciones asociadass",cdtTransactionTest);
  }

}
