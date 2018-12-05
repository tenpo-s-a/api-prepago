package cl.multicaja.test;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.ConfigUtils;

/**
 * @autor vutreras
 */
public class TestDbBasePg extends TestDbBase {

  protected static ConfigUtils configUtils = ConfigUtils.getInstance();

  protected static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");

  protected static final String SCHEMA_ACCOUNTING = ConfigUtils.getInstance().getProperty("schema.acc");

  protected static final String SCHEMA_PARAMETERS = ConfigUtils.getInstance().getProperty("schema.parameters");

  protected static final String SCHEMA_CDT = ConfigUtils.getInstance().getProperty("schema.cdt");
}
