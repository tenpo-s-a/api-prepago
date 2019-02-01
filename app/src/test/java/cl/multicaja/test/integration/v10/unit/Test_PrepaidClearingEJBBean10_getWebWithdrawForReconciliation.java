package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cl.multicaja.test.integration.v10.unit.Test_PrepaidClearingEJBBean10_searchClearingDataToFile.buildClearing;

public class Test_PrepaidClearingEJBBean10_getWebWithdrawForReconciliation extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
  }

  @Test
  public void getWebWithdraws() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    List<Long> clearingIds = new ArrayList<>();

    // mov 1 - RETIRO_WEB + Pendiente Con Tecnocom + Clearing OK
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.OK);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }

    // mov 2 - RETIRO_WEB + Con Tecnocom OK + Clearing OK
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.OK);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);

      clearingIds.add(clearing1.getId());
    }

    // mov 3 - RETIRO_WEB + Con Tecnocom OK + Clearing PENDING
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.PENDING);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }

    // mov 4 - RETIRO_WEB + Con Tecnocom OK + Clearing REJECTED
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.REJECTED);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);

      clearingIds.add(clearing1.getId());
    }

    // mov 5 - RETIRO_WEB + Con Tecnocom OK + Clearing SENT
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.SENT);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }

    // mov 6 - Ya procesado
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);


      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.OK);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }


    List<ClearingData10> data = getPrepaidClearingEJBBean10().getWebWithdrawForReconciliation(null);

    Assert.assertNotNull("Debe tener una lista", data);
    Assert.assertTrue("No debe tener una lista vacia", data.size() > 0);

    Assert.assertEquals("Debe tener 2 movimientos", 2,  data.size());

    data.forEach(d -> {
      Assert.assertTrue("Debe ser de los id OK", clearingIds.contains(d.getId()));
    });
  }

  @Test
  public void getWebWithdraws_empty() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    List<Long> clearingIds = new ArrayList<>();

    // mov 1 - RETIRO_WEB + Pendiente Con Tecnocom + Clearing OK
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.OK);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }

    // mov 3 - RETIRO_WEB + Con Tecnocom OK + Clearing PENDING
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.PENDING);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }

    // mov 5 - RETIRO_WEB + Con Tecnocom OK + Clearing SENT
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.SENT);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }

    // mov 6 - Ya procesado
    {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      //Movimiento
      PrepaidMovement10  prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      // Conciliacion con Tecnocom pendiente
      prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);

      prepaidMovement = createPrepaidMovement10(prepaidMovement);


      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);

      AccountingData10 accounting = buildRandomAccouting();
      accounting.setIdTransaction(prepaidMovement.getId());
      accounting.setType(AccountingTxType.RETIRO_WEB);

      List<AccountingData10> accounting1s = new ArrayList<>();
      accounting1s.add(accounting);
      accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

      // Respuesta de banco OK
      ClearingData10 clearing1 = buildClearing();
      clearing1.setAccountingId(accounting1s.get(0).getId());
      clearing1.setStatus(AccountingStatusType.OK);

      clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    }


    List<ClearingData10> data = getPrepaidClearingEJBBean10().getWebWithdrawForReconciliation(null);

    Assert.assertNotNull("Debe tener una lista", data);
    Assert.assertEquals("No debe tener movimientos", 0,  data.size());

  }


}
