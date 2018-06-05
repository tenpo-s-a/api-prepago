package cl.multicaja.test.v10.api;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.test.TestSuite;
import cl.multicaja.test.v10.unit.TestBaseUnit;
import org.junit.BeforeClass;

/**
 * @autor vutreras
 */
public class TestBaseUnitApi extends TestBaseUnit {

  @BeforeClass
  public static void beforeClass() throws Exception {

    /**
     * IMPORTANTE: Esto se hace en caso que se qieran probar metodos del api de forma directa
     * para estos casos se sobre-escribe el ambiente y el host
     * para apuntar al api desplegada en el payara de development
     */
    if (!TestSuite.isServerRunning()) {

      System.setProperty("db.use.basicdatasource", "true");
      System.setProperty("env", "development");
      System.setProperty("api_host", "http://127.0.0.1:8080");

      TestApiBase.CONTEXT_PATH = ConfigUtils.getInstance().getModuleProperty("context.path");
    }
  }
}
