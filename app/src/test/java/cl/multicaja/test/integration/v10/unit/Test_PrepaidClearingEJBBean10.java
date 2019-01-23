package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidClearingEJBBean10 extends TestBaseUnit {

  private static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");

  //@BeforeClass
  //@AfterClass
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

  //generateClearingFile

  @Test
  public void insertClearingOK3() throws Exception{
    ZonedDateTime zd = ZonedDateTime.now();
    ZonedDateTime midnight = zd.withHour(0).withMinute(0).withSecond(0).withNano(0);
    ZonedDateTime endDay = zd.withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime utdZd = ZonedDateTime.ofInstant(zd.toInstant(), ZoneOffset.UTC);
    ZonedDateTime midnightUtc = ZonedDateTime.ofInstant(midnight.toInstant(), ZoneOffset.UTC);
    ZonedDateTime endDaytUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime localNow = utdZd.toLocalDateTime();
    LocalDateTime localMidnight = midnightUtc.toLocalDateTime();
    LocalDateTime endDayMidnight = endDaytUtc.toLocalDateTime();

    System.out.println(midnight);
    System.out.println(zd);
    System.out.println(endDay);

    System.out.println("================");

    System.out.println(midnightUtc);
    System.out.println(utdZd);
    System.out.println(endDaytUtc);

    System.out.println("================");

    System.out.println(localMidnight);
    System.out.println(localNow);
    System.out.println(endDayMidnight);

  }

  @Test
  public void insertClearingOK2() throws Exception{
    AccountingData10 accounting10 = buildRandomAccouting();
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    ClearingData10 clearing10 = buildClearing();
    clearing10.setAccountingId(accounting10s.get(0).getId());

    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing10.getId().longValue());

    getPrepaidClearingEJBBean10().generateClearingFile(null, ZonedDateTime.now(ZoneId.of("America/Santiago")));
  }

  @Test
  public void insertClearingOK() throws Exception{
    AccountingData10 accounting10 = buildRandomAccouting();
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    ClearingData10 clearing10 = buildClearing();
    clearing10.setAccountingId(accounting10s.get(0).getId());

    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing10.getId().longValue());
  }

  @Test(expected = BaseException.class)
  public void insertClearingAccountingNotExist() throws Exception {
    ClearingData10 clearing10 = buildClearing();
    clearing10.setId(getUniqueLong());
    getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
  }

  @Test(expected = BadRequestException.class)
  public void insertClearing_fail_id() throws Exception{
    ClearingData10 clearing10 = buildClearing();
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
  }

  @Test
  public void updateClearingOK() throws Exception{
    AccountingData10 accounting10 = buildRandomAccouting();
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    ClearingData10 clearing10 = buildClearing();
    clearing10.setAccountingId(accounting10s.get(0).getId());
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing10.getId().longValue());

    ClearingData10 clearing2 = getPrepaidClearingEJBBean10().updateClearingData(null, clearing10.getId(), getUniqueLong(), AccountingStatusType.OK);
    Assert.assertNotNull("No debe ser Nul", clearing2);
    Assert.assertEquals("El status debe ser OK", AccountingStatusType.OK, clearing2.getStatus());
    Assert.assertNotEquals("No deben ser iguales", clearing2.getStatus(), clearing10.getStatus());

  }

  @Test(expected = BadRequestException.class)
  public void updateClearing_fail_id() throws Exception {
    AccountingData10 accounting10 = buildRandomAccouting();
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10s);

    ClearingData10 clearing10 = buildClearing();
    clearing10.setId(accounting10s.get(0).getId());
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);
    Assert.assertNotNull("El objeto no puede ser Null", clearing10);
    Assert.assertNotEquals("El id no puede ser 0", 0, clearing10.getId().longValue());

    ClearingData10 clearing2 = getPrepaidClearingEJBBean10().updateClearingData(null,null,getUniqueLong(),AccountingStatusType.OK);
  }

  @Test(expected = BadRequestException.class)
  public void updateClearing_fail_allNull() throws Exception{
    AccountingData10 accounting10 = buildRandomAccouting();
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10s);

    ClearingData10 clearing10 = buildClearing();
    clearing10.setId(accounting10s.get(0).getId());
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);
    Assert.assertNotNull("El objeto no puede ser Null", clearing10);
    Assert.assertNotEquals("El id no puede ser 0", 0, clearing10.getId().longValue());
    ClearingData10 clearing2 = getPrepaidClearingEJBBean10().updateClearingData(null, clearing10.getId(),null,null);
  }

  @Test
  public void searchClearingByIdOk() throws Exception{
    AccountingData10 accounting10 = buildRandomAccouting();
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10s);

    ClearingData10 clearing = buildClearing();
    clearing.setAccountingId(accounting10s.get(0).getId());
    clearing = getPrepaidClearingEJBBean10().insertClearingData(null, clearing);
    Assert.assertNotNull("El objeto no puede ser Null",clearing);
    Assert.assertNotEquals("El id no puede ser 0", 0, clearing.getId().longValue());

    ClearingData10 clearing2 = getPrepaidClearingEJBBean10().searchClearingDataById(null, clearing.getId());
    Assert.assertNotNull("El objeto no debe ser null", clearing2);
    Assert.assertEquals("Los id deben ser iguales", clearing.getId(), clearing2.getId());
    Assert.assertEquals("Los status deben ser iguales", clearing.getStatus(), clearing2.getStatus());

  }

}
