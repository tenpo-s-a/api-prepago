package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Test_PrepaidAccountingEJBBean10_updateAccountingStatusAndConciliationDate extends TestBaseUnit {

  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private DateTimeFormatter conciliationDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");

  @Test
  public void update_Ok() throws Exception {

    AccountingData10 accounting10 = buildRandomAccouting();
    accounting10.setStatus(AccountingStatusType.OK);
    accounting10.setFileId(36L);
    accounting10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

    Instant instant = Instant.now();
    ZoneId z = ZoneId.of( "UTC" );
    ZonedDateTime zdt = instant.atZone(z);
    String reconciliationDate = zdt.format(dateTimeFormatter);

    getPrepaidAccountingEJBBean10().updateAccountingStatusAndConciliationDate(null, accounting10.getId(), AccountingStatusType.REVERSED, reconciliationDate);

    AccountingData10 updated = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, accounting10.getIdTransaction());
    Assert.assertEquals("Debe tener estado REJECTED", AccountingStatusType.REVERSED, updated.getAccountingStatus());
    Assert.assertEquals("debe tener la misma fecha de conciliacion", updated.getConciliationDate().toLocalDateTime().format(conciliationDateTimeFormatter), zdt.format(conciliationDateTimeFormatter));
  }

  @Test(expected = BadRequestException.class)
  public void update_IdNull() throws Exception {
    AccountingData10 accounting10 = new AccountingData10();
    getPrepaidAccountingEJBBean10().updateAccountingStatusAndConciliationDate(null, null, null, null);
    Assert.fail("No debe pasar por aqui.");
  }

  @Test(expected = BadRequestException.class)
  public void update_statusNull() throws Exception {
    getPrepaidAccountingEJBBean10().updateAccountingStatusAndConciliationDate(null, Long.MAX_VALUE, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void update_dateNull() throws Exception {
    getPrepaidAccountingEJBBean10().updateAccountingStatusAndConciliationDate(null, Long.MAX_VALUE, AccountingStatusType.OK, null);
  }
}
