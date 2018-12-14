package cl.multicaja.test.integration.v10.async;

import cl.multicaja.prepaid.model.v10.CurrencyUsd;
import cl.multicaja.test.integration.v10.helper.sftp.TestSftpServer;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

//FIXME Hacer refactor de los tests
@Ignore
public class Test_PendingCurrencyModification10 extends TestBaseUnitAsync {
  private static Log log = LogFactory.getLog(Test_PendingCurrencyModification10.class);

  @Test
  public void test_when_file_is_ok() throws Exception {
    final String filename = "TEST.AR.T058.OK";
    cleanTest(filename);
    putSuccessFileIntoSftp(filename);
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
    putSuccessFileIntoSftp(filename);
    CurrencyUsd currencyUsd = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia existir el nombre del archivo", currencyUsd.getFileName(), filename);
    putSuccessFileIntoSftp(filename);
    CurrencyUsd currencyUsdAfter = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia ser el mismo registro ", currencyUsd.getCreationDate(), currencyUsdAfter.getCreationDate());
  }

  @Test
  public void test_when_file_contains_error() throws Exception {
    final String filename = "TEST.AR.T058.OK";
    final String filenameError = "TEST.AR.T058.ERR";
    cleanTest(filename);
    putSuccessFileIntoSftp(filename);
    CurrencyUsd currencyUsd = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia existir el nombre del archivo", currencyUsd.getFileName(), filename);
    putSuccessFileIntoSftp(filenameError);
    CurrencyUsd currencyUsdAfter = getPrepaidCardEJBBean10().getCurrencyUsd();
    Assert.assertEquals("Deberia ser el archivo archivo sin errores", currencyUsd.getCreationDate(), currencyUsdAfter.getCreationDate());
  }

  private void putSuccessFileIntoSftp(String filename) throws Exception {
    final Map<String, Object> context = TestSftpServer.getInstance().openChanel();
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
    ChannelSftp channelSftp = (ChannelSftp) context.get("channel");
    channelSftp.put(inputStream, TestSftpServer.getInstance().BASE_DIR + "mastercard/T058/" + filename);
    channelSftp.exit();
    ((Session) context.get("session")).disconnect();
    log.info("Wait for camel process");
    Thread.sleep(3000);
  }

  private void cleanTest(String filename) throws Exception {
    CurrencyUsd currencyUsd = getPrepaidCardEJBBean10().getCurrencyUsd();
    if(currencyUsd != null && filename.equals(currencyUsd.getFileName())){
      deleteFileByName(filename);
    }
  }

  public void deleteFileByName(String filename){
    getDbUtils().getJdbcTemplate().update(String.format("delete from prepago.prp_valor_usd where nombre_archivo = '%s'", filename));
  }

}
