package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_PrepaidAccountingEJBBean10_processMovementForAccounting extends TestBaseUnit {

  private static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");


  @Before
  @After
  public void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", SCHEMA));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void dateNull()throws Exception {
    getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), null);
    Assert.fail("Should not be here");
  }

  @Test
  public void processMovementForAccounting_PosTopup()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(getRandomString(15));
      List<Long> originalMovementsIds = new ArrayList<>();

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

      List<AccountingData10> accountinMovements = getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), utc.toLocalDateTime());

      Assert.assertEquals("Debe ser 3 ", 3,accountinMovements.size());

      for (AccountingData10 m : accountinMovements) {
        Assert.assertNotNull("Debe tener id", m.getId());
        Assert.assertEquals("Debe ser Carga_POS", AccountingTxType.CARGA_POS, m.getType());
        Assert.assertEquals("Debe ser Amount Mastercard -> 0", BigDecimal.ZERO, m.getAmountMastercard().getValue());
        Assert.assertEquals("Debe ser Amount usd -> 0", BigDecimal.ZERO, m.getAmountUsd().getValue());
        Assert.assertEquals("Debe ser Fee -> 0", BigDecimal.ZERO, m.getFee());
        Assert.assertEquals("Debe ser Fee Iva -> 0", BigDecimal.ZERO, m.getFee());
        Assert.assertEquals("Debe ser Fee Iva -> 0", BigDecimal.ZERO, m.getFeeIva());
        Assert.assertEquals("Debe ser ExchangeRateDiff -> 0", BigDecimal.ZERO, m.getExchangeRateDif());
        BigDecimal amountBalance = BigDecimal.ZERO
          .add(m.getAmount().getValue())
          .subtract(m.getCollectorFee())
          .subtract(m.getCollectorFeeIva());

        Assert.assertEquals("AmountBalance = amount - fee - feeIva", amountBalance, m.getAmountBalance().getValue());

        Long originalMovement = originalMovementsIds.stream()
          .filter(mov -> mov.equals(m.getIdTransaction()))
          .findAny()
          .orElse(null);

        Assert.assertNotNull("Debe estar entre los movimientos insertados", originalMovement);

      }
      List<ClearingData10> clearing10s = getDbClearingTransactions();
      Assert.assertEquals("Deben ser iguales",accountinMovements.size(),clearing10s.size());
    }
  }

  @Test
  public void processMovementForAccounting_TefTopup()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      List<Long> originalMovementsIds = new ArrayList<>();

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));


      List<AccountingData10> accountinMovements = getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), utc.toLocalDateTime());

      Assert.assertEquals("Debe ser 3 ", 3,accountinMovements.size());

      for (AccountingData10 m : accountinMovements) {
        Assert.assertNotNull("Debe tener id", m.getId());
        Assert.assertEquals("Debe ser CARGA_WEB", AccountingTxType.CARGA_WEB, m.getType());
        Assert.assertEquals("Debe ser Amount Mastercard -> 0", BigDecimal.ZERO, m.getAmountMastercard().getValue());
        Assert.assertEquals("Debe ser Amount usd -> 0", BigDecimal.ZERO, m.getAmountUsd().getValue());
        Assert.assertEquals("Debe ser Fee -> 0", BigDecimal.ZERO, m.getFee());
        Assert.assertEquals("Debe ser Fee Iva -> 0", BigDecimal.ZERO, m.getFee());
        Assert.assertEquals("Debe ser Collector Fee -> 0", BigDecimal.ZERO, m.getCollectorFee());
        Assert.assertEquals("Debe ser Collector Fee Iva -> 0", BigDecimal.ZERO, m.getCollectorFeeIva());
        Assert.assertEquals("Debe ser ExchangeRateDiff -> 0", BigDecimal.ZERO, m.getExchangeRateDif());
        BigDecimal amountBalance = BigDecimal.ZERO
          .add(m.getAmount().getValue())
          .subtract(m.getCollectorFee())
          .subtract(m.getCollectorFeeIva());

        Assert.assertEquals("AmountBalance = amount - fee - feeIva", amountBalance, m.getAmountBalance().getValue());

        Long originalMovement = originalMovementsIds.stream()
          .filter(mov -> mov.equals(m.getIdTransaction()))
          .findAny()
          .orElse(null);

        Assert.assertNotNull("Debe estar entre los movimientos insertados", originalMovement);

      }
      List<ClearingData10> clearing10s = getDbClearingTransactions();
      Assert.assertEquals("Deben ser iguales",accountinMovements.size(),clearing10s.size());
    }
  }

  @Test
  public void processMovementForAccounting_PosWithdraw()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(getRandomString(15));
      List<Long> originalMovementsIds = new ArrayList<>();

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

      List<AccountingData10> accountinMovements = getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), utc.toLocalDateTime());

      Assert.assertEquals("Debe ser 3 ", 3,accountinMovements.size());

      for (AccountingData10 m : accountinMovements) {
        Assert.assertNotNull("Debe tener id", m.getId());
        Assert.assertEquals("Debe ser RETIRO_POS", AccountingTxType.RETIRO_POS, m.getType());
        Assert.assertEquals("Debe ser Amount Mastercard -> 0", BigDecimal.ZERO, m.getAmountMastercard().getValue());
        Assert.assertEquals("Debe ser Amount usd -> 0", BigDecimal.ZERO, m.getAmountUsd().getValue());
        Assert.assertEquals("Debe ser Fee -> 0", BigDecimal.ZERO, m.getFee());
        Assert.assertEquals("Debe ser Fee Iva -> 0", BigDecimal.ZERO, m.getFee());
        Assert.assertEquals("Debe ser Fee Iva -> 0", BigDecimal.ZERO, m.getFeeIva());
        Assert.assertEquals("Debe ser ExchangeRateDiff -> 0", BigDecimal.ZERO, m.getExchangeRateDif());
        BigDecimal amountBalance = BigDecimal.ZERO
          .add(m.getAmount().getValue())
          .add(m.getCollectorFee())
          .add(m.getCollectorFeeIva());

        Assert.assertEquals("AmountBalance = amount + fee + feeIva", amountBalance, m.getAmountBalance().getValue());

        Long originalMovement = originalMovementsIds.stream()
          .filter(mov -> mov.equals(m.getIdTransaction()))
          .findAny()
          .orElse(null);

        Assert.assertNotNull("Debe estar entre los movimientos insertados", originalMovement);

      }
      List<ClearingData10> clearing10s = getDbClearingTransactions();
      Assert.assertEquals("Deben ser iguales",accountinMovements.size(),clearing10s.size());
    }
  }

  @Test
  public void processMovementForAccounting_TefWithdraw()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      List<Long> originalMovementsIds = new ArrayList<>();

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

      List<AccountingData10> accountinMovements = getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), utc.toLocalDateTime());

      // Retiros web no se procesan por este metodo
      Assert.assertEquals("Debe ser 0 ", 0,accountinMovements.size());
      List<ClearingData10> clearing10s = getDbClearingTransactions();
      Assert.assertEquals("Deben ser iguales",accountinMovements.size(),clearing10s.size());
    }
  }

  private List<ClearingData10> getDbClearingTransactions() {
    List<ClearingData10> trxs = new ArrayList<>();

    List<Map<String, Object>> rows = DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.clearing", SCHEMA));

    for (Map row : rows) {
      ClearingData10 cle = new ClearingData10();

      cle.setId((Long)(row.get("id")));
      cle.setId((Long)(row.get("accounting_id")));
      cle.setStatus(AccountingStatusType.fromValue((String)row.get("status")));
      trxs.add(cle);
    }

    return trxs;
  }

  @Test
  public void nothingToProcessMovementForAccounting()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

      List<Long> originalMovementsIds = new ArrayList<>();

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

      List<AccountingData10> accountinMovements = getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), utc.toLocalDateTime());

      Assert.assertEquals("Debe ser 0", 0, accountinMovements.size());
    }
  }

  private String getNewDate(Integer days, Timestamp ts) {
    LocalDateTime dt = ts.toLocalDateTime();
    dt = dt.minusDays(days);
    return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private void updateDate(Long idMovimiento, String newDate)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento_conciliado SET fecha_registro = "
        + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')"
        + "WHERE id_mov_ref = " + idMovimiento.toString());
  }
}
