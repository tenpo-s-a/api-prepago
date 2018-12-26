package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.CurrencyUsd;
import cl.multicaja.test.integration.v10.async.TestBaseUnitAsync;
import org.junit.*;

import java.io.InputStream;

import static cl.multicaja.core.model.Errors.FILE_ALREADY_PROCESSED;

public class Test_PendingCurrencyModification10 extends TestBaseUnitAsync {

  @AfterClass
  public static void clearData() {
    getDbUtils().getJdbcTemplate().update(String.format("truncate %s.prp_valor_usd", getSchema()));
  }

  @Before
  public void clearData2() {
    getDbUtils().getJdbcTemplate().update(String.format("truncate %s.prp_valor_usd", getSchema()));
  }

  @Test
  public void test_when_file_is_ok() throws Exception {
    final String filename = "TEST.AR.T058.OK";
    cleanTest(filename);

    InputStream is = putSuccessFileIntoSftp(filename);
    try {
      getPrepaidCardEJBBean10().processMastercardUsdFile(is, filename);
    } catch (Exception ex) {
      Assert.fail("Should not be here");
    }

    CurrencyUsd currencyUsd = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia existir el nombre del archivo", currencyUsd.getFileName(), filename);
    Assert.assertTrue("Deberia ser > 0", currencyUsd.getBuyCurrencyConvertion() > 0);
    Assert.assertTrue("Deberia ser > 0", currencyUsd.getMidCurrencyConvertion() > 0);
    Assert.assertTrue("Deberia ser > 0", currencyUsd.getSellCurrencyConvertion() > 0);
  }

  @Test
  public void test_when_file_was_processed() throws Exception {
    final String filename = "TEST.AR.T058.OK";
    cleanTest(filename);

    InputStream is = putSuccessFileIntoSftp(filename);
    try {
      getPrepaidCardEJBBean10().processMastercardUsdFile(is, filename);
    } catch (Exception ex) {
      Assert.fail("Should not be here");
    }

    CurrencyUsd currencyUsd = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia existir el nombre del archivo", currencyUsd.getFileName(), filename);

    InputStream is2 = putSuccessFileIntoSftp(filename);
    try {
      getPrepaidCardEJBBean10().processMastercardUsdFile(is2, filename);
    } catch (ValidationException ex) {
      Assert.assertEquals("Archivo ya procesado", FILE_ALREADY_PROCESSED.getValue(), ex.getCode());
    }
  }

  @Test
  public void test_when_file_contains_error() throws Exception {
    final String filename = "TEST.AR.T058.OK";
    final String filenameError = "TEST.AR.T058.ERR";
    cleanTest(filename);
    InputStream is = putSuccessFileIntoSftp(filename);
    try {
      getPrepaidCardEJBBean10().processMastercardUsdFile(is, filename);
    } catch (Exception ex) {
      Assert.fail("Should not be here");
    }
    CurrencyUsd currencyUsd = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia existir el nombre del archivo", currencyUsd.getFileName(), filename);
    InputStream is2 = putSuccessFileIntoSftp(filename);
    try {
      getPrepaidCardEJBBean10().processMastercardUsdFile(is2, filenameError);
    } catch (Exception ex) {
      Assert.fail("Should not be here");
    }
    CurrencyUsd currencyUsdAfter = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia ser el archivo archivo sin errores", currencyUsd.getCreationDate(), currencyUsdAfter.getCreationDate());
  }

  private InputStream putSuccessFileIntoSftp(String filename) throws Exception {
    return this.getClass().getClassLoader().getResourceAsStream(filename);
  }

  private void cleanTest(String filename) throws Exception {
    CurrencyUsd currencyUsd = getPrepaidCardEJBBean10().getCurrencyUsd();
    if(currencyUsd != null && filename.equals(currencyUsd.getFileName())){
      getDbUtils().getJdbcTemplate().update(String.format("delete from prepago.prp_valor_usd where nombre_archivo = '%s'", filename));
    }
  }

}
