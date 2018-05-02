package cl.multicaja.test.api;

import org.junit.runners.Suite;

/**
 * @autor vutreras
 */
public class DynamicSuite extends Suite {

  public DynamicSuite(Class<?> setupClass) throws Exception {
    super(setupClass, TestSuite.suite());
  }
}
