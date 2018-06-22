package cl.multicaja.test.v10.api;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.test.TestSuite;
import cl.multicaja.test.v10.unit.TestBaseUnit;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class TestBaseUnitApi extends TestBaseUnit {

  private static Log log = LogFactory.getLog(TestBaseUnitApi.class);

  // Descomentar para probar los tests del API al payara
  /*
  static {
    System.setProperty("env", "development");
    PORT_HTTP = 8080;
    CONTEXT_PATH = "api-prepaid-1.0";
    System.setProperty("db.use.basicdatasource", "true");
  }
  */

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

  protected void topupUserBalance(User user, BigDecimal amount) {
    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
    prepaidTopup.getAmount().setValue(amount);
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup", toJson(prepaidTopup));
    System.out.println("respHttp: " + respHttp);
    Assert.assertEquals("Debe cargar ok", 201, respHttp.getStatus());
  }
}
