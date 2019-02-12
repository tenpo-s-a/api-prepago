package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.db.DBUtils;
import org.junit.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_PrepaidAccountingEJBBean10_searchAccountingDataForFile extends TestBaseUnit {

  private static List<LocalDateTime> dates = new ArrayList<>();

  @BeforeClass
  public static void getDatesOfMonth() {

    LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
    LocalDateTime end = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());

    // Agrega los dias del mes
    dates = Stream.iterate(start, date -> date.plusDays(1))
      .limit(ChronoUnit.DAYS.between(start, end) + 1)
      .collect(Collectors.toList());
  }

  @Before
  @After
  public void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
  }

  private LocalDateTime getRandomDate() {
    Random random = new Random();
    int r = random.ints(0, (dates.size() - 1)).findFirst().getAsInt();
    return dates.get(r);
  }

  @Test
  public void searchDataByDate() throws Exception {
    Map<Long, AccountingData10> okData = new HashMap<>();

    // ok
    {
      for (int i = 0; i < 10; i++) {
        AccountingData10 accounting1 = buildRandomAccouting();
        accounting1.setStatus(AccountingStatusType.PENDING);
        accounting1.setTransactionDate(Timestamp.from(getRandomDate().toInstant(ZoneOffset.UTC)));

        accounting1 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting1);

        okData.put(accounting1.getId(), accounting1);
      }
    }

    // Reversed
    {
      AccountingData10 accounting1 = buildRandomAccouting();
      accounting1.setStatus(AccountingStatusType.REVERSED);
      accounting1.setTransactionDate(Timestamp.from(getRandomDate().toInstant(ZoneOffset.UTC)));
      accounting1 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting1);
    }

    // Not in  range
    {
      {
        AccountingData10 accounting1 = buildRandomAccouting();
        accounting1.setStatus(AccountingStatusType.PENDING);
        accounting1.setTransactionDate(Timestamp.from(getRandomDate().plusMonths(1).toInstant(ZoneOffset.UTC)));
        accounting1 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting1);
      }
      {
        AccountingData10 accounting1 = buildRandomAccouting();
        accounting1.setStatus(AccountingStatusType.PENDING);
        accounting1.setTransactionDate(Timestamp.from(getRandomDate().minusMonths(1).toInstant(ZoneOffset.UTC)));
        accounting1 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting1);
      }
    }

    ZonedDateTime zd = ZonedDateTime.now();

    // primer dia del mes
    ZonedDateTime firstDay = zd.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
    // ultimo dia del mes
    ZonedDateTime lastDay = zd.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime firstDayUtc = ZonedDateTime.ofInstant(firstDay.toInstant(), ZoneOffset.UTC);
    ZonedDateTime lastDayUtc = ZonedDateTime.ofInstant(lastDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime ldtFrom = firstDayUtc.toLocalDateTime();
    LocalDateTime ldtTo = lastDayUtc.toLocalDateTime();

    List<AccountingData10> data = getPrepaidAccountingEJBBean10().getAccountingDataForFile(null, ldtFrom, ldtTo, AccountingStatusType.PENDING);

    Assert.assertNotNull("No deberia ser null", data);
    Assert.assertFalse("La lista no debe estar vacia", data.isEmpty());
    Assert.assertEquals("Debe tener 10 registros", 10, data.size());


    for (AccountingData10 d : data) {
      AccountingData10 acc = okData.get(d.getId());
      Assert.assertNotNull("debe ser de los OK", acc);
      this.checkAttributes(acc, d);
    }

  }

  private void checkAttributes(AccountingData10 accounting, AccountingData10 data) {
    Assert.assertEquals("debe ser el mismo", accounting.getAmount().getValue().longValue(), data.getAmount().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmount().getCurrencyCode(), data.getAmount().getCurrencyCode());
    Assert.assertEquals("debe ser el mismo", accounting.getIdTransaction(), data.getIdTransaction());
    Assert.assertEquals("debe ser el mismo", accounting.getType(), data.getType());
    Assert.assertEquals("debe ser el mismo", accounting.getOrigin(), data.getOrigin());
    Assert.assertEquals("debe ser el mismo", accounting.getAccountingMovementType(), data.getAccountingMovementType());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountUsd().getValue().longValue(), data.getAmountUsd().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountMastercard().getValue().longValue(), data.getAmountMastercard().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountMastercard().getCurrencyCode(), data.getAmountMastercard().getCurrencyCode());
    Assert.assertEquals("debe ser el mismo", accounting.getExchangeRateDif().longValue(), data.getExchangeRateDif().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getFee().longValue(), data.getFee().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getFeeIva().longValue(), data.getFeeIva().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getCollectorFee().longValue(), data.getCollectorFee().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getCollectorFeeIva().longValue(), data.getCollectorFeeIva().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountBalance().getValue().longValue(), data.getAmountBalance().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountBalance().getCurrencyCode(), data.getAmountBalance().getCurrencyCode());
  }

  @Test
  public void emptyData() throws Exception {
    ZonedDateTime zd = ZonedDateTime.now();

    // primer dia del mes
    ZonedDateTime firstDay = zd.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
    // ultimo dia del mes
    ZonedDateTime lastDay = zd.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime firstDayUtc = ZonedDateTime.ofInstant(firstDay.toInstant(), ZoneOffset.UTC);
    ZonedDateTime lastDayUtc = ZonedDateTime.ofInstant(lastDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime ldtFrom = firstDayUtc.toLocalDateTime();
    LocalDateTime ldtTo = lastDayUtc.toLocalDateTime();

    List<AccountingData10> data = getPrepaidAccountingEJBBean10().getAccountingDataForFile(null,  ldtFrom, ldtTo, AccountingStatusType.PENDING);

    Assert.assertNotNull("No deberia ser null", data);
    Assert.assertTrue("La lista debe estar vacia", data.isEmpty());
  }

  @Test
  public void shouldFail_missingFrom() throws Exception{
    try{
      getPrepaidAccountingEJBBean10().getAccountingDataForFile(null, null, null, null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Falta parametro", PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
    }
  }

  @Test
  public void shouldFail_missingTo() throws Exception{
    try{
      getPrepaidAccountingEJBBean10().getAccountingDataForFile(null, LocalDateTime.now(), null, null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Falta parametro", PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
    }
  }

  @Test
  public void shouldFail_missingStatus() throws Exception{
    try{
      getPrepaidAccountingEJBBean10().getAccountingDataForFile(null, LocalDateTime.now(), LocalDateTime.now(), null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Falta parametro", PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
    }
  }
}
