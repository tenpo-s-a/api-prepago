package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_20180510114251_create_table_prp_tarjeta extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_tarjeta() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_tarjeta", true,
      new ColumnInfo("id", "bigserial", 19),
      new ColumnInfo("id_usuario", "int8", 19),
      new ColumnInfo("pan", "varchar", 16),
      new ColumnInfo("pan_encriptado", "varchar", 100),
      new ColumnInfo("contrato", "varchar", 20),
      new ColumnInfo("expiracion", "int4", 10),
      new ColumnInfo("estado", "varchar", 20),
      new ColumnInfo("nombre_tarjeta", "varchar", 100),
      new ColumnInfo("fecha_creacion", "timestamp", 29),
      new ColumnInfo("fecha_actualizacion", "timestamp", 29)
    );
    Assert.assertEquals("Existe tabla prp_tarjeta", true, exists);
  }
}
