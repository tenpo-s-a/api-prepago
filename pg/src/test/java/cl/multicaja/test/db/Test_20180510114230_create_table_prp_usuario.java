package cl.multicaja.test.db;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.db.ColumnInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_20180510114230_create_table_prp_usuario extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_usuario() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_usuario", true,
      new ColumnInfo("id", "bigserial", 19),
      new ColumnInfo("id_usuario_mc", "int8", 19),
      new ColumnInfo("rut", "int4", 10),
      new ColumnInfo("estado", "varchar", 20),
      new ColumnInfo("fecha_creacion", "timestamp", 29),
      new ColumnInfo("fecha_actualizacion", "timestamp", 29)
    );
    Assert.assertEquals("Existe tabla prp_usuario", true, exists);
  }
}
