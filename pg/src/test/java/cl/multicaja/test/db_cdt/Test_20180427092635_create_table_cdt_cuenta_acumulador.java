package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180427092635_create_table_cdt_cuenta_acumulador extends TestDbBasePg {

  /**********************************************************
  *       id                    BIGSERIAL NOT NULL,
  *       id_regla_acumulacion  BIGSERIAL NOT NULL,
  *       id_cuenta             BIGSERIAL NOT NULL,
  *       monto                 DECIMAL   NOT NULL,
  *       fecha_inicio          TIMESTAMP NOT NULL,
  *       fecha_fin             TIMESTAMP NOT NULL,
  *       fecha_creacion        TIMESTAMP NOT NULL,
  *       fecha_actualizacion   TIMESTAMP NOT NULL,
  ***********************************************************/

  @Test
  public void CheckTableCuentaAcumulador() {

      boolean exists = dbUtils.tableExists(SCHEMA_CDT, Constants.Tables.CUENTA_ACUMULADOR.getName(), false,
      new ColumnInfo("id", "BIGSERIAL",19),
      new ColumnInfo("id_regla_acumulacion", "INT8", 19),
      new ColumnInfo("id_cuenta", "INT8", 19),
      new ColumnInfo("monto", "NUMERIC", 131089),
      new ColumnInfo("fecha_inicio", "TIMESTAMP", 29),
      new ColumnInfo("fecha_fin", "TIMESTAMP", 29),
      new ColumnInfo("fecha_creacion", "TIMESTAMP", 29),
      new ColumnInfo("fecha_actualizacion", "TIMESTAMP", 29));
      Assert.assertEquals("Existe tabla "+SCHEMA_CDT+"."+Constants.Tables.CUENTA_ACUMULADOR.getName(), true, exists);
  }


}
