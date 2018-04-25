package cl.multicaja.prepago.test.db;

import org.junit.runners.Suite;

/**
 * @autor vutreras
 */
public class DynamicSuite extends Suite {

  public DynamicSuite(Class<?> setupClass) throws Exception {
    super(setupClass, TestSuite.suite());
  }
}
