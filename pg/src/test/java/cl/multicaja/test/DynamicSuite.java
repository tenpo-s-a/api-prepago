package cl.multicaja.test;

import org.junit.runners.Suite;

/**
 * @autor vutreras
 */
public class DynamicSuite extends Suite {

  public DynamicSuite(Class<?> setupClass) throws Exception {
    super(setupClass, TestSuite.suite());
  }
}
