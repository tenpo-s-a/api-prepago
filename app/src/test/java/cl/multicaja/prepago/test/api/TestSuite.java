package cl.multicaja.prepago.test.api;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @autor vutreras
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  /**
   * Aqui se deben registrar todos las clases de test para ser ejecutadas
   */
  Test_ping_v10.class
})
public class TestSuite {

  private static TestServer testServer = new TestServer();

  @BeforeClass
  public static void setUp() throws Exception {
    testServer.start();
  }

  @AfterClass
  public static void tearDown() {
    testServer.stop();
  }
}
