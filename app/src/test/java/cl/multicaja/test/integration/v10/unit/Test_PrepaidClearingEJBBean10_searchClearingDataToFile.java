package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_PrepaidClearingEJBBean10_searchClearingDataToFile extends TestBaseUnit {

  private static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");

  @BeforeClass
  @AfterClass
  public static void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", SCHEMA));
  }

  public ClearingData10 buildClearing() {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setUserAccountId(getUniqueLong());
    clearing10.setFileId(getUniqueLong());
    clearing10.setStatus(AccountingStatusType.PENDING);
    return clearing10;
  }

  @Test
  public void searchData() throws Exception{
    AccountingData10 accounting1 = buildRandomAccouting();
    List<AccountingData10> accounting1s = new ArrayList<>();
    accounting1s.add(accounting1);
    accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

    ClearingData10 clearing1 = buildClearing();
    clearing1.setAccountingId(accounting1s.get(0).getId());

    clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    Assert.assertNotNull("El objeto no puede ser Null",clearing1);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing1.getId().longValue());
    AccountingData10 accounting2 = buildRandomAccouting();

    List<AccountingData10> accounting2s = new ArrayList<>();
    accounting2s.add(accounting2);
    accounting2s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting2s);

    ClearingData10 clearing2 = buildClearing();
    clearing2.setAccountingId(accounting2s.get(0).getId());

    clearing2 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing2);
    Assert.assertNotNull("El objeto no puede ser Null", clearing2);
    Assert.assertNotEquals("El id no puede ser 0",0, clearing2.getId().longValue());

    ZonedDateTime zd = ZonedDateTime.now();
    ZonedDateTime endDay = zd.withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime endDayUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime endDayMidnight = endDayUtc.toLocalDateTime();


    List<ClearingData10> data = getPrepaidClearingEJBBean10().searchClearingDataToFile(null, endDayMidnight);

    Assert.assertNotNull("No deberia ser null", data);
    Assert.assertFalse("La lista no debe estar vacia", data.isEmpty());
    Assert.assertEquals("Debe tener 2 registros", 2, data.size());

    for (ClearingData10 d : data) {
      if(d.getId().equals(clearing1.getId())) {
        this.checkAttributes(accounting1, d);
      } else {
        this.checkAttributes(accounting2, d);
      }
    }
  }

  private void checkAttributes(AccountingData10 accounting, ClearingData10 clearingData10) {
    Assert.assertEquals("debe ser el mismo", accounting.getAmount().getValue().longValue(), clearingData10.getAmount().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmount().getCurrencyCode(), clearingData10.getAmount().getCurrencyCode());
    Assert.assertEquals("debe ser el mismo", accounting.getIdTransaction(), clearingData10.getIdTransaction());
    Assert.assertEquals("debe ser el mismo", accounting.getType(), clearingData10.getType());
    Assert.assertEquals("debe ser el mismo", accounting.getOrigin(), clearingData10.getOrigin());
    Assert.assertEquals("debe ser el mismo", accounting.getAccountingMovementType(), clearingData10.getAccountingMovementType());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountUsd().getValue().longValue(), clearingData10.getAmountUsd().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountMastercard().getValue().longValue(), clearingData10.getAmountMastercard().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountMastercard().getCurrencyCode(), clearingData10.getAmountMastercard().getCurrencyCode());
    Assert.assertEquals("debe ser el mismo", accounting.getExchangeRateDif().longValue(), clearingData10.getExchangeRateDif().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getFee().longValue(), clearingData10.getFee().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getFeeIva().longValue(), clearingData10.getFeeIva().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getCollectorFee().longValue(), clearingData10.getCollectorFee().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getCollectorFeeIva().longValue(), clearingData10.getCollectorFeeIva().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountBalance().getValue().longValue(), clearingData10.getAmountBalance().getValue().longValue());
    Assert.assertEquals("debe ser el mismo", accounting.getAmountBalance().getCurrencyCode(), clearingData10.getAmountBalance().getCurrencyCode());
  }

  @Test
  public void emptyData() throws Exception {
    ZonedDateTime zd = ZonedDateTime.now().minusDays(30);
    ZonedDateTime endDay = zd.withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime endDayUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime endDayMidnight = endDayUtc.toLocalDateTime();

    List<ClearingData10> data = getPrepaidClearingEJBBean10().searchClearingDataToFile(null,  endDayMidnight);

    Assert.assertNotNull("No deberia ser null", data);
    Assert.assertTrue("La lista debe estar vacia", data.isEmpty());
  }

  @Test
  public void shouldFail_missingTo() throws Exception{
    try{
      getPrepaidClearingEJBBean10().searchClearingDataToFile(null, null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Falta parametro", PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
    }
  }
}
