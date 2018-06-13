package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.test.TestSuite;
import cl.multicaja.test.v10.unit.TestBaseUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @autor vutreras
 */
public class TestBaseUnitApi extends TestBaseUnit {

  private static Log log = LogFactory.getLog(TestBaseUnitApi.class);

  @BeforeClass
  public static void beforeClass() throws Exception {
    if (ConfigUtils.isEnvTest()) {
      System.setProperty("db.use.basicdatasource", "true");
      if (!TestSuite.isServerRunning()) {
        TestSuite.startServer();
      }
    }
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (ConfigUtils.isEnvTest()) {
      if (!TestSuite.isRunningInTestSuite() && TestSuite.isServerRunning()) {
        TestSuite.stopServer();
      } else {
        log.warn("No es necesario detener el servidor dado que se encuentra en suite");
      }
    }
  }
}
