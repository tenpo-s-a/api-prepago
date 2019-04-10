package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import com.opencsv.CSVReader;
import org.junit.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_PrepaidClearingEJBBean10_generateClearingFile extends TestBaseUnitAsync {
  private static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");

  @Before
  @After
  public void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", SCHEMA));
  }

  public ClearingData10 buildClearing() {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setUserAccountId(getUniqueLong());
    clearing10.setFileId(getUniqueLong());
    clearing10.setStatus(AccountingStatusType.PENDING);
    return clearing10;
  }

  @Test
  public void generateFile() throws Exception{
    AccountingData10 accounting1 = buildRandomAccouting();
    List<AccountingData10> accounting1s = new ArrayList<>();
    accounting1s.add(accounting1);
    accounting1s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting1s);

    ClearingData10 clearing1 = buildClearing();
    clearing1.setAccountingId(accounting1s.get(0).getId());
    clearing1.setUserAccountId(0L);

    clearing1 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing1);
    Assert.assertNotNull("El objeto no puede ser Null",clearing1);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing1.getId().longValue());
    AccountingData10 accounting2 = buildRandomAccouting();

    List<AccountingData10> accounting2s = new ArrayList<>();
    accounting2s.add(accounting2);
    accounting2s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting2s);

    ClearingData10 clearing2 = buildClearing();
    clearing2.setAccountingId(accounting2s.get(0).getId());
    clearing2.setUserAccountId(0L);

    clearing2 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing2);
    Assert.assertNotNull("El objeto no puede ser Null", clearing2);
    Assert.assertNotEquals("El id no puede ser 0",0, clearing2.getId().longValue());

    AccountingFiles10 clearingFile = getPrepaidClearingEJBBean10().generateClearingFile(null, ZonedDateTime.now());

    Assert.assertNotNull("No deberia ser null", clearingFile);
    Assert.assertTrue("Debe tener id", clearingFile.getId() > 0);
    Assert.assertEquals("Debe estar en status PENDING", AccountingStatusType.PENDING, clearingFile.getStatus());

    List<ClearingData10> data =  getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.SENT, null);
    Assert.assertNotNull("No deberia ser null", data);
    Assert.assertEquals("Debe tener 2 registros", 2,data.size());
    data.forEach(d-> {
      Assert.assertEquals("Debe tener el fileId", clearingFile.getId(), d.getFileId());
    });

    Path file = Paths.get("clearing_files/" + clearingFile.getName());
    Assert.assertTrue("Debe existir el archivo", Files.exists(file));

    validateCsvFile("clearing_files/" + clearingFile.getName(), 2);
    Files.delete(file);
  }

  @Test
  public void generateFileWithBankId() throws Exception{

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw = null;

    try{
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
    } catch(Exception vex) {
      Assert.fail("No debe pasar por acá");
    }

    Thread.sleep(2000);

    AccountingFiles10 clearingFile = getPrepaidClearingEJBBean10().generateClearingFile(null, ZonedDateTime.now());

    Assert.assertNotNull("No deberia ser null", clearingFile);
    Assert.assertTrue("Debe tener id", clearingFile.getId() > 0);
    Assert.assertEquals("Debe estar en status PENDING", AccountingStatusType.PENDING, clearingFile.getStatus());

    List<ClearingData10> data =  getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.SENT, null);
    Assert.assertNotNull("No deberia ser null", data);
    Assert.assertEquals("Debe tener 1 registros", 1,data.size());
    data.forEach(d-> {
      Assert.assertEquals("Debe tener el fileId", clearingFile.getId(), d.getFileId());
    });

    Path file = Paths.get("clearing_files/" + clearingFile.getName());
    Assert.assertTrue("Debe existir el archivo", Files.exists(file));
    validateCsvFile("clearing_files/" + clearingFile.getName(), 1);
    Files.delete(file);
  }

  @Test
  public void shouldFail_missingDate() throws Exception{
    try{
      getPrepaidClearingEJBBean10().generateClearingFile(null, null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Falta parametro", PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
    }
  }

  private void validateCsvFile(String fileName, int size) throws Exception {
    List<ClearingData10> data = getCsvData(fileName);

    Assert.assertEquals(String.format("Debe tener %s registros", size), size, data.size());
  }

  public List<ClearingData10> getCsvData(String fileName) throws Exception {
    FileInputStream is = new FileInputStream(fileName);
    List<ClearingData10> listClearing;
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      CSVReader csvReader = new CSVReader(reader,',');
      csvReader.readNext();
      String[] record;
      listClearing = new ArrayList<>();

      while ((record = csvReader.readNext()) != null) {
        ClearingData10 clearing = new ClearingData10();
        clearing.setId(numberUtils.toLong(record[0]));
        clearing.setIdTransaction(numberUtils.toLong(record[2]));
        listClearing.add(clearing);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Exception: "+e);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
    return listClearing;
  }
}
