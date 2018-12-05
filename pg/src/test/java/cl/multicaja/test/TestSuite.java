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

  @BeforeClass
  public static void setUp() throws Exception {
  }

  @AfterClass
  public static void tearDown() {
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


