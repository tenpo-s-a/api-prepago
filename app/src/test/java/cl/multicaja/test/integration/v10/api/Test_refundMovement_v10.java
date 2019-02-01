package cl.multicaja.test.integration.v10.api;

import cl.multicaja.cdt.ejb.v10.CdtEJB10;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.http.HttpError;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJB10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.TemplateUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.weld.context.http.Http;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.ejb.EJB;
import javax.validation.constraints.AssertTrue;
import java.math.BigDecimal;
import java.util.List;

import static cl.multicaja.core.model.Errors.TRANSACCION_ERROR_GENERICO_$VALUE;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.getParametersUtil;
import static cl.multicaja.prepaid.model.v10.BusinessStatusType.REFUND_OK;
import static cl.multicaja.prepaid.model.v10.BusinessStatusType.TO_REFUND;
import static cl.multicaja.prepaid.model.v10.CdtTransactionType.*;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementStatus.REJECTED;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementType.TOPUP;

public class Test_refundMovement_v10 extends TestBaseUnitApi {

  @EJB
  private CdtEJBBean10 cdtEJB;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJB;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean;


  private PrepaidMovementEJBBean10 getPrepaidMovementEJB() {
    return prepaidMovementEJB;
  }

  private CdtEJB10 getCdtEJB() {
    return cdtEJB;
  }

  private HttpResponse setRefundStatusOnMovement(Long prepaidUserId, Long movementId) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid_testhelpers/%s/transactions/%s/refund", prepaidUserId, movementId), null);
    System.out.println("RESP HTTP: " + respHttp);
    return respHttp;
  }

  private HttpResponse callPrepareToRefundOnMovement() {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid_testhelpers/transactions/prepare_to_refund"), null);
    System.out.println("RESP HTTP: " + respHttp);
    return respHttp;
  }

  public void user_not_exist(Integer userId) throws  Exception {
    PrepaidUser10 prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null,userId.longValue());
      Assert.assertNull("Usario prepago no se encuentra ", prepaidUser);
  }

  public void movement_not_exist(Integer movementId) throws Exception {
    PrepaidMovement10 prepaidMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(movementId.longValue());
      Assert.assertNull("Movimiento no se encuentra ", prepaidMovement);
  }

  public void movement_not_belongs_to_prepaid_user_id(Integer prepaidUserId, Integer movementId) throws Exception {
    PrepaidMovement10 prepaidMovement = getPrepaidMovementEJBBean10().
      getPrepaidMovementByIdPrepaidUserAndIdMovement(prepaidUserId.longValue(),movementId.longValue());
      Assert.assertNull("No se encuentra movimiento para el el usuario y el movimiento: ", prepaidMovement);
  }

  @Test
  public void refund_status_on_movement() throws  Exception {

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
    Assert.assertEquals("Movimiento es TopUp",PrepaidMovementType.TOPUP,prepaidMovement10.getTipoMovimiento());

    prepaidMovement10.setEstado(PrepaidMovementStatus.REJECTED);
    Assert.assertEquals("Estado técnico Rejected", REJECTED, prepaidMovement10.getEstado());
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.TO_REFUND);
    Assert.assertEquals("Estado de negocio Refund", TO_REFUND, prepaidMovement10.getEstadoNegocio());

    //Confirmar el retiro en CDT
    cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Primera Carga Confirmada",PRIMERA_CARGA_CONF,cdtTransaction.getTransactionType());

    //Iniciar reversa en CDT
    cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Reversa de Carga",REVERSA_CARGA,cdtTransaction.getTransactionType());

    //Mi Funcion
    //*****
    //Confirmar reversa en CDT
    cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA_CONF);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);
    Assert.assertEquals("Movimiento es Reversa de Carga Confirmada",REVERSA_CARGA_CONF,cdtTransaction.getTransactionType());

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
    PrepaidMovementEJBBean10 prepaidMovementEJBBean10 = new PrepaidMovementEJBBean10();
    prepaidMovementEJBBean10.updatePrepaidBusinessStatus(null, prepaidMovement10.getId(), BusinessStatusType.TO_REFUND);

    CdtEJBBean10 cdtEJBBean10 = new CdtEJBBean10();

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

    HttpResponse httpResponse = setRefundStatusOnMovement(prepaidUser.getId(), prepaidMovement10.getId());
    Assert.assertEquals("Refund exitoso",201, httpResponse.getStatus());

    Assert.assertEquals("Se encuentra con estado "+REJECTED.name(), REJECTED,
      getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId()).getEstado());
    Assert.assertEquals("Se encuentra con estado "+REFUND_OK.getValue(),REFUND_OK,
      getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId()).getEstadoNegocio());

    Assert.assertEquals(" Se encuentra con estado "+REVERSA_PRIMERA_CARGA.getName(),
      REVERSA_PRIMERA_CARGA,cdtTransaction.getTransactionType());

    Assert.assertEquals(" Se encuentra con estado "+REVERSA_PRIMERA_CARGA_CONF.getName(),
      REVERSA_PRIMERA_CARGA_CONF,cdtTransaction.getCdtTransactionTypeConfirm());

    Assert.assertEquals(" Se encuentra con estado "+REVERSA_CARGA.getName()+" NULL ",null,
      cdtTransaction.getCdtTransactionTypeReverse());

  }

  @Test
  public void refund_status_on_movement_with_user_not_found() throws  Exception {
    user_not_exist(123456789);
  }

  @Test
  public void refund_status_on_movement_with_movement_not_found() throws Exception {
    movement_not_exist(123456789);
  }

  @Test
  public void refund_status_on_movement_with_movement_not_belongs_to_prepare_user_id() throws Exception {
    Integer prepareUserId = 123;
    Integer movemementId = 456;
    movement_not_belongs_to_prepaid_user_id(prepareUserId,movemementId);
  }

  @Ignore
  @Test
  public void call_prepare_to_refund_on_movement() throws  Exception {
    HttpResponse httpResponse = callPrepareToRefundOnMovement();
    System.out.println("callPrepareToRefundOnMovement: "+httpResponse.getResp());
  }


}
