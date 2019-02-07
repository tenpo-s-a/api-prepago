package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.MovementOriginType;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.*;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class Test_PendingStoreWithdrawToAccounting extends TestBaseUnitAsync {

  protected static final String SCHEMA_ACCOUNTING = ConfigUtils.getInstance().getProperty("schema.acc");

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", getSchemaAccounting()));
  }

  @Test
  public void pendingWithdrawToAccount_ok() throws Exception {
    Date dateToday = new Date();

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setId(100L);
    prepaidMovement.setTipofac(TipoFactura.RETIRO_TRANSFERENCIA);
    prepaidMovement.setFecfac(dateToday);
    prepaidMovement.setImpfac(new BigDecimal(10000));
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement.setOriginType(MovementOriginType.API);
    prepaidMovement.setFechaCreacion(new Timestamp(dateToday.getTime()));

    UserAccount userAccount = new UserAccount();
    userAccount.setId(10L);

    String messageId = sendWithdrawToAccounting(prepaidMovement, userAccount);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MOVEMENT_TO_ACCOUNTING_RESP);
    ExchangeData<PrepaidTopupData10> remoteData = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteData);

    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, dateToday);
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 solo movimiento de account", 1, accounting10s.size());

    AccountingData10 accounting10 = accounting10s.get(0);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener el mismo id", prepaidMovement.getId(), accounting10.getIdTransaction());

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 solo movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.get(0);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", new Long(10), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado PENDING", AccountingStatusType.PENDING, clearing10.getStatus());
  }

  @Test
  public void pendingWithdrawToAccount_notOK_movementNull() throws Exception {
    Date dateToday = new Date();

    UserAccount userAccount = new UserAccount();
    userAccount.setId(10L);

    String messageId = sendWithdrawToAccounting(null, userAccount);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MOVEMENT_TO_ACCOUNTING_RESP);
    ExchangeData<PrepaidTopupData10> remoteData = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un withdraw", remoteData);

    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, dateToday);
    Assert.assertNull("Debe ser null", accounting10s);

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING);
    Assert.assertEquals("Debe ser de tamaño zero", 0, clearing10s.size());
  }

  @Test
  public void pendingWithdrawToAccount_notOK_accountNull() throws Exception {
    Date dateToday = new Date();

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setId(100L);
    prepaidMovement.setTipofac(TipoFactura.RETIRO_TRANSFERENCIA);
    prepaidMovement.setFecfac(dateToday);
    prepaidMovement.setImpfac(new BigDecimal(10000));
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement.setOriginType(MovementOriginType.API);
    prepaidMovement.setFechaCreacion(new Timestamp(dateToday.getTime()));


    String messageId = sendWithdrawToAccounting(prepaidMovement, null);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MOVEMENT_TO_ACCOUNTING_RESP);
    ExchangeData<PrepaidTopupData10> remoteData = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un withdraw", remoteData);

    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, dateToday);
    Assert.assertNull("Debe ser null", accounting10s);

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING);
    Assert.assertEquals("Debe ser de tamaño zero", 0, clearing10s.size());
  }

  @Test
  public void pendingWithdrawToAccount_notOK_accountIdNull() throws Exception {
    Date dateToday = new Date();

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setId(100L);
    prepaidMovement.setTipofac(TipoFactura.RETIRO_TRANSFERENCIA);
    prepaidMovement.setFecfac(dateToday);
    prepaidMovement.setImpfac(new BigDecimal(10000));
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement.setOriginType(MovementOriginType.API);
    prepaidMovement.setFechaCreacion(new Timestamp(dateToday.getTime()));

    UserAccount userAccount = new UserAccount();

    String messageId = sendWithdrawToAccounting(prepaidMovement, userAccount);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MOVEMENT_TO_ACCOUNTING_RESP);
    ExchangeData<PrepaidTopupData10> remoteData = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un withdraw", remoteData);

    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, dateToday);
    Assert.assertNull("Debe ser null", accounting10s);

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING);
    Assert.assertEquals("Debe ser de tamaño zero", 0, clearing10s.size());
  }
}
