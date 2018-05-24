package cl.multicaja.test.db;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.ConfigUtils;

/**
 * @autor vutreras
 */
public class TestDbBasePg extends TestDbBase {

  protected static ConfigUtils configUtils = ConfigUtils.getInstance();

  protected static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");

  protected static final String SCHEMA_PARAMETERS = ConfigUtils.getInstance().getProperty("schema.parameters");
}
