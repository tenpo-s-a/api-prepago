package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class Test_PrepaidAccountingEJBBean10_expireIpmMovements extends TestBaseUnit {

  @Before
  @After
  public void afterEachTest() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", getSchemaAccounting()));
  }

  @Test
  public void expireIpmSuscriptions() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // Se crean 10 movimientos, con 0, 1, 2... 9 archivos procesados despues de ellos.
    ArrayList<PrepaidMovement10> allMovements = new ArrayList<>();
    ArrayList<AccountingData10> allAccountings = new ArrayList<>();
    ArrayList<ClearingData10> allClearings = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar archivo
      IpmFile ipmFile = new IpmFile();
      ipmFile.setFileId(getRandomString(10));
      ipmFile.setFileName("archivo");
      ipmFile.setStatus(IpmFileStatus.PROCESSED);
      getPrepaidAccountingEJBBean10().saveIpmFileRecord(null, ipmFile);

      // Insertar movimiento
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, PrepaidMovementType.SUSCRIPTION);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);
      allMovements.add(0, prepaidMovement);

      AccountingData10 accountingData = buildRandomAccouting();
      accountingData.setIdTransaction(prepaidMovement.getId());
      if(i % 2 == 0) {
        accountingData.setStatus(AccountingStatusType.PENDING);
      } else {
        accountingData.setStatus(AccountingStatusType.SENT_PENDING_CON);
      }
      accountingData.setAccountingStatus(AccountingStatusType.OK);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      Date parsedDate = dateFormat.parse("4000-06-20 00:00:00");
      accountingData.setConciliationDate(new Timestamp(parsedDate.getTime()));
      accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);
      allAccountings.add(0, accountingData);

      ClearingData10 clearingData = new ClearingData10();
      clearingData.setStatus(AccountingStatusType.PENDING);
      clearingData.setUserBankAccount(null);
      clearingData.setAccountingId(accountingData.getId());
      clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);
      allClearings.add(0, clearingData);

      Thread.sleep(10);
    }

    getPrepaidAccountingEJBBean10().expireIpmMovements();

    for(int i = 0; i < 10; i++) {
      PrepaidMovement10 prepaidMovement10 = allMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getId(), storedMovement.getId());

      AccountingData10 accountingData10 = allAccountings.get(i);
      AccountingData10 storedAccounting = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, prepaidMovement10.getId());

      ClearingData10 clearingData10 = allClearings.get(i);
      ClearingData10 storedClearing = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, accountingData10.getId());

      if(i <= 6) {
        Assert.assertEquals("Las primeras deben tener estado PENDING", PrepaidMovementStatus.PENDING, storedMovement.getEstado());
        Assert.assertEquals("No debe cambiar status accounting", accountingData10.getStatus(), storedAccounting.getStatus());
        Assert.assertEquals("No debe cambiar status accounting", accountingData10.getAccountingStatus(), storedAccounting.getAccountingStatus());
        Assert.assertEquals("No debe cambiar clearing status", clearingData10.getStatus(), storedClearing.getStatus());
        Assert.assertEquals("No debe cambiar la fecha de conciliacion", 4000, storedAccounting.getConciliationDate().toLocalDateTime().getYear());
      } else {
        Assert.assertEquals("Las ultimas deben tener estado EXPIRED", PrepaidMovementStatus.EXPIRED, storedMovement.getEstado());
        if (AccountingStatusType.PENDING.equals(accountingData10.getStatus())) {
          Assert.assertEquals("Si era pending debe cambiar status", AccountingStatusType.NOT_SEND, storedAccounting.getStatus());
        } else {
          Assert.assertEquals("Si no, no debe cambiar status", AccountingStatusType.SENT_PENDING_CON, storedAccounting.getStatus());
        }
        Assert.assertEquals("Debe cambiar status accounting", AccountingStatusType.NOT_OK, storedAccounting.getAccountingStatus());
        Assert.assertEquals("Debe cambiar clearing status", AccountingStatusType.NOT_CONFIRMED, storedClearing.getStatus());
        Assert.assertEquals("Debe actualizar la fecha de conciliacion", LocalDateTime.now().getDayOfYear(), storedAccounting.getConciliationDate().toLocalDateTime().getDayOfYear());
      }
    }
  }

  @Test
  public void expireIpmPurchases() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // Se crean 10 movimientos, con 0, 1, 2... 9 archivos procesados despues de ellos.
    ArrayList<PrepaidMovement10> allMovements = new ArrayList<>();
    ArrayList<AccountingData10> allAccountings = new ArrayList<>();
    ArrayList<ClearingData10> allClearings = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar archivo
      IpmFile ipmFile = new IpmFile();
      ipmFile.setFileId(getRandomString(10));
      ipmFile.setFileName("archivo");
      ipmFile.setStatus(IpmFileStatus.PROCESSED);
      getPrepaidAccountingEJBBean10().saveIpmFileRecord(null, ipmFile);

      // Insertar movimiento
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, PrepaidMovementType.PURCHASE);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);
      allMovements.add(0, prepaidMovement);

      AccountingData10 accountingData = buildRandomAccouting();
      accountingData.setIdTransaction(prepaidMovement.getId());
      if(i % 2 == 0) {
        accountingData.setStatus(AccountingStatusType.PENDING);
      } else {
        accountingData.setStatus(AccountingStatusType.SENT_PENDING_CON);
      }
      accountingData.setAccountingStatus(AccountingStatusType.OK);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      Date parsedDate = dateFormat.parse("4000-06-20 00:00:00");
      accountingData.setConciliationDate(new Timestamp(parsedDate.getTime()));
      accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);
      allAccountings.add(0, accountingData);

      ClearingData10 clearingData = new ClearingData10();
      clearingData.setStatus(AccountingStatusType.PENDING);
      clearingData.setUserBankAccount(null);
      clearingData.setAccountingId(accountingData.getId());
      clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);
      allClearings.add(0, clearingData);

      Thread.sleep(10);
    }

    getPrepaidAccountingEJBBean10().expireIpmMovements();

    for(int i = 0; i < 10; i++) {
      PrepaidMovement10 prepaidMovement10 = allMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getId(), storedMovement.getId());

      AccountingData10 accountingData10 = allAccountings.get(i);
      AccountingData10 storedAccounting = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, prepaidMovement10.getId());

      ClearingData10 clearingData10 = allClearings.get(i);
      ClearingData10 storedClearing = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, accountingData10.getId());

      if(i <= 6) {
        Assert.assertEquals("Las primeras deben tener estado PENDING", PrepaidMovementStatus.PENDING, storedMovement.getEstado());
        Assert.assertEquals("No debe cambiar status accounting", accountingData10.getStatus(), storedAccounting.getStatus());
        Assert.assertEquals("No debe cambiar status accounting", accountingData10.getAccountingStatus(), storedAccounting.getAccountingStatus());
        Assert.assertEquals("No debe cambiar clearing status", clearingData10.getStatus(), storedClearing.getStatus());
        Assert.assertEquals("No debe cambiar la fecha de conciliacion", 4000, storedAccounting.getConciliationDate().toLocalDateTime().getYear());
      } else {
        Assert.assertEquals("Las ultimas deben tener estado EXPIRED", PrepaidMovementStatus.EXPIRED, storedMovement.getEstado());
        if (AccountingStatusType.PENDING.equals(accountingData10.getStatus())) {
          Assert.assertEquals("Si era pending debe cambiar status", AccountingStatusType.NOT_SEND, storedAccounting.getStatus());
        } else {
          Assert.assertEquals("Si no, no debe cambiar status", AccountingStatusType.SENT_PENDING_CON, storedAccounting.getStatus());
        }
        Assert.assertEquals("Debe cambiar status accounting", AccountingStatusType.NOT_OK, storedAccounting.getAccountingStatus());
        Assert.assertEquals("Debe cambiar clearing status", AccountingStatusType.NOT_CONFIRMED, storedClearing.getStatus());
        Assert.assertEquals("Debe actualizar la fecha de conciliacion", LocalDateTime.now().getDayOfYear(), storedAccounting.getConciliationDate().toLocalDateTime().getDayOfYear());
      }
    }
  }

  @Test
  public void doNotExpireOtherMovements() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // Se crean 10 movimientos, con 0, 1, 2... 9 archivos procesados despues de ellos.
    ArrayList<PrepaidMovement10> allMovements = new ArrayList<>();
    ArrayList<AccountingData10> allAccountings = new ArrayList<>();
    ArrayList<ClearingData10> allClearings = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar archivo
      IpmFile ipmFile = new IpmFile();
      ipmFile.setFileId(getRandomString(10));
      ipmFile.setFileName("archivo");
      ipmFile.setStatus(IpmFileStatus.PROCESSED);
      getPrepaidAccountingEJBBean10().saveIpmFileRecord(null, ipmFile);

      // Insertar movimiento
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, (i % 2 == 0) ? PrepaidMovementType.TOPUP : PrepaidMovementType.WITHDRAW);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);
      allMovements.add(0, prepaidMovement);

      AccountingData10 accountingData = buildRandomAccouting();
      accountingData.setIdTransaction(prepaidMovement.getId());
      accountingData.setStatus(AccountingStatusType.PENDING);
      accountingData.setAccountingStatus(AccountingStatusType.OK);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      Date parsedDate = dateFormat.parse("4000-06-20 00:00:00");
      accountingData.setConciliationDate(new Timestamp(parsedDate.getTime()));
      accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);
      allAccountings.add(0, accountingData);

      ClearingData10 clearingData = new ClearingData10();
      clearingData.setStatus(AccountingStatusType.PENDING);
      clearingData.setUserBankAccount(null);
      clearingData.setAccountingId(accountingData.getId());
      clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);
      allClearings.add(0, clearingData);

      Thread.sleep(10);
    }

    getPrepaidAccountingEJBBean10().expireIpmMovements();

    // todas deben ser ignoradas
    for(int i = 0; i < 10; i++) {
      PrepaidMovement10 prepaidMovement10 = allMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getId(), storedMovement.getId());

      AccountingData10 accountingData10 = allAccountings.get(i);
      AccountingData10 storedAccounting = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, prepaidMovement10.getId());

      ClearingData10 clearingData10 = allClearings.get(i);
      ClearingData10 storedClearing = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, accountingData10.getId());

      Assert.assertEquals("Deben tener estado PENDING", PrepaidMovementStatus.PENDING, storedMovement.getEstado());
      Assert.assertEquals("No debe cambiar status accounting", accountingData10.getStatus(), storedAccounting.getStatus());
      Assert.assertEquals("No debe cambiar status accounting", accountingData10.getAccountingStatus(), storedAccounting.getAccountingStatus());
      Assert.assertEquals("No debe cambiar clearing status", clearingData10.getStatus(), storedClearing.getStatus());
      Assert.assertEquals("No debe cambiar la fecha de conciliacion", 4000, storedAccounting.getConciliationDate().toLocalDateTime().getYear());
    }
  }
}
