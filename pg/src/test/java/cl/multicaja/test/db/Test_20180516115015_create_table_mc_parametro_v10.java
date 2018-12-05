package cl.multicaja.test.db;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor abarazarte
 */
public class Test_20180516115015_create_table_mc_parametro_v10 extends TestDbBasePg {

  protected static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");

  @Test
  public void checkIfTableExists_prp_parametro(){
    boolean exists = dbUtils.tableExists(SCHEMA, "mc_parametro", Boolean.TRUE,
      new ColumnInfo("id", "bigserial", 19),
      new ColumnInfo("aplicacion", "varchar", 25),
      new ColumnInfo("nombre", "varchar", 100),
      new ColumnInfo("version", "varchar", 5),
      new ColumnInfo("valor", "json", 2147483647),
      new ColumnInfo("expiracion", "int8", 19),
      new ColumnInfo("fecha_creacion", "timestamp", 29)
    );

    Assert.assertTrue("Existe la tabla mc_parametro", exists);
  }
}
