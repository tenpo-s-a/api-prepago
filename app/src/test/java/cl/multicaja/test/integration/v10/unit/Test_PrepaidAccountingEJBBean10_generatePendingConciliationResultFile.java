package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingFiles10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.db.DBUtils;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_PrepaidAccountingEJBBean10_generatePendingConciliationResultFile extends TestBaseUnit {

  private static List<ZonedDateTime> dates = new ArrayList<>();

  @BeforeClass
  public static void getDatesOfLastMonth() {

    ZonedDateTime start = ZonedDateTime.now()
      .minusMonths(2)
      .with(TemporalAdjusters.firstDayOfMonth());
    ZonedDateTime end = ZonedDateTime.now()
      .minusMonths(2)
      .with(TemporalAdjusters.lastDayOfMonth());

    // Agrega los dias del mes
    dates = Stream.iterate(start, date -> date.plusDays(1))
      .limit(ChronoUnit.DAYS.between(start, end) + 1)
      .collect(Collectors.toList());
  }

  @Before
  @After
  public void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", getSchemaAccounting()));
  }

  private LocalDateTime getRandomDateInUTC() {
    Random random = new Random();
    int r = random.ints(0, (dates.size() - 1)).findFirst().getAsInt();

    ZonedDateTime firstDayUtc = ZonedDateTime.ofInstant(dates.get(r).toInstant(), ZoneOffset.UTC);

    return firstDayUtc.toLocalDateTime();
  }

  @Test
  public void generateFile_sentPendingConciliation() throws Exception {
    Map<Long, AccountingData10> okData = new HashMap<>();
    Map<Long, AccountingData10> reversedData = new HashMap<>();
    Map<Long, AccountingData10> notConfirmedData = new HashMap<>();

    // ok
    {
      for (int i = 0; i < 10; i++) {
        AccountingData10 accounting1 = buildRandomAccouting();
        accounting1.setStatus(AccountingStatusType.SENT_PENDING_CON);
        accounting1.setAccountingStatus(AccountingStatusType.OK);
        LocalDateTime date = getRandomDateInUTC();
        accounting1.setTransactionDate(Timestamp.valueOf(date));
        accounting1.setConciliationDate(Timestamp.valueOf(getRandomDateInUTC().plusMonths(1)));

        accounting1 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting1);

        okData.put(accounting1.getId(), accounting1);
      }
    }

    // REVERSED
    {
      for (int i = 0; i < 10; i++) {
        AccountingData10 accounting1 = buildRandomAccouting();
        accounting1.setStatus(AccountingStatusType.SENT_PENDING_CON);
        accounting1.setAccountingStatus(AccountingStatusType.NOT_OK);
        LocalDateTime date = getRandomDateInUTC();
        accounting1.setTransactionDate(Timestamp.valueOf(date));
        accounting1.setConciliationDate(Timestamp.valueOf(getRandomDateInUTC().plusMonths(1)));

        accounting1 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting1);

        reversedData.put(accounting1.getId(), accounting1);
      }
    }

    // NOT_CONFIRM
    {
      for (int i = 0; i < 10; i++) {
        AccountingData10 accounting1 = buildRandomAccouting();
        accounting1.setStatus(AccountingStatusType.SENT_PENDING_CON);
        accounting1.setAccountingStatus(AccountingStatusType.NOT_OK);
        accounting1.setTransactionDate(Timestamp.valueOf(getRandomDateInUTC()));
        accounting1.setConciliationDate(Timestamp.valueOf(ZonedDateTime.now(ZoneOffset.UTC).plusYears(1000).toLocalDateTime()));

        accounting1 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting1);

        notConfirmedData.put(accounting1.getId(), accounting1);
      }
    }

    ZonedDateTime zd = ZonedDateTime.now();

    AccountingFiles10 accountingFile = getPrepaidAccountingEJBBean10().generatePendingConciliationResultFile(null, zd);
    Assert.assertNotNull("No deberia ser null", accountingFile);
    Assert.assertTrue("Debe tener id", accountingFile.getId() > 0);
    Assert.assertEquals("Debe estar en status PENDING", AccountingStatusType.PENDING, accountingFile.getStatus());


    // primer dia del mes anterior
    ZonedDateTime firstDay = zd
      .minusMonths(2)
      .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
    // ultimo dia del mes anterior
    ZonedDateTime lastDay = zd
      .minusMonths(2)
      .with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano( 999999999);

    ZonedDateTime firstDayUtc = ZonedDateTime.ofInstant(firstDay.toInstant(), ZoneOffset.UTC);
    ZonedDateTime lastDayUtc = ZonedDateTime.ofInstant(lastDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime ldtFrom = firstDayUtc.toLocalDateTime();
    LocalDateTime ldtTo = lastDayUtc.toLocalDateTime();


    List<AccountingData10> data =  getPrepaidAccountingEJBBean10().getAccountingDataForFile(null, ldtFrom, ldtTo, AccountingStatusType.SENT, null);
    Assert.assertNotNull("No deberia ser null", data);
    Assert.assertEquals("Debe tener 30 registros", 30,data.size());
    data.forEach(d-> {
      Assert.assertEquals("Debe tener el fileId", accountingFile.getId(), d.getFileId());
    });

    Path file = Paths.get("accounting_files/" + accountingFile.getName());
    Assert.assertTrue("Debe existir el archivo", Files.exists(file));

    List<AccountingData10> fileData = this.getCsvData("accounting_files/" + accountingFile.getName());

    fileData.forEach(d -> {
      if(okData.containsKey(d.getId())) {
        Assert.assertNotNull("Debe tener fecha de conciliacion", d.getConciliationDate());
        Assert.assertNull("No debe tener estado contable", d.getAccountingStatus());
      } else if( reversedData.containsKey(d.getId())) {
        Assert.assertNull("No debe tener fecha de conciliacion", d.getConciliationDate());
        Assert.assertEquals("Debe tener estado contable REVERSED", AccountingStatusType.NOT_CONFIRMED, d.getAccountingStatus());
      } else if(notConfirmedData.containsKey(d.getId())) {
        Assert.assertNull("No debe tener fecha de conciliacion", d.getConciliationDate());
        Assert.assertEquals("Debe tener estado contable NO CONFIRMADA", AccountingStatusType.NOT_CONFIRMED, d.getAccountingStatus());
      } else {
        Assert.fail("Should not be here");
      }
    });

    //Files.delete(file);
  }

  @Test
  public void shouldFail_missingDate() throws Exception{
    try{
      getPrepaidAccountingEJBBean10().generateAccountingFile(null, null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Falta parametro", PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
    }
  }

  private List<AccountingData10> getCsvData(String fileName) throws Exception {
    FileInputStream is = new FileInputStream(fileName);
    List<AccountingData10> listClearing;
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      CSVReader csvReader = new CSVReader(reader,',');
      csvReader.readNext();
      String[] record;
      listClearing = new ArrayList<>();

      while ((record = csvReader.readNext()) != null) {
        AccountingData10 acc = new AccountingData10();
        acc.setId(numberUtils.toLong(record[0]));
        acc.setIdTransaction(numberUtils.toLong(record[2]));

        String reconciliationDate = String.valueOf(record[7]);

        if(!StringUtils.isAllBlank(reconciliationDate)) {
          LocalDateTime ldt = LocalDateTime.parse(reconciliationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
          ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/Santiago"));

          ZonedDateTime utc = ZonedDateTime.ofInstant(zdt.toInstant(), ZoneOffset.UTC);

          acc.setConciliationDate(Timestamp.from(utc.toInstant()));
        }

        String accountingStatus = String.valueOf(record[19]);
        if(!StringUtils.isAllBlank(accountingStatus)) {
          acc.setAccountingStatus(AccountingStatusType.fromValue(accountingStatus));
        }

        listClearing.add(acc);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Exception: "+e);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
    return listClearing;
  }
}
