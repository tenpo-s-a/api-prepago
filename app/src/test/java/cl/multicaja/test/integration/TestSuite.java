package cl.multicaja.test.integration;

import cl.multicaja.core.test.TestSuiteBase;
import cl.multicaja.test.integration.v10.helper.sftp.TestSftpServer;
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

  private static boolean runningInTestSuite = false;

  /**
   *
   * @throws Exception
   */
  public static void startServer() throws Exception {
    System.setProperty("project.artifactId", "api-prepaid");
    TestSftpServer.getInstance().start();
    TestSftpServer.getInstance().createDirectories();
    testServer.start();
  }

  /**
   *
   */
  public static void stopServer() throws Exception {
    TestSftpServer.getInstance().end();
    testServer.stop();
  }

  @BeforeClass
  public static void setUp() throws Exception {
    runningInTestSuite = true;
    startServer();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    stopServer();
  }

  /**
   *
   * @return
   */
  public static boolean isServerRunning() {
    return testServer.isServerRunning();
  }

  /**
   *
   * @return
   */
  public static boolean isRunningInTestSuite() {
    return runningInTestSuite;
  }

  public static Class<?>[] suite() throws Exception {
    String packageName = TestSuite.class.getPackage().getName();
    log.info("packageName: " + packageName);
    Class[] classList = getClasses(packageName);
    log.info("------------ Lista de clases de test ------------");
    for (Class cls : classList) {
      log.info(cls.getSimpleName());
    }
    return  classList;
  }
}
