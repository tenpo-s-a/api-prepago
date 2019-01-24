package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingFiles10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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

    Path file = Paths.get("clearing_files/" + clearingFile.getName());
    Assert.assertTrue("Debe existir el archivo", Files.exists(file));
    Files.delete(file);

  }

  @Test
  public void generateFileWithBankId() throws Exception{
    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    InclusionMovimientosDTO mov =  topupInTecnocom(prepaidCard, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw = null;

    try {
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw,true);

    } catch(Exception vex) {
      Assert.fail("No debe pasar por acá");
    }

    Thread.sleep(2000);

    AccountingFiles10 clearingFile = getPrepaidClearingEJBBean10().generateClearingFile(null, ZonedDateTime.now());

    Assert.assertNotNull("No deberia ser null", clearingFile);
    Assert.assertTrue("Debe tener id", clearingFile.getId() > 0);

    Path file = Paths.get("clearing_files/" + clearingFile.getName());
    Assert.assertTrue("Debe existir el archivo", Files.exists(file));
    //Files.delete(file);

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
}
