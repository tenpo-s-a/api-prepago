package cl.multicaja.test;

import cl.multicaja.core.test.TestSuiteBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @autor vutreras
 */
@RunWith(DynamicSuite.class)
public class TestSuite extends TestSuiteBase {

  private static Log log = LogFactory.getLog(TestSuite.class);

  private static TestServer testServer = new TestServer();

  private static boolean serverRunning;

  @BeforeClass
  public static void setUp() throws Exception {
    System.setProperty("project.artifactId", "api-prepaid");
    testServer.start();
    serverRunning = true;
  }

  @AfterClass
  public static void tearDown() {
    testServer.stop();
    serverRunning = false;
  }

  /**
   *
   * @return
   */
  public static boolean isServerRunning() {
    return serverRunning;
  }

  public static Class<?>[] suite() throws Exception {
    String packageName = new TestSuite().getClass().getPackage().getName();
    log.info("packageName: " + packageName);
    Class[] classList = getClasses(packageName);
    log.info("------------ Lista de clases de test ------------");
    for (Class cls : classList) {
      log.info(cls.getSimpleName());
    }
    return  classList;
  }
}
